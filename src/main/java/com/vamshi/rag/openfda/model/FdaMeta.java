package com.vamshi.rag.openfda.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FdaMeta(
    String disclaimer,
    String terms,
    String license,
    @JsonProperty("last_updated") String lastUpdated,
    Results results
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Results(int skip, int limit, int total) {}
}
