package com.example.demogenai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestDemoGenaiApplication {

    public static void main(String[] args) {
        SpringApplication.from(DemoGenaiApplication::main).with(TestDemoGenaiApplication.class).run(args);
    }
}
