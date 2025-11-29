package com.example.smtp.handler;

import com.example.common.repository.EmailRepository;
import com.example.smtp.util.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

@Component
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class MailHandlerFactory implements MessageHandlerFactory {

    private final EmailRepository emailRepository;
    private final com.example.common.repository.MailQueueRepository mailQueueRepository;
    private final RateLimiter rateLimiter;

    @Override
    public MessageHandler create(MessageContext context) {
        log.info("create called. RemoteAddress: {}, Type: {}", context.getRemoteAddress(),
                context.getRemoteAddress() != null ? context.getRemoteAddress().getClass().getName() : "null");
        if (context.getRemoteAddress() instanceof InetSocketAddress) {
            try {
                String ip = ((InetSocketAddress) context.getRemoteAddress()).getAddress().getHostAddress();
                log.info("New connection from: {}", ip);
                rateLimiter.onConnect(((InetSocketAddress) context.getRemoteAddress()).getAddress());
            } catch (RejectException e) {
                log.warn("Connection rejected: {}", e.getMessage());
                return new RejectingMessageHandler(e.getMessage());
            }
        }
        return new MailHandler(context, emailRepository, mailQueueRepository);
    }

    private static class RejectingMessageHandler implements MessageHandler {
        private final String message;

        public RejectingMessageHandler(String message) {
            this.message = message;
        }

        @Override
        public void from(String from) throws RejectException {
            throw new RejectException(421, message);
        }

        @Override
        public void recipient(String recipient) throws RejectException {
            throw new RejectException(421, message);
        }

        @Override
        public String data(InputStream data) throws RejectException, IOException {
            throw new RejectException(421, message);
        }

        @Override
        public void done() {
        }
    }
}
