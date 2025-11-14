/*
 * Copyright (c) 2025 [bitryon.io]
 * Licensed under the Elastic License 2.0
 * https://www.elastic.co/licensing/elastic-license
 */
package io.bitryon.logger.spring;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.bitryon.logger.Logger;
import io.bitryon.logger.PreDefinition;
import io.bitryon.logger.boostrap.LoggingMethodIntercepter;
import io.bitryon.logger.helper.LogLineHelper;
import io.bitryon.logger.helper.LoggingUnitMethodHelper;
import io.bitryon.logger.model.LogLineInvoke;
import io.bitryon.logger.model.LogLineSample;

/**
 * helper for the log sample
 */
public final class LogLineInvokerHelper {
	
	/**
	 * constructor
	 */
	public LogLineInvokerHelper() {}

	private static final ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);;

	private static Class<?> getRealClass(Object bean) throws Exception {
		if (bean instanceof Advised) {
			if (AopUtils.isCglibProxy(bean) || AopUtils.isAopProxy(bean)) {
				return AopProxyUtils.ultimateTargetClass(bean);
			}else {
				return ((Advised) bean).getTargetSource().getTarget().getClass();
			}
		} else if (bean instanceof Class){
			return (Class<?>) bean;
		}else {
			return bean.getClass();
		}
	}

	/**
	 * Get the methods of the object.
	 * Non final/native/abstract, must not be private
	 * 
	 * @param objects the src
	 * @return the methods to the object
	 * @throws Exception 
	 * @throws SecurityException 
	 */
	public static Map<Method, Object> loadMethods(Object... objects) throws SecurityException, Exception {
		Map<Method, Object> result = new HashMap<>();
		for(Object object : objects) {
			for(Method method : getRealClass(object).getDeclaredMethods()) {
				if (Modifier.isFinal(method.getModifiers())
						|| Modifier.isAbstract(method.getModifiers())
						|| Modifier.isNative(method.getModifiers())
						|| Modifier.isPrivate(method.getModifiers())
						) {
					continue;
				}
				result.put(method, object);
			}
		}
		return result;
	}

	public static Object transformObject(Object object) throws JsonProcessingException {
		if (object==null) { return null; }
		String jsonPayload = objectMapper.writeValueAsString(object);
		return objectMapper.readValue(jsonPayload, Object.class);
	}

	/**
	 * <pre>
	 * The structural match if the value is Collection, Map and Array, equals at the exact same depth like json.
	 * it will transform the structure to Collection, Map and Array then match if there is a subset.
	 * </pre>
	 * 
	 * @param subsetObject
	 * @param supersetObject
	 * @return
	 */
	public static boolean containSubset(Object subsetObject, Object supersetObject) throws JsonProcessingException {
		Object subset   = transformObject(subsetObject);
		Object superset = transformObject(supersetObject);
		return LogLineHelper.containObject(subset, superset);
	}
	
	/**
	 * pull the log sample
	 * @param url full URL
	 * @param app_key app key for the application
	 * @param offset size of pagination
	 * @param size size of pagination
	 * @return The list of the response
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static List<LogLineSample> pullLogLineSamples(String url, String app_key, int offset, int size) throws MalformedURLException, IOException, URISyntaxException {
		String apiUrl = url + "?app_key="+app_key+"&offset="+offset+"&size="+size;

		// Create connection
		HttpURLConnection conn = (HttpURLConnection) (new URI(apiUrl).toURL()).openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");

		// Check response
		int status = conn.getResponseCode();
		if (status == HttpURLConnection.HTTP_OK) {
			InputStream input = conn.getInputStream();
			
			JsonNode json = objectMapper.readTree(input);
			input.close();
			JsonNode entity = json.get("entity");
			if (entity != null) {
				List<LogLineSample> logLineSamples = objectMapper.convertValue(entity, new TypeReference<List<LogLineSample>>() {});
				return logLineSamples;
			}
		} else {
			System.err.println("Request failed with HTTP "+url+" status: " + status);
		}

		conn.disconnect();
		return null;
	}

	public static List<LogLineSample> pullAllSamples(String url, String app_key, int max) throws InterruptedException, MalformedURLException, IOException, URISyntaxException {
		List<LogLineSample> samples = new ArrayList<>();
		for(int k=0,size=100; k<max; k+=size) {
			Thread.sleep(100);
			
			List<LogLineSample> logLineSampleList = LogLineInvokerHelper.pullLogLineSamples(url, app_key, k, size);
			if (logLineSampleList == null || logLineSampleList.isEmpty()) {
				break;
			}
			
			samples.addAll(logLineSampleList);
		}
		return samples;
	}
	
	public static Object[] parseReturnObject(Method method, String jsonPayload) throws JsonProcessingException, IllegalArgumentException {
		Object[] returnAndParameter = new Object[2];
		ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(jsonPayload);
		
		Type returnType = method.getGenericReturnType();
		JsonNode returnValue = arrayNode.get(0);
		returnAndParameter[0] = objectMapper.treeToValue(returnValue, objectMapper.getTypeFactory().constructType(returnType));
		
		JsonNode parameterValue = arrayNode.get(1);
		if (parameterValue!=null) {
			Class<?>[] paramTypes = method.getParameterTypes();
			Object[] argsForMethod = new Object[paramTypes.length];
			Iterator<JsonNode> parameterElements = parameterValue.elements();
			int index = 0;
			while (parameterElements.hasNext()) {
				JsonNode valueNode = parameterElements.next();
				argsForMethod[index] = objectMapper.treeToValue(valueNode, paramTypes[index++]);
			}
			returnAndParameter[1] = argsForMethod;
		}
		return returnAndParameter;
	}

	public static Object[] parseParameters(Method method, String jsonPayload) throws JsonProcessingException, IllegalArgumentException {
		ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(jsonPayload);
		
		Class<?>[] paramTypes = method.getParameterTypes();
		Object[] argsForMethod = new Object[paramTypes.length];
		JsonNode parameterValue = arrayNode.get(0);
		Iterator<JsonNode> parameterElements = parameterValue.elements();
		int index = 0;
		while (parameterElements.hasNext()) {
			JsonNode valueNode = parameterElements.next();
			argsForMethod[index] = objectMapper.treeToValue(valueNode, paramTypes[index++]);
		}
		return argsForMethod;
	}

	public static void invokeSample(Logger logger, LogLineInvoke logLineInvoke) throws IllegalAccessException, InvocationTargetException, JsonProcessingException, IllegalArgumentException{
		if (logLineInvoke == null || logLineInvoke.getParameterSample() == null || logLineInvoke.getMethod() == null) { return; }
		try {
			PreDefinition.TYPE type = PreDefinition.TYPE.getType(logLineInvoke.getParameterSample().getType());
			switch(type) {
			case TEXT:
				logLineInvoke.setArgsForMethod(new Object[] {logLineInvoke.getParameterSample().getPayload()});
				LogLineHelper.invokeLogLine(logger, logLineInvoke);
				break;
			case JSON:
				Object[] argsForMethod = parseParameters(logLineInvoke.getMethod(), logLineInvoke.getParameterSample().getPayload());
				logLineInvoke.setArgsForMethod(argsForMethod);
				LogLineHelper.invokeLogLine(logger, logLineInvoke);
				break;
			default:
				System.err.println("type["+logLineInvoke.getParameterSample().getType()+"] no supported");
			}
		}finally {
			logger.reset();
		}
	}
	
	public static void invokeAllMethodsByLogLineSamples(LoggingMethodIntercepter loggingMethodIntercepter, Map<Method, Object> objects, List<LogLineInvoke> logLineInvokes) throws InterruptedException, JsonProcessingException, IllegalAccessException, InvocationTargetException, IllegalArgumentException {
		for(Method method:objects.keySet()) {
			loggingMethodIntercepter.addTargetMethod(method, null);
		}
		Map<String, Method> onTargetMethodSteps = LoggingUnitMethodHelper.convertToStepMethods(loggingMethodIntercepter.getOnTargetMethods());
		for(LogLineInvoke logLineInvoke : logLineInvokes) {
			Method method = LogLineHelper.getMatchedMethod(onTargetMethodSteps, logLineInvoke.getParameterSample().getStep());
			if (method==null) { continue; }
			Object object = objects.get(method);
			if (object==null) { continue; }

			logLineInvoke.setMethod(method);
			logLineInvoke.setTargetObject(object);
			
			invokeSample(loggingMethodIntercepter.getLogger(), logLineInvoke);
		}
	}
	
	public static LogLineInvoke createLogLineInvoke(LogLineSample logLineSample, Map<String, Method> onTargetMethodSteps, Map<Method, Object> methodToBeans) {
		LogLineInvoke logLineInvoke = new LogLineInvoke();
		logLineInvoke.setParameterSample(logLineSample);
		
		Method method = LogLineHelper.getMatchedMethod(onTargetMethodSteps, logLineInvoke.getParameterSample().getStep());
		if (method!=null) { logLineInvoke.setMethod(method); }
		Object object = methodToBeans.get(method);
		if (object!=null) { logLineInvoke.setTargetObject(object); }
		return logLineInvoke;
	}
	
	public static List<LogLineInvoke> createLogLineInvokes(List<LogLineSample> logLineSamples, Map<String, Method> onTargetMethodSteps, Map<Method, Object> methodToBeans) {
		List<LogLineInvoke> logLineInvokes = new ArrayList<>();
		for(LogLineSample logLineSample : logLineSamples) {
			logLineInvokes.add(createLogLineInvoke(logLineSample, onTargetMethodSteps, methodToBeans));
		}
		return logLineInvokes;
	}
	
	public static Map<String, Method> getOnTargetMethodSteps(LoggingMethodIntercepter loggingMethodIntercepter, Map<Method, Object> methodToBeans) {
		for(Method method:methodToBeans.keySet()) {
			loggingMethodIntercepter.addTargetMethod(method, null);
		}
		Map<String, Method> onTargetMethodSteps = LoggingUnitMethodHelper.convertToStepMethods(loggingMethodIntercepter.getOnTargetMethods());
		return onTargetMethodSteps;
	}
	
	public static void invokeLogLineInvokers(LoggingMethodIntercepter loggingMethodIntercepter, Map<Method, Object> methodToBeans, LogLineInvoke logLineInvoke) throws InterruptedException, JsonProcessingException, IllegalAccessException, InvocationTargetException, IllegalArgumentException {
		Map<String, Method> onTargetMethodSteps = getOnTargetMethodSteps(loggingMethodIntercepter, methodToBeans);
		Method method = LogLineHelper.getMatchedMethod(onTargetMethodSteps, logLineInvoke.getParameterSample().getStep());
		if (method==null) { return; }
		Object object = methodToBeans.get(method);
		if (object==null) { return; }

		logLineInvoke.setMethod(method);
		logLineInvoke.setTargetObject(object);
		
		invokeSample(loggingMethodIntercepter.getLogger(), logLineInvoke);
	}
	
	public static void invokeLogLineInvokers(LoggingMethodIntercepter loggingMethodIntercepter, Map<Method, Object> methodToBeans, List<LogLineInvoke> logLineInvokes) throws InterruptedException, JsonProcessingException, IllegalAccessException, InvocationTargetException, IllegalArgumentException {
		for(LogLineInvoke logLineInvoke : logLineInvokes) {
			invokeLogLineInvokers(loggingMethodIntercepter, methodToBeans, logLineInvoke);
		}
	}
}
