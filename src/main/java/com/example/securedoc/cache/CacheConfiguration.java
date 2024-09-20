package com.example.securedoc.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfiguration {

    @Bean
    public CacheStore<String, Integer> userLoginCache() {
        return new CacheStore<>(600, TimeUnit.SECONDS);
    }
}
