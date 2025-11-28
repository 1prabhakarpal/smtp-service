package com.example.smtp.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.RejectException;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class RateLimiter {

    private final ConcurrentHashMap<String, Queue<Instant>> connectionsPerIp = new ConcurrentHashMap<>();
    private static final int MAX_CONNECTIONS_PER_MINUTE = 20;

    public void onConnect(InetAddress clientAddress) throws RejectException {
        if (clientAddress == null)
            return;

        String ip = clientAddress.getHostAddress();
        Queue<Instant> timestamps = connectionsPerIp.computeIfAbsent(ip, k -> new ConcurrentLinkedQueue<>());

        Instant now = Instant.now();
        timestamps.add(now);

        // Remove old timestamps
        Instant oneMinuteAgo = now.minusSeconds(60);
        Iterator<Instant> iterator = timestamps.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isBefore(oneMinuteAgo)) {
                iterator.remove();
            } else {
                break; // Queue is sorted by time
            }
        }

        log.info("Checking rate limit for IP: {}. Current count: {}", ip, timestamps.size());

        if (timestamps.size() > MAX_CONNECTIONS_PER_MINUTE) {
            log.warn("Rate limit exceeded for IP: {} ({} connections/min)", ip, timestamps.size());
            throw new RejectException(421, "Too many connections from your IP. Please try again later.");
        }
        log.debug("Connection from {} (Recent: {})", ip, timestamps.size());
    }
}
