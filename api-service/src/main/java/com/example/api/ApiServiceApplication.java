package com.example.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = { "com.example.common.entity",
        "com.example.api" })
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = { "com.example.common.repository",
        "com.example.api" })
public class ApiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiServiceApplication.class, args);
    }

}
