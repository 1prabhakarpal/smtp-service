package com.example.smtp.config;

import com.example.common.entity.User;
import com.example.common.repository.UserRepository;
import com.example.smtp.handler.MailHandlerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.auth.EasyAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.server.SMTPServer;

@Configuration
public class SmtpServerConfig {

    @Value("${smtp.port:25000}")
    private int port;

    @Value("${smtp.submission.port:58700}")
    private int submissionPort;

    @Bean
    public AuthenticationHandlerFactory authenticationHandlerFactory(UserRepository userRepository) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return new EasyAuthenticationHandlerFactory(new UsernamePasswordValidator() {
            @Override
            public void login(String username, String password, MessageContext context) throws LoginFailedException {
                if (username == null || password == null) {
                    throw new LoginFailedException("Username or password cannot be null");
                }
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new LoginFailedException("User not found"));

                if (!passwordEncoder.matches(password, user.getPassword())) {
                    throw new LoginFailedException("Invalid password");
                }
            }
        });
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public SMTPServer smtpServerInbound(MailHandlerFactory mailHandlerFactory) {
        return SMTPServer.port(port)
                .messageHandlerFactory(mailHandlerFactory)
                .enableTLS(true)
                .requireTLS(false) // Opportunistic TLS
                .build();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public SMTPServer smtpServerSubmission(MailHandlerFactory mailHandlerFactory,
            AuthenticationHandlerFactory authHandlerFactory) {
        return SMTPServer.port(submissionPort)
                .messageHandlerFactory(mailHandlerFactory)
                .authenticationHandlerFactory(authHandlerFactory)
                .enableTLS(true)
                .requireTLS(false) // Clients can choose
                .requireAuth(true)
                .build();
    }
}
