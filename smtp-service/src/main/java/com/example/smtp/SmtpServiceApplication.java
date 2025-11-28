package com.example.smtp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = { "com.example.common.entity",
        "com.example.smtp" })
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = { "com.example.common.repository",
        "com.example.smtp" })
public class SmtpServiceApplication {

    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.keyStore",
                "d:/WORKSPACE/smtp-service/smtp-service/src/main/resources/keystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        SpringApplication.run(SmtpServiceApplication.class, args);
    }

    @org.springframework.context.annotation.Bean(initMethod = "start", destroyMethod = "stop")
    public org.subethamail.smtp.server.SMTPServer smtpServer(SimpleMessageHandlerFactory messageHandlerFactory) {
        return org.subethamail.smtp.server.SMTPServer.port(25000)
                .messageHandlerFactory(messageHandlerFactory)
                .requireTLS(true)
                .build();
    }

}
