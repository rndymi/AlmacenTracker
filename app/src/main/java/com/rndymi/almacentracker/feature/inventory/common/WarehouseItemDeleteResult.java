package com.rndymi.almacentracker.feature.inventory.common;

public final class WarehouseItemDeleteResult {

    public enum Status {
        SUCCESS,
        PARTIAL_SUCCESS,
        EMPTY_SELECTION,
        INVALID_ID,
        NOT_FOUND,
        PERSISTENCE_ERROR
    }

    private final Status status;
    private final int requestedCount;
    private final int deletedCount;
    private final Throwable cause;

    private WarehouseItemDeleteResult(
            Status status,
            int requestedCount,
            int deletedCount,
            Throwable cause
    ) {
        this.status = status;
        this.requestedCount = requestedCount;
        this.deletedCount = deletedCount;
        this.cause = cause;
    }

    public static WarehouseItemDeleteResult success(
            int requestedCount
    ) {
        return new WarehouseItemDeleteResult(
                Status.SUCCESS,
                requestedCount,
                requestedCount,
                null
        );
    }

    public static WarehouseItemDeleteResult partialSuccess(
            int requestedCount,
            int deletedCount
    ) {
        return new WarehouseItemDeleteResult(
                Status.PARTIAL_SUCCESS,
                requestedCount,
                deletedCount,
                null
        );
    }

    public static WarehouseItemDeleteResult emptySelection() {
        return new WarehouseItemDeleteResult(
                Status.EMPTY_SELECTION,
                0,
                0,
                null
        );
    }

    public static WarehouseItemDeleteResult invalidId(
            int requestedCount
    ) {
        return new WarehouseItemDeleteResult(
                Status.INVALID_ID,
                requestedCount,
                0,
                null
        );
    }

    public static WarehouseItemDeleteResult notFound(
            int requestedCount
    ) {
        return new WarehouseItemDeleteResult(
                Status.NOT_FOUND,
                requestedCount,
                0,
                null
        );
    }

    public static WarehouseItemDeleteResult persistenceError(
            int requestedCount,
            Throwable cause
    ) {
        return new WarehouseItemDeleteResult(
                Status.PERSISTENCE_ERROR,
                requestedCount,
                0,
                cause
        );
    }

    public Status getStatus() {
        return status;
    }

    public int getRequestedCount() {
        return requestedCount;
    }

    public int getDeletedCount() {
        return deletedCount;
    }

    public Throwable getCause() {
        return cause;
    }
}
