package com.vamshi.rag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
public class VectorPublishService {

    private static final Logger log = LoggerFactory.getLogger(VectorPublishService.class);

    private final VectorStore vectorStore;

    @Autowired
    public VectorPublishService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public Mono<Void> publishDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("No documents provided for publishing to vector store. Skipping.");
            return Mono.empty();
        }

        log.info("Publishing {} documents to the vector store...", documents.size());

        return Mono.fromRunnable(() -> vectorStore.add(documents))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> log.info("Successfully published {} documents to the vector store.", documents.size()))
                .then();
    }
}
