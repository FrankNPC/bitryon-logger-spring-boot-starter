
![Java](https://img.shields.io/badge/Java-9+-orange?logo=java)
![Maven Central](https://img.shields.io/badge/Maven%20Central-available-blue?logo=apachemaven)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-brightgreen?logo=springboot)


# bitryon-logger-spring-boot-starter

 - 1, introduce the jar, annotate beans with [@Logging](https://github.com/FrankNPC/bitryon-logger/blob/master/src/main/java/io/bitryon/logger/annotation/Logging.java)

```
		<dependency>
			<groupId>io.bitryon</groupId>
			<artifactId>bitryon-logger-spring-boot-starter</artifactId>
			<version>1.0-SNAPSHOT</version> <!--  new version https://repo1.maven.org/maven2/io/bitryon/bitryon-logger-spring-boot-starter -->
		</dependency>
```


 - 2, configuration. see the explains in src/*/resource/application.xml, configure logger and app-node.
    -  import AutoConfigurationBitryonLogger.class to declare default Logging.
    -  If there is no configs from spring, it will go to LoggerFactory and bitryon_logger.properties to get the LoggerProvider as fallback

 - 3, configure http client by adding LoggingHttpClientHeaderWriterInterceptor to write header HTTP_HEADER_STEP_LOG_ID(X-Step-Log-Id), so the next app/service/web-server can pick it up. 
See [UserServiceSubscriber](https://github.com/FrankNPC/bitryon-logging-examples/blob/master/bitryon-logging-java-spring-example/src/main/java/io/bitryon/example/web/config/UserServiceSubscriber.java)

 - 4, configure web server by adding FilterRegistrationBean< LoggingHttpRequestWebFilter > to pick up header HTTP_HEADER_STEP_LOG_ID(X-Step-Log-Id) from the http request. 
See [ExampleWebServerConfiguration](https://github.com/FrankNPC/bitryon-logging-examples/blob/master/bitryon-logging-java-spring-example/src/main/java/io/bitryon/example/web/config/ExampleWebServerConfiguration.java)

 
### see example [bitryon-logging-java-spring-example](https://github.com/FrankNPC/bitryon-logging-examples/tree/master/bitryon-logging-java-spring-example) 
