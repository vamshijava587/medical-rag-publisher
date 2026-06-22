package com.vamshi.rag.service;

import com.vamshi.rag.common.MedicalAiRagProperties;
import com.vamshi.rag.model.DrugDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VectorPipelineService {

    private static final Logger log = LoggerFactory.getLogger(VectorPipelineService.class);

    private final MedicalAiRagProperties properties;
    private final DrugDocumentTransformer drugDocumentTransformer;
    private final VectorPublishService vectorPublishService; // Inject VectorPublishService

    @Autowired
    public VectorPipelineService(MedicalAiRagProperties properties,
                                 DrugDocumentTransformer drugDocumentTransformer,
                                 VectorPublishService vectorPublishService) {
        this.properties = properties;
        this.drugDocumentTransformer = drugDocumentTransformer;
        this.vectorPublishService = vectorPublishService;
    }

    public Mono<Void> publishVectorPipeline(List<DrugDocument> drugDocuments) {
        if (!properties.vector().publish()) {
            log.info("Vector pipeline publishing is disabled. Skipping.");
            return Mono.empty();
        }

        log.info("Starting vector pipeline publishing process...");

        List<Document> documentsToStore = drugDocuments.stream()
                .map(drugDocumentTransformer::transform)
                .collect(Collectors.toList());

        return vectorPublishService.publishDocuments(documentsToStore)
                .doOnSuccess(unused -> log.info("Vector pipeline publishing process completed."))
                .onErrorResume(e -> {
                    log.error("Error during vector pipeline publishing: {}", e.getMessage(), e);
                    return Mono.empty();
                });
    }
}