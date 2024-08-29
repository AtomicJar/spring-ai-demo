package com.example.demogenai;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class Config {

	@Bean
	ApplicationListener<ApplicationReadyEvent> logbackOtelAppenderInitializer(OpenTelemetry openTelemetry) {
		return event -> OpenTelemetryAppender.install(openTelemetry);
	}

}
