/*
 * Copyright (c) 2025 [bitryon.io]
 * Licensed under the Elastic License 2.0
 * https://www.elastic.co/licensing/elastic-license
 */
package io.bitryon.logger.spring;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplicationRunListener;

/**
 * Used to load LoggingInitiation in spring META-INF/spring.factories before any classes loaded. Or can do it in main method. See bitryon-logger/README.md and bitryon-logging-integration-java-spring-example
 */
public class LoggingInitiationSpringApplicationRunListener implements SpringApplicationRunListener {
	
	/**
	 * constructor
	 */
	public LoggingInitiationSpringApplicationRunListener() {}
	
	public void starting(ConfigurableBootstrapContext bootstrapContext) {
		io.bitryon.logger.boostrap.LoggingInitiation.premain(null);
	}
}
