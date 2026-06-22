package com.vamshi.rag.service;

import com.vamshi.rag.model.DrugDocument;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class DrugDocumentTransformer {

    public Document transform(DrugDocument drugDocument) {
        String content = buildContentForEmbedding(drugDocument);
        Map<String, Object> metadata = buildMetadata(drugDocument);
        return new Document(drugDocument.id(), content, metadata);
    }

    private String buildContentForEmbedding(DrugDocument drugDocument) {
        return String.join(" ",
                Objects.toString(drugDocument.id(), ""),
                Objects.toString(drugDocument.brandName(), ""),
                Objects.toString(drugDocument.productNdc(), ""),
                Objects.toString(drugDocument.genericName(), ""),
                Objects.toString(drugDocument.substanceName(), ""),
                Objects.toString(drugDocument.manufacturer(), ""),
                Objects.toString(drugDocument.route(), ""),
                Objects.toString(drugDocument.productType(), ""),
                Objects.toString(drugDocument.dosageForm(), ""),
                Objects.toString(drugDocument.activeIngredientsWithStrength(), ""),
                Objects.toString(drugDocument.purpose(), ""),
                Objects.toString(drugDocument.indicationsAndUsage(), ""),
                Objects.toString(drugDocument.dosageAndAdministration(), ""),
                Objects.toString(drugDocument.warnings(), ""),
                Objects.toString(drugDocument.keepOutOfReachOfChildren(), ""),
                Objects.toString(drugDocument.otherSafetyInformation(), ""),
                Objects.toString(drugDocument.adverseEventSummary(), "")
        ).trim();
    }

    /**
     * Spring AI's Document constructor rejects metadata maps containing any null
     * values (Assert.noNullElements). openFDA data is frequently sparse - fields
     * like otherSafetyInformation or adverseEventSummary are often absent for a
     * given drug - so null entries are filtered out here rather than coerced to
     * empty strings, which would misleadingly suggest the field exists but is blank.
     */
    private Map<String, Object> buildMetadata(DrugDocument drugDocument) {
        Map<String, Object> metadata = new HashMap<>();
        putIfNotNull(metadata, "id", drugDocument.id());
        putIfNotNull(metadata, "setId", drugDocument.setId());
        putIfNotNull(metadata, "productNdc", drugDocument.productNdc());
        putIfNotNull(metadata, "brandName", drugDocument.brandName());
        putIfNotNull(metadata, "genericName", drugDocument.genericName());
        putIfNotNull(metadata, "substanceName", drugDocument.substanceName());
        putIfNotNull(metadata, "manufacturer", drugDocument.manufacturer());
        putIfNotNull(metadata, "route", drugDocument.route());
        putIfNotNull(metadata, "productType", drugDocument.productType());
        putIfNotNull(metadata, "dosageForm", drugDocument.dosageForm());
        putIfNotNull(metadata, "activeIngredientsWithStrength", drugDocument.activeIngredientsWithStrength());
        putIfNotNull(metadata, "purpose", drugDocument.purpose());
        putIfNotNull(metadata, "indicationsAndUsage", drugDocument.indicationsAndUsage());
        putIfNotNull(metadata, "dosageAndAdministration", drugDocument.dosageAndAdministration());
        putIfNotNull(metadata, "warnings", drugDocument.warnings());
        putIfNotNull(metadata, "keepOutOfReachOfChildren", drugDocument.keepOutOfReachOfChildren());
        putIfNotNull(metadata, "otherSafetyInformation", drugDocument.otherSafetyInformation());
        putIfNotNull(metadata, "adverseEventSummary", drugDocument.adverseEventSummary());
        // inactiveIngredient deliberately omitted - reserved for v2, always null for now
        return metadata;
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}