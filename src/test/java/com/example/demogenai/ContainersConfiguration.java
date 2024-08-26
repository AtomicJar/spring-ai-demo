package com.example.demogenai;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.qdrant.QdrantContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfiguration {

	@Bean
	@ServiceConnection
	@RestartScope
	OllamaContainer ollama() {
		return new OllamaContainer(
				DockerImageName.parse("ghcr.io/thomasvitale/ollama-llama3:sha-747708c338babbb52c67e913928000ce8f3ed6ec")
					.asCompatibleSubstituteFor("ollama/ollama"));
	}

	@Bean
	@ServiceConnection
	@RestartScope
	QdrantContainer qdrant() {
		return new QdrantContainer("qdrant/qdrant:v1.9.0");
	}

}