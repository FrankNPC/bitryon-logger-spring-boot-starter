/*
 * Copyright (c) 2025 [bitryon.io]
 * Licensed under the Elastic License 2.0
 * https://www.elastic.co/licensing/elastic-license
 */
package io.bitryon.logger.spring;

import io.bitryon.logger.model.LoggerConfiguration;
import io.bitryon.logger.Logger;
import io.bitryon.logger.boostrap.LoggingMethodIntercepter;
import io.bitryon.logger.model.AppNodeConfiguration;
import io.bitryon.logger.model.GitHubDeployment;
import io.bitryon.logger.model.LocalConfiguration;
import io.bitryon.logger.provider.LoggerFactory;
import io.bitryon.logger.provider.LoggerProvider;
import io.bitryon.logger.provider.io.LogDispatcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto load all configures for the provider and bitryon logger
 */
@Configuration
@ConfigurationProperties(prefix = "bitryon")
@ConditionalOnClass(AutoConfigurationBitryonLogger.class)
public class AutoConfigurationBitryonLogger {
	
	/**
	 * constructor
	 */
	public AutoConfigurationBitryonLogger() {}
	
	private LoggerConfiguration logger;

	private AppNodeConfiguration appNode;
	
	private LocalConfiguration local;

	private GitHubDeployment github;
	
	private Map<String, Object> bootInfo;
	
	/**
	 * set boot info
	 * @param bootInfo the boot info
	 */
	public void setBootInfo(Map<String, Object> bootInfo) {
		this.bootInfo = bootInfo;
	}

	/**
	 * set LoggerConfiguration 
	 * @param loggerConfiguration loggerConfiguration
	 */
	public void setLogger(LoggerConfiguration loggerConfiguration) {
		this.logger = loggerConfiguration;
	}

	/**
	 * set AppNodeConfiguration 
	 * @param appNodeConfiguration appNodeConfiguration
	 */
	public void setAppNode(AppNodeConfiguration appNodeConfiguration) {
		this.appNode = appNodeConfiguration;
	}

	/**
	 * set GitHubDeployment 
	 * @param github github
	 */
	public void setGithub(GitHubDeployment github) {
		this.github = github;
	}

	/**
	 * set LocalConfiguration 
	 * @param localConfiguration localConfiguration
	 */
	public void setLocal(LocalConfiguration localConfiguration) {
		this.local = localConfiguration;
	}
	
	/**
	 * get LoggerConfiguration
	 * @return LoggerConfiguration
	 */
	public LoggerConfiguration getLoggerConfiguration() {
		return logger;
	}

	/**
	 * get AppNodeConfiguration
	 * @return AppNodeConfiguration
	 */
	public AppNodeConfiguration getAppNodeConfiguration() {
		return appNode;
	}

	/**
	 * get LocalConfiguration
	 * @return LocalConfiguration
	 */
	public LocalConfiguration getLocalConfiguration() {
		return local;
	}

	/**
	 * get github deployment info
	 * 
	 * @return the github deployment info
	 */
	public GitHubDeployment getGithub() {
		return github;
	}
	
	/**
	 * get extraInfo
	 * @return the extrainfo
	 */
	public Map<String, Object> getBootInfo() {
		return bootInfo = bootInfo==null ? new HashMap<>() : bootInfo;
	}

	/**
	 * get Logger
	 * @return Logger
	 */
	@Bean
	Logger getDefaultLogger(LoggerProvider bitryonLoggerProvider) {
		return bitryonLoggerProvider.getLogger();
	}
	
	/**
	 * get LoggingMethodPointcut 
	 * @return LoggingMethodPointcut
	 */
	@Bean
	public LoggingMethodPointcut getLoggingMethodPointcut(LoggerProvider bitryonLoggerProvider) {
		return new LoggingMethodPointcut(bitryonLoggerProvider);
	}
	
	/**
	 * get LoggingMethodIntercepter
	 * @return LoggingMethodIntercepter
	 */
	@Bean
	public LoggingMethodIntercepter getLoggerMethodIntercepter(LoggingMethodPointcut loggingMethodPointcut) {
		return loggingMethodPointcut.getLoggingTraceMethodInterceptor();
	}
	
	/**
	 * <pre>
	 * get and load the bean for LoggerProvider
	 * If there is no configs from spring, it will go to LoggerFactory and bitryon_logger.properties to get the LoggerProvider as fallback
	 * </pre>
	 * 
	 * @param logDispatcher optional, if this is by spring's conf, you can wire it or leave it null.
	 * @return LoggerProvider
	 * @throws IOException file errors
	 */
	@Bean
	public LoggerProvider getProvider(@Autowired(required = false) LogDispatcher logDispatcher) throws IOException {
		AppNodeConfiguration theAppNodeConfiguration = getAppNodeConfiguration();
		LoggerConfiguration theLoggerConfiguration = getLoggerConfiguration();
		LocalConfiguration theLocalConfiguration = getLocalConfiguration();
		GitHubDeployment theGitHubDeployment = getGithub();
		
		LoggerProvider bitryonLoggerProvider = null;
		if (theAppNodeConfiguration==null 
				&& theLoggerConfiguration==null
				&& theLocalConfiguration==null
				&& theGitHubDeployment==null) {
			bitryonLoggerProvider = LoggerFactory.getLoggerProvider();
		}else {
			bitryonLoggerProvider = new LoggerProvider(theLoggerConfiguration, theAppNodeConfiguration, theLocalConfiguration, getBootInfo(), theGitHubDeployment, null, logDispatcher);
			LoggerFactory.setLoggerProvider(bitryonLoggerProvider);
		}
		return bitryonLoggerProvider;
	}


}