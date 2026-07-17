package com.rndymi.almacentracker.application.result;

public final class DeleteWarehouseItemResult {

    public enum Status {
        SUCCESS,
        INVALID_ID,
        NOT_FOUND,
        PERSISTENCE_ERROR
    }

    private final Status status;
    private final Throwable cause;

    private DeleteWarehouseItemResult(
            Status status,
            Throwable cause
    ) {
        this.status = status;
        this.cause = cause;
    }

    public static DeleteWarehouseItemResult success() {
        return new DeleteWarehouseItemResult(
                Status.SUCCESS,
                null
        );
    }

    public static DeleteWarehouseItemResult invalidId() {
        return new DeleteWarehouseItemResult(
                Status.INVALID_ID,
                null
        );
    }

    public static DeleteWarehouseItemResult notFound() {
        return new DeleteWarehouseItemResult(
                Status.NOT_FOUND,
                null
        );
    }

    public static DeleteWarehouseItemResult persistenceError(
            Throwable cause
    ) {
        return new DeleteWarehouseItemResult(
                Status.PERSISTENCE_ERROR,
                cause
        );
    }

    public Status getStatus() {
        return status;
    }

    public Throwable getCause() {
        return cause;
    }
}