/*
 * Copyright (c) 2025 [bitryon.io]
 * Licensed under the Elastic License 2.0
 * https://www.elastic.co/licensing/elastic-license
 */
package io.bitryon.logger.spring;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import io.bitryon.logger.boostrap.LoggingMethodIntercepter;
import io.bitryon.logger.provider.LoggerProvider;

/**
 * Wrapper of LoggingMethodIntercepter
 */
public class LoggingTraceMethodInterceptor extends LoggingMethodIntercepter implements MethodInterceptor {
	
	/**
	 * constructor
	 * @param bitryonLoggerProvider logger provider
	 */
	public LoggingTraceMethodInterceptor(LoggerProvider bitryonLoggerProvider) {
		super(bitryonLoggerProvider);
	}
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		return invoke(new MethodInvoker<>() {
			@Override
			public Object invoke() throws Throwable {
				return invocation.proceed();
			}

			@Override
			public Method getMethod() {
				return invocation.getMethod();
			}

			@Override
			public Object[] getArguments() {
				return invocation.getArguments();
			}
		});
	}

}
