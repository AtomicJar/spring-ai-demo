package com.example.demogenai;

import org.springframework.boot.actuate.autoconfigure.logging.opentelemetry.otlp.OtlpLoggingConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.testcontainers.grafana.LgtmStackContainer;

class OpenTelemetryLoggingContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<LgtmStackContainer, OtlpLoggingConnectionDetails> {

	OpenTelemetryLoggingContainerConnectionDetailsFactory() {
		super(ANY_CONNECTION_NAME, "org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpAutoConfiguration");
	}

	@Override
	protected OtlpLoggingConnectionDetails getContainerConnectionDetails(
			ContainerConnectionSource<LgtmStackContainer> source) {
		return new OpenTelemetryLoggingContainerConnectionDetails(source);
	}

	private static final class OpenTelemetryLoggingContainerConnectionDetails
			extends ContainerConnectionDetails<LgtmStackContainer> implements OtlpLoggingConnectionDetails {

		private OpenTelemetryLoggingContainerConnectionDetails(ContainerConnectionSource<LgtmStackContainer> source) {
			super(source);
		}

		@Override
		public String getUrl() {
			return "http://%s:%d/v1/logs".formatted(getContainer().getHost(), getContainer().getMappedPort(4318));
		}

	}

}
