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

    private static final String UNAPPROVED_HOMEOPATHIC = "UNAPPROVED HOMEOPATHIC";
    private static final String BULK_INGREDIENT_MARKER = "BULK INGREDIENT";
    private static final String FURTHER_PROCESSING_MARKER = "FURTHER PROCESSING";
    private static final int MAX_GENERIC_NAME_COMPONENTS = 1;

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
                .filter(this::isQueryableDrug)
                .flatMap(this::buildDrugDocument)
                .collectList();
    }

    /**
     * Builds a DrugDocument only when a real label match is found. If the label
     * lookup comes back empty (no match in openFDA), or either call errors out,
     * the record is dropped entirely from the resulting list - no sparse/partial
     * documents are created.
     */
    private Mono<DrugDocument> buildDrugDocument(FdaDrugRecord ndcRecord) {
        String brandName = ndcRecord.brandName();
        String genericName = normalizeGenericName(ndcRecord.genericName());
        Mono<FdaDrugLabelResponse.FdaDrugLabelRecord> labelMono = openFdaClient.fetchDrugLabel(brandName)
                .mapNotNull(this::firstLabelRecord)
                .onErrorResume(e -> {
                    log.warn("Error fetching drug label for {}: {}", brandName, e.getMessage());
                    return Mono.empty();
                });

        Mono<List<FdaDrugEventResponse.FdaDrugEventRecord>> eventsMono = openFdaClient.fetchDrugEvents(genericName, 1)
                .map(this::eventRecordsOrEmpty)
                .onErrorResume(e -> {
                    log.warn("Error fetching drug events for {}: {}", genericName, e.getMessage());
                    return Mono.just(Collections.emptyList());
                })
                .defaultIfEmpty(Collections.emptyList());

        // zip requires both sources to emit; since labelMono is empty whenever no
        // label was found, the whole record is correctly dropped (not surfaced as
        // a sparse document) - this is the desired behavior here.
        return Mono.zip(labelMono, eventsMono)
                .map(tuple -> drugMapper.toDrugDocument(tuple.getT1(), ndcRecord, tuple.getT2()))
                .doOnDiscard(FdaDrugLabelResponse.FdaDrugLabelRecord.class, label ->
                        log.info("No label match for NDC record {} (brand='{}') - record dropped",
                                ndcRecord.productId(), brandName));
    }

    /**
     * Filters out NDC records that are known to fail label/event lookups, BEFORE
     * any API call is made for them:
     * - Unfinished products, bulk ingredients, or drugs pending further processing
     * - Unapproved homeopathic products (multi-ingredient blends rarely match FAERS/label data)
     * - Records with no brand name (label lookup needs a non-null query)
     * - Generic names that are comma-separated blends with more than a few components
     */
    private boolean isQueryableDrug(FdaDrugRecord ndcRecord) {
        if (!Boolean.TRUE.equals(ndcRecord.finished())) {
            log.info("Filtering out NDC record {} - not a finished product", ndcRecord.productId());
            return false;
        }

        String marketingCategory = ndcRecord.marketingCategory();
        if (marketingCategory != null && marketingCategory.equalsIgnoreCase(UNAPPROVED_HOMEOPATHIC)) {
            log.info("Filtering out NDC record {} - unapproved homeopathic", ndcRecord.productId());
            return false;
        }

        String productType = ndcRecord.productType();
        if (productType != null
                && (productType.contains(BULK_INGREDIENT_MARKER) || productType.contains(FURTHER_PROCESSING_MARKER))) {
            log.info("Filtering out NDC record {} - bulk ingredient / further processing", ndcRecord.productId());
            return false;
        }

        String brandName = ndcRecord.brandName();
        if (brandName == null || brandName.isBlank()) {
            log.info("Filtering out NDC record {} - no brand name", ndcRecord.productId());
            return false;
        }

        String genericName = ndcRecord.genericName();
        if (genericName == null || genericName.isBlank()) {
            log.info("Filtering out NDC record {} - no generic name", ndcRecord.productId());
            return false;
        }
        if (genericName.split(",").length > MAX_GENERIC_NAME_COMPONENTS) {
            log.info("Filtering out NDC record {} - multi-ingredient blend ({})",
                    ndcRecord.productId(), genericName);
            return false;
        }

        return true;
    }

    /**
     * Strips trailing strength/percentage tokens (e.g. "Capsaicin 0.03%" -> "Capsaicin",
     * "ZINC OXIDE 22.75%" -> "ZINC OXIDE") so the event-query name is a clean substance
     * name rather than a strength-qualified product description.
     */
    private String normalizeGenericName(String genericName) {
        if (genericName == null) {
            return null;
        }
        return genericName.replaceAll("[\\d.]+%?", "").trim();
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