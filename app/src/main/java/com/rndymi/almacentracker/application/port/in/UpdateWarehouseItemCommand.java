package com.rndymi.almacentracker.application.port.in;

public final class UpdateWarehouseItemCommand {

    private final long warehouseItemId;
    private final String category;
    private final String code;
    private final String site;
    private final String position;
    private final String observations;

    public UpdateWarehouseItemCommand(
            long warehouseItemId,
            String category,
            String code,
            String site,
            String position,
            String observations
    ) {
        this.warehouseItemId = warehouseItemId;
        this.category = category;
        this.code = code;
        this.site = site;
        this.position = position;
        this.observations = observations;
    }

    public long getWarehouseItemId() {
        return warehouseItemId;
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