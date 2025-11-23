package io.bitryon.logger.spring;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplicationRunListener;

/**
 * Used to load LoggingProxyInitiation in spring META-INF/spring.factories before any classes loaded. Or can do it in main method. See bitryon-logger/README.md and bitryon-logging-java-spring-example
 */
public class LoggingProxyInitiationSpringApplicationRunListener implements SpringApplicationRunListener {
	
	/**
	 * constructor
	 */
	public LoggingProxyInitiationSpringApplicationRunListener() {}
	
	public void starting(ConfigurableBootstrapContext bootstrapContext) {
		io.bitryon.logger.boostrap.LoggingProxyInitiation.premain(null);
	}
}
