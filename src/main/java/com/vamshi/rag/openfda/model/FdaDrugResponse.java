package com.vamshi.rag.openfda.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FdaDrugResponse(
    FdaMeta meta,
    List<FdaDrugRecord> results
) {}
