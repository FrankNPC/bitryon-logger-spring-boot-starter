/*
 * Copyright (c) 2025 [bitryon.io]
 * Licensed under the Elastic License 2.0
 * https://www.elastic.co/licensing/elastic-license
 */
package io.bitryon.logger.spring;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import io.bitryon.logger.provider.LoggerProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * [Not recommended]
 * 
 * read the step log id from request.
 */
public class LoggingHttpHeaderReaderInterceptor implements HandlerInterceptor {

	/**
	 * default provider
	 */
	protected final LoggerProvider loggerProvider;
	
	/**
	 * default headers to print
	 */
	private final String[] printHeaderNames;
	
	/**
	 * print http request
	 */
	private final boolean printHttpRequest;
	
	/**
	 * constructor
	 * @param loggerProvider logger provider
	 * @param printHttpRequest true printing http request; false or null will not print
	 * @param printHeaderNames if null no print http URL, headers, query string and query parameters(body?).
	 */
	public LoggingHttpHeaderReaderInterceptor(LoggerProvider loggerProvider, Boolean printHttpRequest, String... printHeaderNames) {
		this.loggerProvider = loggerProvider;
		this.printHeaderNames = printHeaderNames;
		this.printHttpRequest = printHttpRequest!=null && printHttpRequest;
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		LoggingHttpRequestWebReader.readHttpRequest(loggerProvider, request, printHttpRequest, printHeaderNames);
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable Exception ex) throws Exception {
		loggerProvider.getLogger().reset();
	}
}
