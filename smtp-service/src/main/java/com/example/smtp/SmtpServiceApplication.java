package com.example.smtp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = { "com.example.common.repository", "com.example.smtp" })
@EntityScan(basePackages = { "com.example.common.entity", "com.example.smtp" })
@EnableScheduling
public class SmtpServiceApplication {

        public static void main(String[] args) {
                System.setProperty("javax.net.ssl.keyStore",
                                "d:/WORKSPACE/smtp-service/smtp-service/src/main/resources/keystore.jks");
                System.setProperty("javax.net.ssl.keyStorePassword", "password");
                SpringApplication.run(SmtpServiceApplication.class, args);
        }

}
