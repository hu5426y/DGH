package com.example.dgh.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenStore {
    private final Map<String, String> tokenToUserId = new ConcurrentHashMap<>();

    public String issueToken(String userId) {
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, userId);
        return token;
    }

    public String getUserId(String token) {
        return tokenToUserId.get(token);
    }
}
