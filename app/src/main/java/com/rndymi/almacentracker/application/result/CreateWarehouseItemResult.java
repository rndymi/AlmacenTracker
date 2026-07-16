package com.rndymi.almacentracker.application.result;

public final class CreateWarehouseItemResult {

    public enum Status {
        SUCCESS,
        VALIDATION_ERROR,
        DUPLICATE,
        PERSISTENCE_ERROR
    }

    private final Status status;
    private final long createdItemId;
    private final boolean categoryRequired;
    private final boolean codeRequired;
    private final boolean siteRequired;
    private final Throwable cause;

    public CreateWarehouseItemResult(
            Status status,
            long createdItemId,
            boolean categoryRequired,
            boolean codeRequired,
            boolean siteRequired,
            Throwable cause) {
        this.status = status;
        this.createdItemId = createdItemId;
        this.categoryRequired = categoryRequired;
        this.codeRequired = codeRequired;
        this.siteRequired = siteRequired;
        this.cause = cause;
    }

    public static CreateWarehouseItemResult success(long createdItemId) {
        return new CreateWarehouseItemResult(
                Status.SUCCESS,
                createdItemId,
                false,
                false,
                false,
                null
        );
    }

    public static CreateWarehouseItemResult validationError(
            boolean categoryRequired,
            boolean codeRequired,
            boolean siteRequired
    ) {
        return new CreateWarehouseItemResult(
                Status.VALIDATION_ERROR,
                0L,
                categoryRequired,
                codeRequired,
                siteRequired,
                null
        );
    }

    public static CreateWarehouseItemResult duplicate() {
        return new CreateWarehouseItemResult(
                Status.DUPLICATE,
                0L,
                false,
                false,
                false,
                null
        );
    }

    public static CreateWarehouseItemResult persistenceError(
            Throwable cause
    ) {
        return new CreateWarehouseItemResult(
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

    public long getCreatedItemId() {
        return createdItemId;
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
