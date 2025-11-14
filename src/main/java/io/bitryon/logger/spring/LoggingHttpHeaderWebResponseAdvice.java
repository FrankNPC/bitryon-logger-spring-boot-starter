/*
 * Copyright (c) 2025 [bitryon.io]
 * Licensed under the Elastic License 2.0
 * https://www.elastic.co/licensing/elastic-license
 */
package io.bitryon.logger.spring;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import io.bitryon.logger.PreDefinition;
import io.bitryon.logger.provider.LoggerProvider;
import jakarta.annotation.Resource;

/**
 * [Not recommended]
 * write trace id to the http response. 
 * Manually import it needed.
 */
@ControllerAdvice
public class LoggingHttpHeaderWebResponseAdvice implements ResponseBodyAdvice<Object> {
	
	/**
	 * constructor
	 */
	public LoggingHttpHeaderWebResponseAdvice() {}

	@Resource
	LoggerProvider bitryonLoggerProvider;
	
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return bitryonLoggerProvider.getAppNodeConfiguration().getHttpHeaderIdType() != PreDefinition.HTTP_HEADER_ID_TYPE_NONE;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
								  Class<? extends HttpMessageConverter<?>> selectedConverterType,
								  ServerHttpRequest request, ServerHttpResponse response) {
		LoggingHttpResponseWebWriter.writeHeader(bitryonLoggerProvider, response);
		return body;
	}
	
}