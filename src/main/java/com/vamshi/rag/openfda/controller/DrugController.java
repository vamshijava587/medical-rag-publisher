package com.vamshi.rag.controller;

import com.vamshi.rag.model.DrugDocument;
import com.vamshi.rag.openfda.service.OpenFdaService;
import com.vamshi.rag.service.VectorPipelineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drugs")
public class DrugController {

    private final OpenFdaService openFdaService;
    private final VectorPipelineService vectorPipelineService;

    public DrugController(OpenFdaService openFdaService, VectorPipelineService vectorPipelineService) {
        this.openFdaService = openFdaService;
        this.vectorPipelineService = vectorPipelineService;
    }

    @GetMapping
    public Mono<List<DrugDocument>> getDrugs(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int skip,
            @RequestHeader(name = "X-Publish-And-Return", defaultValue = "false") boolean publishAndReturn) {

        return openFdaService.getDrugs(limit, skip)
                .flatMap(drugDocuments -> {
                    if (!publishAndReturn) {
                        return Mono.just(drugDocuments);
                    }
                    return vectorPipelineService.publishVectorPipeline(drugDocuments)
                            .thenReturn(drugDocuments);
                });
    }
}