package com.rndymi.almacentracker.application.result;

import java.util.Objects;

public final class WarehouseItemCsvRow {

    private final String category;
    private final String code;
    private final String site;
    private final String position;
    private final String observations;

    public WarehouseItemCsvRow(
            String category,
            String code,
            String site,
            String position,
            String observations
    ) {
        this.category = Objects.requireNonNull(category);
        this.code = Objects.requireNonNull(code);
        this.site = Objects.requireNonNull(site);
        this.position = Objects.requireNonNull(position);
        this.observations = Objects.requireNonNull(observations);
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