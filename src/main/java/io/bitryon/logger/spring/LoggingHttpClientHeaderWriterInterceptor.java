package io.bitryon.logger.spring;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import io.bitryon.logger.Logger;
import io.bitryon.logger.PreDefinition;
import io.bitryon.logger.provider.LoggerProvider;

/**
 * Used to write trace id or step log id to the client such as browsers for debug purpose if needed.
 */
public class LoggingHttpClientHeaderWriterInterceptor implements ClientHttpRequestInterceptor {
	
	/**
	 * default provider
	 */
	protected final LoggerProvider loggerProvider;

	/**
	 * constructor
	 * @param loggerProvider logger provider
	 */
	public LoggingHttpClientHeaderWriterInterceptor(LoggerProvider loggerProvider) {
		this.loggerProvider = loggerProvider;
	}
	
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		Logger logger = loggerProvider.getLogger();
		request.getHeaders().set(PreDefinition.HTTP_HEADER_STEP_LOG_ID, logger.getStepLogId());
		if (logger.getSkip()) {
			request.getHeaders().set(PreDefinition.HTTP_HEADER_STEP_LOG_SKIP, Boolean.TRUE.toString());
		}
		return execution.execute(request, body);
	}
}