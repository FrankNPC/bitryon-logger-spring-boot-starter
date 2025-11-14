//package io.bitryon.logger.spring;
//
//
//import java.util.List;
//
//import org.junit.jupiter.api.Test;
//
//import io.bitryon.logger.boostrap.LoggingMethodIntercepter;
//import io.bitryon.logger.model.LogLineSample;
//import io.bitryon.logger.provider.LoggerProvider;
//import jakarta.annotation.Resource;
//
//
////@ExtendWith({SpringExtension.class})
////@SpringBootTest(classes = BitryonSearchServiceBootApplication.class)
//public final class LogLineSampleHelperTest {
//
//	@Resource 
//	AutoConfigurationBitryonLogger autoConfigurationBitryonLogger;
//
//	@Resource 
//	LoggerProvider provider;
//
//	@Resource 
//	LoggingMethodIntercepter loggingMethodIntercepter;
//	
//	@Test
//	public void test_Method_Remote_Payload() throws Exception {
//		// fill up the objects
//		List<LogLineSample> samples = LogLineSampleHelper.pullAllSamples(autoConfigurationBitryonLogger.getLocalConfiguration().getUrlLogSample(), loggerAutoConfiguration.getLocalConfiguration().getAppKey(), 
//				10000);
//		LogLineSampleHelper.invokeAllMethodsByLogSamples(
//				loggingMethodIntercepter, null, samples);
//	}
//	
//}
