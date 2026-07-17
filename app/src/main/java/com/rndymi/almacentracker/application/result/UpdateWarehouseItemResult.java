package com.rndymi.almacentracker.application.result;

public final class UpdateWarehouseItemResult {

    public enum Status {
        SUCCESS,
        VALIDATION_ERROR,
        DUPLICATE,
        NOT_FOUND,
        PERSISTENCE_ERROR
    }

    private final Status status;
    private final boolean categoryRequired;
    private final boolean codeRequired;
    private final boolean siteRequired;
    private final Throwable cause;

    private UpdateWarehouseItemResult(
            Status status,
            boolean categoryRequired,
            boolean codeRequired,
            boolean siteRequired,
            Throwable cause
    ) {
        this.status = status;
        this.categoryRequired = categoryRequired;
        this.codeRequired = codeRequired;
        this.siteRequired = siteRequired;
        this.cause = cause;
    }

    public static UpdateWarehouseItemResult success() {
        return new UpdateWarehouseItemResult(
                Status.SUCCESS,
                false,
                false,
                false,
                null
        );
    }

    public static UpdateWarehouseItemResult validationError(
            boolean categoryRequired,
            boolean codeRequired,
            boolean siteRequired
    ) {
        return new UpdateWarehouseItemResult(
                Status.VALIDATION_ERROR,
                categoryRequired,
                codeRequired,
                siteRequired,
                null
        );
    }

    public static UpdateWarehouseItemResult duplicate() {
        return new UpdateWarehouseItemResult(
                Status.DUPLICATE,
                false,
                false,
                false,
                null
        );
    }

    public static UpdateWarehouseItemResult notFound() {
        return new UpdateWarehouseItemResult(
                Status.NOT_FOUND,
                false,
                false,
                false,
                null
        );
    }

    public static UpdateWarehouseItemResult persistenceError(
            Throwable cause
    ) {
        return new UpdateWarehouseItemResult(
                Status.PERSISTENCE_ERROR,
                false,
                false,
                false,
                cause
        );
    }

    public Status getStatus() {
        return status;
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