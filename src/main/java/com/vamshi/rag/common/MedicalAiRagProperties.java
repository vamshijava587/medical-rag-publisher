package com.vamshi.rag.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "medicalai")
public record MedicalAiRagProperties(OpenFda openFda){
    public record OpenFda(Api api, Ingestion ingestion) {}
    public record Api(String baseUrl, String drugNdcPath) {}
    public record Ingestion(boolean enabled, int limit, int skip) {}
}
