package com.example.demogenai;

import org.springframework.boot.SpringApplication;

public class TestDemoGenaiApplication {

	public static void main(String[] args) {
		SpringApplication.from(DemoGenaiApplication::main)
			.with(ContainersConfiguration.class, GrafanaContainerConfiguration.class, IngestionConfiguration.class)
			.run(args);
	}

}
