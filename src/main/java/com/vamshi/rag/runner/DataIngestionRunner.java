package com.vamshi.rag.runner;

import com.vamshi.rag.common.MedicalAiRagProperties;
import com.vamshi.rag.openfda.service.OpenFdaService;
import com.vamshi.rag.service.VectorPipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataIngestionRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionRunner.class);

    private final OpenFdaService openFdaService;
    private final MedicalAiRagProperties properties;
    private final VectorPipelineService vectorPipelineService;

    public DataIngestionRunner(OpenFdaService openFdaService, MedicalAiRagProperties properties,VectorPipelineService vectorPipelineService) {
        this.openFdaService = openFdaService;
        this.properties = properties;
        this.vectorPipelineService = vectorPipelineService;
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

        openFdaService.getDrugs(limit, skip)
                .flatMap(vectorPipelineService::publishVectorPipeline)
                .subscribe(
                        unused -> {},
                        error -> log.error("Error during data ingestion: ", error),
                        () -> log.info("Data ingestion process completed.")
                );
    }
}