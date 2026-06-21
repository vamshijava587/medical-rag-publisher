package com.vamshi.rag.openfda.mapper;

import com.vamshi.rag.model.DrugDocument;
import com.vamshi.rag.openfda.model.FdaDrugRecord;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DrugMapper {

    public DrugDocument toDrugDocument(FdaDrugRecord record) {
        String activeIngredients = record.activeIngredients() == null ? "" :
                record.activeIngredients().stream()
                        .map(ai -> ai.name() + " (" + ai.strength() + ")")
                        .collect(Collectors.joining(", "));

        String manufacturer = (record.openfda() != null && record.openfda().manufacturerName() != null) ?
                String.join(", ", record.openfda().manufacturerName()) : record.labelerName();

        String description = record.packaging().stream().map(FdaDrugRecord.Packaging::description).collect(Collectors.joining(","));

        return new DrugDocument(
                record.productNdc(),
                record.brandName(),
                record.genericName(),
                record.dosageForm(),
                manufacturer,
                activeIngredients,
                description,
                record.listingExpirationDate()
        );
    }
}
