package com.vamshi.rag.runner;

import com.vamshi.rag.common.MedicalAiRagProperties;
import com.vamshi.rag.openfda.mapper.DrugMapper;
import com.vamshi.rag.openfda.service.OpenFdaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

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
                    if (response.results() == null) {
                        return Flux.empty();
                    }
                    return Flux.fromIterable(response.results());
                })
                .map(drugMapper::toDrugDocument)
                .collectList()
                .subscribe(
                    documents -> log.info("Successfully ingested {} drugs.", documents.size()),
                    error -> log.error("Error during data ingestion: ", error),
                    () -> log.info("Data ingestion process completed.")
                );
    }
}
