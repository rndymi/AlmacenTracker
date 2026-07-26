package com.rndymi.almacentracker.domain.reference;

import java.util.Objects;

public final class WarehouseReferenceLocation {

    public enum Status {
        FOUND,
        NOT_FOUND
    }

    private static final long NO_WAREHOUSE_ITEM_ID = -1L;

    private final WarehouseReference reference;
    private final Status status;
    private final long warehouseItemId;
    private final String site;
    private final String position;

    private WarehouseReferenceLocation(
            WarehouseReference reference,
            Status status,
            long warehouseItemId,
            String site,
            String position
    ) {
        this.reference = Objects.requireNonNull(
                reference,
                "reference"
        );

        this.status = Objects.requireNonNull(
                status,
                "status"
        );

        this.warehouseItemId = warehouseItemId;
        this.site = site;
        this.position = position;
    }

    public static WarehouseReferenceLocation found(
            WarehouseReference reference,
            long warehouseItemId,
            String site,
            String position
    ) {
        Objects.requireNonNull(site, "site");

        if (warehouseItemId <= 0L) {
            throw new IllegalArgumentException(
                    "warehouseItemId must be positive"
            );
        }

        if (site.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "site must not be empty"
            );
        }

        return new WarehouseReferenceLocation(
                reference,
                Status.FOUND,
                warehouseItemId,
                site,
                position
        );
    }

    public static WarehouseReferenceLocation notFound(
            WarehouseReference reference
    ) {
        return new WarehouseReferenceLocation(
                reference,
                Status.NOT_FOUND,
                NO_WAREHOUSE_ITEM_ID,
                null,
                null
        );
    }

    public WarehouseReference getReference() {
        return reference;
    }

    public Status getStatus() {
        return status;
    }

    public long getWarehouseItemId() {
        return warehouseItemId;
    }

    public String getSite() {
        return site;
    }

    public String getPosition() {
        return position;
    }

    public boolean isFound() {
        return status == Status.FOUND;
    }

    public boolean hasPosition() {
        return position != null
                && !position.trim().isEmpty();
    }
}