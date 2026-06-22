package com.vamshi.rag.openfda.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FdaDrugRecord(
    @JsonProperty("product_ndc") String productNdc,
    @JsonProperty("generic_name") String genericName,
    @JsonProperty("labeler_name") String labelerName,
    @JsonProperty("brand_name") String brandName,
    @JsonProperty("active_ingredients") List<ActiveIngredient> activeIngredients,
    Boolean finished,
    List<Packaging> packaging,
    @JsonProperty("listing_expiration_date") String listingExpirationDate,
    OpenFdaData openfda,
    @JsonProperty("marketing_category") String marketingCategory,
    @JsonProperty("dosage_form") String dosageForm,
    @JsonProperty("spl_id") String splId,
    @JsonProperty("product_type") String productType,
    List<String> route,
    @JsonProperty("marketing_start_date") String marketingStartDate,
    @JsonProperty("product_id") String productId,
    @JsonProperty("application_number") String applicationNumber,
    @JsonProperty("brand_name_base") String brandNameBase,
    @JsonProperty("pharm_class") List<String> pharmClass
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ActiveIngredient(
        String name,
        String strength
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Packaging(
        @JsonProperty("package_ndc") String packageNdc,
        String description,
        @JsonProperty("marketing_start_date") String marketingStartDate,
        Boolean sample
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpenFdaData(
        @JsonProperty("manufacturer_name") List<String> manufacturerName,
        List<String> rxcui,
        @JsonProperty("spl_set_id") List<String> splSetId,
        @JsonProperty("is_original_packager") List<Boolean> isOriginalPackager,
        List<String> upc,
        List<String> nui,
        @JsonProperty("pharm_class_epc") List<String> pharmClassEpc,
        @JsonProperty("pharm_class_cs") List<String> pharmClassCs,
        List<String> unii
    ) {}
}
