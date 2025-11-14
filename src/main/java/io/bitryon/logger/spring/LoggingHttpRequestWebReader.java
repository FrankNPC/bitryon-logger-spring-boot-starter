package io.bitryon.logger.spring;


import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Collectors;

import io.bitryon.logger.InternalLogger;
import io.bitryon.logger.PreDefinition;
import io.bitryon.logger.provider.LoggerProvider;
import jakarta.servlet.http.HttpServletRequest;

public final class LoggingHttpRequestWebReader {
	
	public static void readHttpRequest(LoggerProvider bitryonLoggerProvider, HttpServletRequest request, boolean logHttpRequest, String... logHttpHeaderNames) {
		InternalLogger logger = bitryonLoggerProvider.getInternalLogger();

		String stepLogId = request.getHeader(PreDefinition.HTTP_HEADER_STEP_LOG_ID);
		logger.setStepLogId(stepLogId);
		
		if (logHttpRequest) {
			Map<String, Object> headerValues = logHttpHeaderNames == null ? Collections.emptyMap() : 
					Arrays.stream(logHttpHeaderNames).filter(header->{
						Enumeration<String> enumHeader = request.getHeaders(header);
							return enumHeader!=null && enumHeader.hasMoreElements();
						})
						.collect(Collectors.toMap(key -> key, key -> Collections.list(request.getHeaders(key))));
			
			logger.snap(null, PreDefinition.TYPE.HTTP, null, null,
				Map.of(PreDefinition.HTTP_RemoteHost, request.getRemoteHost() + ":" + request.getRemotePort()),
				Map.of(PreDefinition.HTTP_Method, request.getMethod()),
				Map.of(PreDefinition.HTTP_RequestURL, request.getRequestURL().toString()),
				Map.of(PreDefinition.HTTP_Headers, headerValues),
				Map.of(PreDefinition.HTTP_Body, request.getParameterMap()));
		}

		String skip = request.getHeader(PreDefinition.HTTP_HEADER_STEP_LOG_SKIP);
		logger.skip(skip!=null && "true".equalsIgnoreCase(skip));
	}

}