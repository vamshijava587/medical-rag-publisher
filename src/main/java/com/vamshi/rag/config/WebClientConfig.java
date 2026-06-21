package com.vamshi.rag.config;

import com.vamshi.rag.common.MedicalAiRagProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClientBuilder(MedicalAiRagProperties properties) {
        final int size = 16 * 1024 * 1024; // 16 MB
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();

        return WebClient.builder()
                .baseUrl(properties.openFda().api().baseUrl())
                .exchangeStrategies(strategies)
                .build();
    }
}