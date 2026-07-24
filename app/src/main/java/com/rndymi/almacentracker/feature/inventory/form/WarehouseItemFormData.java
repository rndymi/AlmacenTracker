package com.rndymi.almacentracker.feature.inventory.form;

public final class WarehouseItemFormData {

    private final String category;
    private final String code;
    private final String site;
    private final String position;
    private final String observations;

    public WarehouseItemFormData(
            String category,
            String code,
            String site,
            String position,
            String observations
    ) {
        this.category = category;
        this.code = code;
        this.site = site;
        this.position = position;
        this.observations = observations;
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
