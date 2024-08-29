package com.example.demogenai;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.grafana.LgtmStackContainer;

@TestConfiguration(proxyBeanMethods = false)
public class GrafanaContainerConfiguration {

	@Bean
	@ServiceConnection
	@RestartScope
	LgtmStackContainer lgtmContainer() {
		return new LgtmStackContainer("grafana/otel-lgtm:0.7.1");
	}

}
