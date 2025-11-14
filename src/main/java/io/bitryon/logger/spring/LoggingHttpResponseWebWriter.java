package io.bitryon.logger.spring;


import org.springframework.http.server.ServerHttpResponse;

import io.bitryon.logger.PreDefinition;
import io.bitryon.logger.provider.LoggerProvider;
import jakarta.servlet.http.HttpServletResponse;

public final class LoggingHttpResponseWebWriter {
	
	/**
	 * write trace id or step log id
	 * @param response used to write header to the response before it writes anything.
	 */
	public static void writeHeader(LoggerProvider bitryonLoggerProvider, ServerHttpResponse response) {
		switch(bitryonLoggerProvider.getAppNodeConfiguration().getHttpHeaderIdType()){
		case PreDefinition.HTTP_HEADER_ID_TYPE_X_STEP_TRACE_Id: 
			response.getHeaders().add(PreDefinition.HTTP_HEADER_STEP_TRACE_ID, bitryonLoggerProvider.getLogger().getTraceId());
			break;
		case PreDefinition.HTTP_HEADER_ID_TYPE_X_STEP_LOG_ID: 
			response.getHeaders().add(PreDefinition.HTTP_HEADER_STEP_LOG_ID, bitryonLoggerProvider.getLogger().getStepLogId());
			break;
		}
	}

	/**
	 * write trace id or step log id
	 * @param response used to write header to the response before it writes anything.
	 */
	public static void writeHeader(LoggerProvider bitryonLoggerProvider, HttpServletResponse response) {
		switch(bitryonLoggerProvider.getAppNodeConfiguration().getHttpHeaderIdType()){
		case PreDefinition.HTTP_HEADER_ID_TYPE_X_STEP_TRACE_Id: 
			response.addHeader(PreDefinition.HTTP_HEADER_STEP_TRACE_ID, bitryonLoggerProvider.getLogger().getTraceId());
			break;
		case PreDefinition.HTTP_HEADER_ID_TYPE_X_STEP_LOG_ID: 
			response.addHeader(PreDefinition.HTTP_HEADER_STEP_LOG_ID, bitryonLoggerProvider.getLogger().getStepLogId());
			break;
		}
	}
}