package com.vamshi.rag.openfda.service;

import com.vamshi.rag.openfda.model.FdaDrugEventResponse;
import com.vamshi.rag.openfda.model.FdaDrugLabelResponse;
import com.vamshi.rag.openfda.model.FdaDrugResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OpenFdaClient {

    private final WebClient webClient;

    public OpenFdaClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<FdaDrugResponse> fetchDrugs(int limit, int skip) {
        return webClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/drug/ndc.json")
                                .queryParam("limit", limit)
                                .queryParam("skip", skip)
                                .build())
                .retrieve()
                .bodyToMono(FdaDrugResponse.class);
    }

    public Mono<FdaDrugLabelResponse> fetchDrugLabel(String brandName) {
        return webClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/drug/label.json")
                                .queryParam("search", "openfda.brand_name:\"" + brandName + "\"")
                                .queryParam("limit", 1)
                                .build())
                .retrieve()
                .bodyToMono(FdaDrugLabelResponse.class);
    }

    // -------------------------------------
    // Drug Event API
    // Adverse Events
    // Patient Reactions
    // -------------------------------------
    public Mono<FdaDrugEventResponse> fetchDrugEvents(
            String drugName,
            int limit) {
        return webClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/drug/event.json")
                                .queryParam(
                                        "search",
                                        "patient.drug.medicinalproduct:\"" + drugName + "\"")
                                .queryParam(
                                        "limit",
                                        limit)
                                .build())
                .retrieve()
                .bodyToMono(FdaDrugEventResponse.class);
    }
}
