package com.vamshi.rag.model;

import java.util.List;

public record DrugDocument(
        String id,
        String setId,
        String productNdc,
        String brandName,
        String genericName,
        List<String> substanceName,
        String manufacturer,
        List<String> route,
        String productType,
        String dosageForm,
        String activeIngredientsWithStrength,
        String purpose,
        String indicationsAndUsage,
        String dosageAndAdministration,
        String warnings,
        String keepOutOfReachOfChildren,
        String otherSafetyInformation,
        String inactiveIngredient,        // reserved for v2, null for now
        String adverseEventSummary,       // single rolled-up string, not a list of objects
        String embeddingText
) {}