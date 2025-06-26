package com.se.EdgeHire.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://127.0.0.1:8081")
                .defaultHeader("Content-Type", "application.yml/json")
                .build();
    }
}
