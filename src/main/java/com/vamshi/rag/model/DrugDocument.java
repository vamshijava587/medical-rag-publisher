package com.vamshi.rag.model;

public record DrugDocument(
        String productNdc,
        String brandName,
        String genericName,
        String dosageForm,
        String manufacturer,
        String activeIngredients,
        String description,
        String expirationDate
) {}