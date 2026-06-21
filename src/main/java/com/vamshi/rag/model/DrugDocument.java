package com.vamshi.rag.model;

import java.util.List;

public record DrugDocument(
        String id,
        String setId,
        String productNdc,
        String brandName,
        String genericName,
        String substanceName,
        String manufacturer,
        String route,
        String productType,
        String dosageForm,
        String activeIngredientsWithStrength,
        String purpose,
        String indicationsAndUsage,
        String dosageAndAdministration,
        String warnings,
        String keepOutOfReachOfChildren,
        String otherSafetyInformation,
        String adverseEventSummary
) {}