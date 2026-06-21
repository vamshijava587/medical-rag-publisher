package com.vamshi.rag.openfda.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FdaDrugEventResponse(
    FdaMeta meta,
    List<FdaDrugEventRecord> results
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FdaDrugEventRecord(
        @JsonProperty("safetyreportversion") String safetyReportVersion,
        @JsonProperty("safetyreportid") String safetyReportId,
        @JsonProperty("primarysourcecountry") String primarySourceCountry,
        @JsonProperty("occurcountry") String occurCountry,
        Patient patient
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Patient(
        @JsonProperty("patientonsetage") String patientOnsetAge,
        @JsonProperty("patientonsetageunit") String patientOnsetAgeUnit,
        @JsonProperty("patientweight") String patientWeight,
        @JsonProperty("patientsex") String patientSex,
        List<Reaction> reaction,
        List<Drug> drug
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Reaction(
        @JsonProperty("reactionmeddraversionpt") String reactionMeddraVersionPt,
        @JsonProperty("reactionmeddrapt") String reactionMeddraPt,
        @JsonProperty("reactionoutcome") String reactionOutcome
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Drug(
        @JsonProperty("drugcharacterization") String drugCharacterization,
        @JsonProperty("medicinalproduct") String medicinalProduct,
        @JsonProperty("drugauthorizationnumb") String drugAuthorizationNumb,
        @JsonProperty("drugdosagetext") String drugDosageText,
        @JsonProperty("actiondrug") String actionDrug,
        @JsonProperty("drugadditional") String drugAdditional,
        ActiveSubstance activesubstance,
        OpenFdaData openfda
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ActiveSubstance(
        @JsonProperty("activesubstancename") String activeSubstanceName
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
        @JsonProperty("rxcui") List<String> rxcui,
        @JsonProperty("spl_id") List<String> splId,
        @JsonProperty("spl_set_id") List<String> splSetId,
        @JsonProperty("package_ndc") List<String> packageNdc,
        List<String> unii,
        @JsonProperty("nui") List<String> nui,
        @JsonProperty("pharm_class_epc") List<String> pharmClassEpc,
        @JsonProperty("pharm_class_cs") List<String> pharmClassCs,
        @JsonProperty("pharm_class_moa") List<String> pharmClassMoa,
        @JsonProperty("pharm_class_pe") List<String> pharmClassPe
    ) {}
}
