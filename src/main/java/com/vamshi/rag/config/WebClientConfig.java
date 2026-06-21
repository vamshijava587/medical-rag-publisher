package com.vamshi.rag.config;

import com.vamshi.rag.common.MedicalAiRagProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClientBuilder(MedicalAiRagProperties properties) {
        return  WebClient.builder()
                .baseUrl(properties.openFda().api().baseUrl())
                .build();
    }

}