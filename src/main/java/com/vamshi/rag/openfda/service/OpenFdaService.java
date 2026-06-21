package com.vamshi.rag.openfda.service;

import com.vamshi.rag.model.DrugDocument;
import com.vamshi.rag.openfda.mapper.DrugMapper;
import com.vamshi.rag.openfda.model.FdaDrugEventResponse;
import com.vamshi.rag.openfda.model.FdaDrugLabelResponse;
import com.vamshi.rag.openfda.model.FdaDrugRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class OpenFdaService {
    private static final Logger log = LoggerFactory.getLogger(OpenFdaService.class);

    private final OpenFdaClient openFdaClient;
    private final DrugMapper drugMapper;


    public OpenFdaService(OpenFdaClient openFdaClient, DrugMapper drugMapper) {
        this.openFdaClient = openFdaClient;
        this.drugMapper = drugMapper;
    }

    public Mono<List<DrugDocument>> getDrugs(int limit, int skip) {
        return openFdaClient.fetchDrugs(limit, skip)
                .flatMapMany(response -> {
                    if (response.results() == null) {
                        return Flux.empty();
                    }
                    return Flux.fromIterable(response.results());
                })
                .flatMap(this::buildDrugDocument)
                .collectList();
    }
    private Mono<DrugDocument> buildDrugDocument(FdaDrugRecord ndcRecord) {
        String brandName = ndcRecord.brandName();
        String genericName = ndcRecord.genericName();

        Mono<FdaDrugLabelResponse.FdaDrugLabelRecord> labelMono = openFdaClient.fetchDrugLabel(brandName)
                .mapNotNull(this::firstLabelRecord)
                .onErrorResume(e -> {
                    log.error("Error fetching drug label for {}: {}", brandName, e.getMessage());
                    return Mono.empty();
                });

        Mono<List<FdaDrugEventResponse.FdaDrugEventRecord>> eventsMono = openFdaClient.fetchDrugEvents(genericName, 1)
                .map(this::eventRecordsOrEmpty)
                .onErrorResume(e -> {
                    log.error("Error fetching drug events for {}: {}", genericName, e.getMessage());
                    return Mono.just(Collections.emptyList());
                });

        return Mono.zip(labelMono, eventsMono)
                .map(tuple -> drugMapper.toDrugDocument(tuple.getT1(), ndcRecord, tuple.getT2()));
    }

    private FdaDrugLabelResponse.FdaDrugLabelRecord firstLabelRecord(FdaDrugLabelResponse response) {
        if (response == null || response.results() == null || response.results().isEmpty()) {
            return null;
        }
        return response.results().getFirst();
    }

    private List<FdaDrugEventResponse.FdaDrugEventRecord> eventRecordsOrEmpty(FdaDrugEventResponse response) {
        return (response == null || response.results() == null) ? Collections.emptyList() : response.results();
    }
}
