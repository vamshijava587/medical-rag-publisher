package com.vamshi.rag.openfda.controller;

import com.vamshi.rag.model.DrugDocument;
import com.vamshi.rag.openfda.mapper.DrugMapper;
import com.vamshi.rag.openfda.service.OpenFdaClient;
import com.vamshi.rag.openfda.service.OpenFdaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drugs")
public class DrugController {

    private final OpenFdaService openFdaService;

    public DrugController(OpenFdaService openFdaService) {
        this.openFdaService = openFdaService;
    }

    @GetMapping
    public Mono<List<DrugDocument>> getDrugs(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int skip) {
        
        return openFdaService.getDrugs(limit, skip);
    }
}
