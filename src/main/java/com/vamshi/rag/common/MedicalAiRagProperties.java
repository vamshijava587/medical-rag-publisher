package com.vamshi.rag.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "medicalai")
public record MedicalAiRagProperties(OpenFda openFda,Vector vector){
    public record OpenFda(Api api, Ingestion ingestion) {}
    public record Api(String baseUrl) {}
    public record Ingestion(boolean enabled, int limit, int skip) {}
    public record Vector(boolean publish) {}
}
