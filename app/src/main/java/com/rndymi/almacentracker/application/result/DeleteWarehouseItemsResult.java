package com.rndymi.almacentracker.application.result;

public final class DeleteWarehouseItemsResult {

    public enum Status {
        SUCCESS,
        PARTIAL_SUCCESS,
        EMPTY_SELECTION,
        INVALID_IDS,
        NOT_FOUND,
        PERSISTENCE_ERROR
    }

    private final Status status;
    private final int requestedCount;
    private final int deletedCount;
    private final Throwable cause;

    private DeleteWarehouseItemsResult(
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

    public static DeleteWarehouseItemsResult success(
            int requestedCount
    ) {
        return new DeleteWarehouseItemsResult(
                Status.SUCCESS,
                requestedCount,
                requestedCount,
                null
        );
    }

    public static DeleteWarehouseItemsResult partialSuccess(
            int requestedCount,
            int deletedCount
    ) {
        return new DeleteWarehouseItemsResult(
                Status.PARTIAL_SUCCESS,
                requestedCount,
                deletedCount,
                null
        );
    }

    public static DeleteWarehouseItemsResult emptySelection() {
        return new DeleteWarehouseItemsResult(
                Status.EMPTY_SELECTION,
                0,
                0,
                null
        );
    }

    public static DeleteWarehouseItemsResult invalidIds(
            int requestedCount
    ) {
        return new DeleteWarehouseItemsResult(
                Status.INVALID_IDS,
                requestedCount,
                0,
                null
        );
    }

    public static DeleteWarehouseItemsResult notFound(
            int requestedCount
    ) {
        return new DeleteWarehouseItemsResult(
                Status.NOT_FOUND,
                requestedCount,
                0,
                null
        );
    }

    public static DeleteWarehouseItemsResult persistenceError(
            int requestedCount,
            Throwable cause
    ) {
        return new DeleteWarehouseItemsResult(
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