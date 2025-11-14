package io.bitryon.logger.spring;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import io.bitryon.logger.provider.LoggerProvider;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <pre>
 * [Recommended]
 * read the step log id from request.
 * write trace id to the response header.
 * 
 * Another way is LoggingWebMvcConfigurer/LoggingHttpHeaderReaderInterceptor and LoggingHttpHeaderWebResponseAdvice, but they may break the chain on preHandler results the logger reset failed.
 * </pre>
 */
public class LoggingHttpRequestWebFilter extends OncePerRequestFilter {

	private static final String LISTENER_ADDED =
			LoggingHttpRequestWebFilter.class.getName() + ".ASYNC_LISTENER_ADDED";
	
	/**
	 * default provider
	 */
	protected final LoggerProvider loggerProvider;
	
	/**
	 * http headers to log
	 */
	private final String[] logHttpHeaderNames;
	
	/**
	 * write http request
	 */
	private final boolean logHttpRequest;
	
	/**
	 * constructor
	 * @param loggerProvider logger provider
	 * @param logHttpRequest true write http request (URL, headers, query string, query parameters, body) to log; false or null will not skip
	 * @param logHttpHeaderNames write http header by name to log.
	 */
	public LoggingHttpRequestWebFilter(LoggerProvider loggerProvider, Boolean logHttpRequest, String... logHttpHeaderNames) {
		this.loggerProvider = loggerProvider;
		this.logHttpHeaderNames = logHttpHeaderNames == null || logHttpHeaderNames.length==0 ? null : logHttpHeaderNames;
		this.logHttpRequest = logHttpRequest!=null && logHttpRequest;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain chain)
			throws ServletException, IOException {
		LoggingHttpRequestWebReader.readHttpRequest(loggerProvider, request, logHttpRequest, logHttpHeaderNames);
		LoggingHttpResponseWebWriter.writeHeader(loggerProvider, response);
		try {
			chain.doFilter(request, response);
		} finally {
			if (request.isAsyncStarted()) {
				addCleanupOnAsyncComplete(request);
			} else {
				loggerProvider.getLogger().reset();
			}
		}
	}

	private void addCleanupOnAsyncComplete(HttpServletRequest request) {
		if (request.getAttribute(LISTENER_ADDED) != null) return;
		request.setAttribute(LISTENER_ADDED, Boolean.TRUE);
		request.getAsyncContext().addListener(new AsyncListener() {
			@Override public void onComplete(AsyncEvent event) { loggerProvider.getLogger().reset(); }
			@Override public void onTimeout(AsyncEvent event)  { loggerProvider.getLogger().reset(); }
			@Override public void onError(AsyncEvent event)	{ loggerProvider.getLogger().reset(); }
			@Override public void onStartAsync(AsyncEvent event) { }
		});
	}
	
	@Override
	protected boolean shouldNotFilterAsyncDispatch() {
		return true;
	}
	
}
