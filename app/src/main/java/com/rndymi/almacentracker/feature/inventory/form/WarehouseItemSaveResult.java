package com.rndymi.almacentracker.feature.inventory.form;

public final class WarehouseItemSaveResult {

    public enum Status {
        SUCCESS,
        VALIDATION_ERROR,
        DUPLICATE,
        NOT_FOUND,
        PERSISTENCE_ERROR
    }

    private final Status status;
    private final long warehouseItemId;
    private final boolean categoryRequired;
    private final boolean codeRequired;
    private final boolean siteRequired;
    private final Throwable cause;

    private WarehouseItemSaveResult(
            Status status,
            long warehouseItemId,
            boolean categoryRequired,
            boolean codeRequired,
            boolean siteRequired,
            Throwable cause
    ) {
        this.status = status;
        this.warehouseItemId = warehouseItemId;
        this.categoryRequired = categoryRequired;
        this.codeRequired = codeRequired;
        this.siteRequired = siteRequired;
        this.cause = cause;
    }

    public static WarehouseItemSaveResult success(
            long warehouseItemId
    ) {
        return new WarehouseItemSaveResult(
                Status.SUCCESS,
                warehouseItemId,
                false,
                false,
                false,
                null
        );
    }

    public static WarehouseItemSaveResult validationError(
            boolean categoryRequired,
            boolean codeRequired,
            boolean siteRequired
    ) {
        return new WarehouseItemSaveResult(
                Status.VALIDATION_ERROR,
                0L,
                categoryRequired,
                codeRequired,
                siteRequired,
                null
        );
    }

    public static WarehouseItemSaveResult duplicate() {
        return new WarehouseItemSaveResult(
                Status.DUPLICATE,
                0L,
                false,
                false,
                false,
                null
        );
    }

    public static WarehouseItemSaveResult notFound() {
        return new WarehouseItemSaveResult(
                Status.NOT_FOUND,
                0L,
                false,
                false,
                false,
                null
        );
    }

    public static WarehouseItemSaveResult persistenceError(
            Throwable cause
    ) {
        return new WarehouseItemSaveResult(
                Status.PERSISTENCE_ERROR,
                0L,
                false,
                false,
                false,
                cause
        );
    }

    public Status getStatus() {
        return status;
    }

    public long getWarehouseItemId() {
        return warehouseItemId;
    }

    public boolean isCategoryRequired() {
        return categoryRequired;
    }

    public boolean isCodeRequired() {
        return codeRequired;
    }

    public boolean isSiteRequired() {
        return siteRequired;
    }

    public Throwable getCause() {
        return cause;
    }
}
