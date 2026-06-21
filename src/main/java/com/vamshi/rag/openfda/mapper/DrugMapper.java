package com.vamshi.rag.openfda.mapper;

import com.vamshi.rag.model.DrugDocument;
import com.vamshi.rag.openfda.model.FdaDrugEventResponse;
import com.vamshi.rag.openfda.model.FdaDrugEventResponse.FdaDrugEventRecord;
import com.vamshi.rag.openfda.model.FdaDrugLabelResponse.FdaDrugLabelRecord;
import com.vamshi.rag.openfda.model.FdaDrugRecord;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DrugMapper {

    private static final int MAX_REACTIONS_IN_SUMMARY = 5;
    private static final int MAX_INGREDIENT_TEXT_LENGTH = 300;

    /**
     * Combines Label data (clinical content), NDC Directory data (product listing
     * detail), and a pre-filtered list of Adverse Event reports relevant to this
     * drug (matched upstream, before this method is called) into a single
     * DrugDocument for vector storage.
     * ndcRecord and events may be null/empty - label data is the only required
     * source, since it's the only one with clinical content (purpose, warnings,
     * dosage). Adverse events are rolled up into one short summary string rather
     * than stored as a nested list, to keep the Qdrant payload small.
     */
    public DrugDocument toDrugDocument(FdaDrugLabelRecord label,
                                       FdaDrugRecord ndcRecord,
                                       List<FdaDrugEventRecord> events) {

        var openfda = label.openfda();

        String productNdc = firstOrNull(openfda != null ? openfda.productNdc() : null);
        if (productNdc == null && ndcRecord != null) {
            productNdc = ndcRecord.productNdc();
        }

        String brandName = firstOrNull(openfda != null ? openfda.brandName() : null);
        if (brandName == null && ndcRecord != null) {
            brandName = ndcRecord.brandName();
        }

        String genericName = firstOrNull(openfda != null ? openfda.genericName() : null);
        if (genericName == null && ndcRecord != null) {
            genericName = ndcRecord.genericName();
        }

        String manufacturer = firstOrNull(openfda != null ? openfda.manufacturerName() : null);
        if (manufacturer == null && ndcRecord != null) {
            manufacturer = ndcRecord.labelerName();
        }

        String substanceName = (openfda != null && openfda.substanceName() != null)
                ? String.join(", ", openfda.substanceName())  : null;
        String route = (openfda != null && openfda.route() != null)
                ? String.join(", ", openfda.route())  : null;
        String productType = firstOrNull(openfda != null ? openfda.productType() : null);

        String dosageForm = (ndcRecord != null) ? ndcRecord.dosageForm() : null;

        String activeIngredientsWithStrength = buildActiveIngredientsText(label, ndcRecord);

        String purpose = firstOrNull(label.purpose());
        String indicationsAndUsage = firstOrNull(label.indicationsAndUsage());
        String dosageAndAdministration = firstOrNull(label.dosageAndAdministration());
        String warnings = firstOrNull(label.warnings());
        String keepOutOfReachOfChildren = firstOrNull(label.keepOutOfReachOfChildren());
        String otherSafetyInformation = firstOrNull(label.otherSafetyInformation());

        String adverseEventSummary = buildAdverseEventSummary(events);


        return new DrugDocument(
                label.id(),
                label.setId(),
                productNdc,
                brandName,
                genericName,
                substanceName,
                manufacturer,
                route,
                productType,
                dosageForm,
                activeIngredientsWithStrength,
                purpose,
                indicationsAndUsage,
                dosageAndAdministration,
                warnings,
                keepOutOfReachOfChildren,
                otherSafetyInformation,
                adverseEventSummary
        );
    }

    /**
     * Prefers the NDC Directory's structured ingredient+strength data when available
     * (it's already clean: "Acetaminophen (500 mg)"), falling back to the label's
     * active_ingredient field otherwise. Truncated to keep payload size bounded -
     * some labels have long compound ingredient strings.
     */
    private String buildActiveIngredientsText(FdaDrugLabelRecord label, FdaDrugRecord ndcRecord) {
        String text;
        if (ndcRecord != null && ndcRecord.activeIngredients() != null && !ndcRecord.activeIngredients().isEmpty()) {
            text = ndcRecord.activeIngredients().stream()
                    .map(ai -> ai.name() + " (" + ai.strength() + ")")
                    .collect(Collectors.joining(", "));
        } else {
            text = firstOrNull(label.activeIngredient());
        }

        if (text == null) {
            return null;
        }
        return text.length() > MAX_INGREDIENT_TEXT_LENGTH
                ? text.substring(0, MAX_INGREDIENT_TEXT_LENGTH) + "..."
                : text;
    }

    /**
     * Rolls up adverse event reports into one short, human-readable string instead
     * of a nested list of objects - this is the main payload-size fix versus the
     * earlier version. Caps at MAX_REACTIONS_IN_SUMMARY distinct reactions.
     * NOTE: FAERS reports establish association, not causation - this phrasing is
     * deliberately hedged and should be reflected in how the system prompt frames
     * this content to the user.
     */
    private String buildAdverseEventSummary(List<FdaDrugEventRecord> events) {
        if (events == null || events.isEmpty()) {
            return null;
        }

        List<String> reactions = events.stream()
                .filter(e -> e.patient() != null && e.patient().reaction() != null)
                .flatMap(e -> e.patient().reaction().stream())
                .map(FdaDrugEventResponse.Reaction::reactionMeddraPt)
                .filter(Objects::nonNull)
                .distinct()
                .limit(MAX_REACTIONS_IN_SUMMARY)
                .collect(Collectors.toList());

        if (reactions.isEmpty()) {
            return null;
        }

        return "Reported reactions across " + events.size() + " case(s) in FAERS (association only, not confirmed causation): "
                + String.join(", ", reactions) + ".";
    }


    private String firstOrNull(List<String> values) {
        return (values == null || values.isEmpty()) ? null : values.getFirst();
    }
}