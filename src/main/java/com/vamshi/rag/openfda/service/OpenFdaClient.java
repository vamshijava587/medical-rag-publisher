package com.vamshi.rag.openfda.service;

import com.vamshi.rag.common.MedicalAiRagProperties;
import com.vamshi.rag.openfda.model.FdaDrugResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OpenFdaClient {

    private final WebClient webClient;
    private final MedicalAiRagProperties properties;

    public OpenFdaClient(WebClient webClient, MedicalAiRagProperties properties) {
        this.properties = properties;
        this.webClient = webClient;
    }

    public Mono<FdaDrugResponse> fetchDrugs(int limit, int skip) {
        return webClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path(properties.openFda().api().drugNdcPath())
                                .queryParam("limit", limit)
                                .queryParam("skip", skip)
                                .build())
                .retrieve()
                .bodyToMono(FdaDrugResponse.class);
    }
}
