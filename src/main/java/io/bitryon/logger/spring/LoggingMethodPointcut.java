/*
 * Copyright (c) 2025 [bitryon.io]
 * Licensed under the Elastic License 2.0
 * https://www.elastic.co/licensing/elastic-license
 */
package io.bitryon.logger.spring;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;

import io.bitryon.logger.annotation.LoggingUnit;
import io.bitryon.logger.provider.LoggerProvider;

/**
 * Should avoid duplication beans of it
 */
public class LoggingMethodPointcut extends AbstractPointcutAdvisor {
	
	private static final long serialVersionUID = -408807346069204771L;

	/**
	 * logger provider
	 */
	protected final LoggerProvider bitryonLoggerProvider;
	
	/**
	 * trace method interceptor
	 */
	private final LoggingTraceMethodInterceptor loggingTraceMethodInterceptor;
	
	/**
	 * get LoggingTraceMethodInterceptor, used to manually fill up more classes or methods for logging.
	 * @return LoggingTraceMethodInterceptor
	 */
	public LoggingTraceMethodInterceptor getLoggingTraceMethodInterceptor() {
		return loggingTraceMethodInterceptor;
	}

	/**
	 * constructor
	 * 
	 * @param bitryonLoggerProvider logger provider
	 */
	public LoggingMethodPointcut(LoggerProvider bitryonLoggerProvider) {
		this.bitryonLoggerProvider = bitryonLoggerProvider;
		this.loggingTraceMethodInterceptor = new LoggingTraceMethodInterceptor(bitryonLoggerProvider);
		this.setOrder(HIGHEST_PRECEDENCE);
	}
	
	private LoggingUnit getLoggingUnit(Method method) {
		LoggingUnit loggingUnit = LoggingTraceMethodInterceptor.getTargetMethods().get(method);
		if (loggingUnit == null) {
			this.loggingTraceMethodInterceptor.addTargetMethod(method, null);
			loggingUnit = LoggingTraceMethodInterceptor.getTargetMethods().get(method);
		}
		return loggingUnit;
	}
	
	/**
	 * Convenient bean to match methods that may annotated with @Logging
	 */
	private Pointcut pointcut = new DynamicMethodMatcherPointcut() {
		@Override
		public boolean matches(Method method, Class<?> targetClass, Object... args) {
			return matches(method, targetClass);
		}
		
		@Override
		public boolean matches(Method method, Class<?> targetClass) {
			if (!method.getDeclaringClass().isInterface()) {
				return false;
			}
			LoggingUnit logUnit = getLoggingUnit(method);
			if (logUnit==null) {
				return false;
			}
			return logUnit.getValid();
		}
	};
	
	@Override
	public Pointcut getPointcut() {
		return pointcut;
	}

	@Override
	public Advice getAdvice() {
		return getLoggingTraceMethodInterceptor();
	}
	
	/**
	 * to proxy direct class without @Logging
	 * 
	 * @param loggingUnit the logging conf
	 * @param beanInstance the instance to proxy
	 * @param realClass the class to proxy
	 * @return the proxy of the instance.
	 */
	public Object proxyBeanInstance(LoggingUnit loggingUnit, Object beanInstance, Class<?> realClass) {
		ProxyFactory factory = new ProxyFactory(beanInstance);
		factory.setProxyTargetClass(true);
		factory.addAdvice(getAdvice());
		if (realClass!=null) {
			getLoggingTraceMethodInterceptor().addClasses(loggingUnit, realClass);
		}else {
			getLoggingTraceMethodInterceptor().addClasses(loggingUnit, beanInstance.getClass());
		}
		return factory.getProxy();
	}

	/**
	 * to proxy direct class without @Logging
	 * 
	 * @param loggingUnit the logging conf
	 * @param beanInstance the instance to proxy
	 * @return the proxy of the instance.
	 */
	public Object proxyBeanInstance(LoggingUnit loggingUnit, Object beanInstance) {
		ProxyFactory factory = new ProxyFactory(beanInstance);
		factory.setProxyTargetClass(true);
		factory.addAdvice(getAdvice());
		getLoggingTraceMethodInterceptor().addClasses(loggingUnit, beanInstance.getClass());
		return factory.getProxy();
	}
	
	/**
	 * to proxy interface without @Logging
	 * 
	 * @param loggingUnit the logging conf
	 * @param clazz the interface to proxy
	 * @return the proxy of the interface.
	 */
	public Object proxyBeanInterface(LoggingUnit loggingUnit, Class<?> clazz) {
		ProxyFactory factory = new ProxyFactory();
		factory.setInterfaces(clazz);
		factory.addAdvice(getAdvice());
		getLoggingTraceMethodInterceptor().addClasses(loggingUnit, clazz);
		return factory.getProxy();
	}
}
