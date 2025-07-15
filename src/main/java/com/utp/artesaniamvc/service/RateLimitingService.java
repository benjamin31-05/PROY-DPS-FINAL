package com.utp.artesaniamvc.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class RateLimitingService {
    
    private final Map<String, List<Long>> attemptsByIp = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long TIME_WINDOW_MS = 3600000; // 1 hora
    
    public boolean isAllowed(String ipAddress) {
        long currentTime = System.currentTimeMillis();
        
        attemptsByIp.compute(ipAddress, (ip, attempts) -> {
            if (attempts == null) {
                attempts = new ArrayList<>();
            }
            
            // Limpiar intentos antiguos
            attempts.removeIf(timestamp -> currentTime - timestamp > TIME_WINDOW_MS);
            
            return attempts;
        });
        
        List<Long> attempts = attemptsByIp.get(ipAddress);
        return attempts.size() < MAX_ATTEMPTS;
    }
    
    public void recordAttempt(String ipAddress) {
        long currentTime = System.currentTimeMillis();
        attemptsByIp.compute(ipAddress, (ip, attempts) -> {
            if (attempts == null) {
                attempts = new ArrayList<>();
            }
            attempts.add(currentTime);
            return attempts;
        });
    }
    
    public void clearAttempts(String ipAddress) {
        attemptsByIp.remove(ipAddress);
    }
}