package com.documind.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.util.Map;

@Configuration
public class RateLimitConfig {
    
    @Bean
    public Map<String, LocalDateTime> rateLimitCache() {
        return new ConcurrentHashMap<>();
    }
}