package com.smartlogix.authservice.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {
    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    // token -> expirationMillis
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void add(String token, long expirationMillis) {
        if (token == null || token.isBlank()) return;
        blacklist.put(token, expirationMillis);
        log.debug("Token added to blacklist, expires at {} (ms)", expirationMillis);
    }

    public boolean isBlacklisted(String token) {
        if (token == null) return false;
        Long exp = blacklist.get(token);
        if (exp == null) return false;
        // if token is expired, treat as not blacklisted (cleanup will remove it) OR we can remove here
        if (exp < System.currentTimeMillis()) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    // periodic cleanup of expired entries
    @Scheduled(fixedDelayString = "60000") // every 60s
    public void cleanup() {
        long now = System.currentTimeMillis();
        int removed = 0;
        for (Map.Entry<String, Long> e : blacklist.entrySet()) {
            if (e.getValue() < now) {
                blacklist.remove(e.getKey());
                removed++;
            }
        }
        if (removed > 0) log.debug("TokenBlacklistService cleanup removed {} expired tokens", removed);
    }
}

