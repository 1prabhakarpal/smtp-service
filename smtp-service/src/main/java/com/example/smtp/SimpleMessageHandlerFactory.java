package com.example.smtp;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.springframework.stereotype.Component;

@Component
public class SimpleMessageHandlerFactory implements MessageHandlerFactory {

    private final com.example.common.repository.EmailRepository emailRepository;

    public SimpleMessageHandlerFactory(com.example.common.repository.EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    @Override
    public MessageHandler create(MessageContext context) {
        return new SimpleMessageHandler(context, emailRepository);
    }
}
