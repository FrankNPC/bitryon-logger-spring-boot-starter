package io.bitryon.logger.spring;

import java.util.List;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.bitryon.logger.provider.LoggerProvider;

/**
 * [Not recommended]
 * 
 * If printing http URL, headers and body etc. needed.
 * recommend LoggingHttpOncePerRequestFilter.
 */
public class LoggingWebMvcConfigurer implements WebMvcConfigurer {

	private final LoggerProvider bitryonLoggerProvider;
	
	// default header names to print
	private final String[] printHeaderNames;
	
	// default paths
	private final String[] paths;
	
	// print http request including url body and headers.
	private final boolean printHttpRequest;

	/**
	 * constructor
	 * @param printHeaderNames headers to print, or null not to print
	 * @param printHttpRequest true printing http request; false or null will not print
	 * @param paths http paths to intercept for the logging
	 */
	public LoggingWebMvcConfigurer(LoggerProvider bitryonLoggerProvider, Boolean printHttpRequest, List<String> printHeaderNames, String... paths) {
		this.bitryonLoggerProvider = bitryonLoggerProvider;
		this.printHeaderNames = printHeaderNames==null?new String[0]:printHeaderNames.stream().toArray(String[]::new);
		this.printHttpRequest = printHttpRequest!=null && printHttpRequest;
		this.paths = paths == null? new String[0]:paths;
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new LoggingHttpHeaderReaderInterceptor(bitryonLoggerProvider, printHttpRequest, printHeaderNames))
				.addPathPatterns(paths);
	}
	
}
