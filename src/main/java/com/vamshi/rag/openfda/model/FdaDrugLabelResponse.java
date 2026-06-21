package com.vamshi.rag.openfda.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FdaDrugLabelResponse(
    FdaMeta meta,
    List<FdaDrugLabelRecord> results
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FdaDrugLabelRecord(
        @JsonProperty("spl_product_data_elements") List<String> splProductDataElements,
        @JsonProperty("active_ingredient") List<String> activeIngredient,
        List<String> purpose,
        @JsonProperty("indications_and_usage") List<String> indicationsAndUsage,
        List<String> warnings,
        @JsonProperty("keep_out_of_reach_of_children") List<String> keepOutOfReachOfChildren,
        @JsonProperty("dosage_and_administration") List<String> dosageAndAdministration,
        @JsonProperty("inactive_ingredient") List<String> inactiveIngredient,
        @JsonProperty("other_safety_information") List<String> otherSafetyInformation,
        List<String> questions,
        @JsonProperty("package_label_principal_display_panel") List<String> packageLabelPrincipalDisplayPanel,
        @JsonProperty("set_id") String setId,
        String id,
        @JsonProperty("effective_time") String effectiveTime,
        String version,
        OpenFdaData openfda
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpenFdaData(
        @JsonProperty("application_number") List<String> applicationNumber,
        @JsonProperty("brand_name") List<String> brandName,
        @JsonProperty("generic_name") List<String> genericName,
        @JsonProperty("manufacturer_name") List<String> manufacturerName,
        @JsonProperty("product_ndc") List<String> productNdc,
        @JsonProperty("product_type") List<String> productType,
        List<String> route,
        @JsonProperty("substance_name") List<String> substanceName,
        @JsonProperty("spl_id") List<String> splId,
        @JsonProperty("spl_set_id") List<String> splSetId,
        @JsonProperty("package_ndc") List<String> packageNdc,
        @JsonProperty("is_original_packager") List<Boolean> isOriginalPackager,
        List<String> unii
    ) {}
}
