package com.rndymi.almacentracker.application.port.in;

public final class CreateWarehouseItemCommand {
    private final String category;
    private final String code;
    private final String site;
    private final String position;
    private final String observations;

    public CreateWarehouseItemCommand(String category, String code, String site, String position, String observations) {
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
