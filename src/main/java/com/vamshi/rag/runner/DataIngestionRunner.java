package com.vamshi.rag.runner;

import com.vamshi.rag.common.MedicalAiRagProperties;
import com.vamshi.rag.model.DrugDocument;
import com.vamshi.rag.openfda.mapper.DrugMapper;
import com.vamshi.rag.openfda.model.FdaDrugEventResponse;
import com.vamshi.rag.openfda.model.FdaDrugLabelResponse;
import com.vamshi.rag.openfda.model.FdaDrugLabelResponse.FdaDrugLabelRecord;
import com.vamshi.rag.openfda.model.FdaDrugRecord;
import com.vamshi.rag.openfda.service.OpenFdaClient;
import com.vamshi.rag.openfda.service.OpenFdaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class DataIngestionRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionRunner.class);

    private final OpenFdaService openFdaService;
    private final MedicalAiRagProperties properties;

    public DataIngestionRunner(OpenFdaService openFdaService, MedicalAiRagProperties properties) {
        this.openFdaService = openFdaService;
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

        openFdaService.getDrugs(limit, skip);

        }
}