package com.vamshi.rag.service;

import com.vamshi.rag.model.DrugDocument;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DrugDocumentTransformer {

    public Document transform(DrugDocument drugDocument) {
        String content = buildContentForEmbedding(drugDocument);
        Map<String, Object> metadata = buildMetadata(drugDocument);
        return new Document(drugDocument.id(), content, metadata);
    }

    /**
     * Builds the text actually sent to the embedding model. Capped per-field
     * (not one global truncation) to stay under mxbai-embed-large's token limit
     * while keeping the most safety-critical content - warnings first - as
     * intact as possible. All text here is verbatim FDA wording, truncated by
     * character count only, never rewritten or paraphrased: an LLM rephrase
     * could silently drop or alter a clinical qualifier (a dose limit, an
     * exception clause) with no way to detect it happened across a batch.
     * The full untruncated text is preserved separately in metadata (see
     * buildMetadata below) for display/retrieval after a document is found.
     */
    private String buildContentForEmbedding(DrugDocument drugDocument) {
        StringBuilder sb = new StringBuilder();

        appendCapped(sb, drugDocument.warnings(), 600);
        appendCapped(sb, drugDocument.dosageAndAdministration(), 400);
        appendCapped(sb, drugDocument.indicationsAndUsage(), 300);
        appendCapped(sb, drugDocument.keepOutOfReachOfChildren(), 150);
        appendCapped(sb, drugDocument.otherSafetyInformation(), 150);
        appendCapped(sb, drugDocument.adverseEventSummary(), 200);
        appendCapped(sb, drugDocument.purpose(), 100);
        appendCapped(sb, drugDocument.activeIngredientsWithStrength(), 100);

        appendIfPresent(sb, drugDocument.brandName());
        appendIfPresent(sb, drugDocument.genericName());

        return sb.toString().trim();
    }

    private void appendCapped(StringBuilder sb, String value, int maxChars) {
        if (value == null || value.isBlank()) {
            return;
        }
        String text = value.length() > maxChars ? value.substring(0, maxChars) : value;
        sb.append(" ").append(text);
    }

    private void appendIfPresent(StringBuilder sb, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(" ").append(value);
        }
    }

    /**
     * Spring AI's Document constructor rejects metadata maps containing any null
     * values (Assert.noNullElements). openFDA data is frequently sparse - fields
     * like otherSafetyInformation or adverseEventSummary are often absent for a
     * given drug - so null entries are filtered out here rather than coerced to
     * empty strings, which would misleadingly suggest the field exists but is blank.
     * Stores the FULL untruncated text for every field, regardless of what was
     * capped in buildContentForEmbedding above - this is the source of truth
     * shown to the user/LLM after retrieval, not what was searched on.
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