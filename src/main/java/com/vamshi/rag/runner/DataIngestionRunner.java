package com.vamshi.rag.runner;

import com.vamshi.rag.common.MedicalAiRagProperties;
import com.vamshi.rag.model.DrugDocument;
import com.vamshi.rag.openfda.mapper.DrugMapper;
import com.vamshi.rag.openfda.model.FdaDrugEventResponse;
import com.vamshi.rag.openfda.model.FdaDrugLabelResponse;
import com.vamshi.rag.openfda.service.OpenFdaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DataIngestionRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionRunner.class);

    private final OpenFdaClient openFdaClient;
    private final DrugMapper drugMapper;
    private final MedicalAiRagProperties properties;

    public DataIngestionRunner(OpenFdaClient openFdaClient, DrugMapper drugMapper, MedicalAiRagProperties properties) {
        this.openFdaClient = openFdaClient;
        this.drugMapper = drugMapper;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        if (!properties.openFda().ingestion().enabled()) {
            log.info("Data ingestion is disabled.");
            return;
        }

        int limit = properties.openFda().ingestion().limit();
        int skip = properties.openFda().ingestion().skip();

        log.info("Starting data ingestion with limit={} and skip={}", limit, skip);

        openFdaClient.fetchDrugs(limit, skip)
                .flatMapMany(response -> {
                    if (response == null || response.results() == null) {
                        return Flux.empty();
                    }
                    return Flux.fromIterable(response.results());
                })
                .flatMap(fdaDrugRecord -> {
                    DrugDocument drugDocument = drugMapper.toDrugDocument(fdaDrugRecord);
                    log.info("Ingested Drug: {} [{}]", drugDocument.brandName(), drugDocument.productNdc());

                    // Fetch additional data for each drug and log it
                    Mono<FdaDrugLabelResponse> labelMono = openFdaClient.fetchDrugLabel(drugDocument.brandName())
                            .doOnNext(label -> log.info("Drug Label for {}: {}", drugDocument.brandName(), label))
                            .onErrorResume(e -> {
                                log.error("Error fetching drug label for {}: {}", drugDocument.brandName(), e.getMessage());
                                return Mono.empty();
                            });

                    Mono<FdaDrugEventResponse> eventsMono = openFdaClient.fetchDrugEvents(drugDocument.genericName(), 1)
                            .doOnNext(events -> log.info("Drug Events for {}: {}", drugDocument.genericName(), events))
                            .onErrorResume(e -> {
                                log.error("Error fetching drug events for {}: {}", drugDocument.genericName(), e.getMessage());
                                return Mono.empty();
                            });

                    // Combine all additional fetches and return the original drug document
                    return Mono.when(labelMono, eventsMono)
                            .thenReturn(drugDocument);
                })
                .collectList()
                .subscribe(
                    documents -> log.info("Successfully processed and ingested {} drugs.", documents.size()),
                    error -> log.error("Error during data ingestion: ", error),
                    () -> log.info("Data ingestion process completed.")
                );
    }
}
