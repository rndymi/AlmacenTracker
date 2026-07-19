package com.rndymi.almacentracker.application.result;

import java.util.Objects;

public final class WarehouseItemCsvRow {

    private final int rowNumber;
    private final String category;
    private final String code;
    private final String site;
    private final String position;
    private final String observations;

    public WarehouseItemCsvRow(
            int rowNumber,
            String category,
            String code,
            String site,
            String position,
            String observations
    ) {
        if (rowNumber < 2) {
            throw new IllegalArgumentException(
                    "CSV data row number must be at least 2"
            );
        }

        this.rowNumber = rowNumber;
        this.category = Objects.requireNonNull(category);
        this.code = Objects.requireNonNull(code);
        this.site = Objects.requireNonNull(site);
        this.position = Objects.requireNonNull(position);
        this.observations = Objects.requireNonNull(observations);
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getSite() {
        return site;
    }

    public String getPosition() {
        return position;
    }

    public String getObservations() {
        return observations;
    }
}