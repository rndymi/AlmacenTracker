package com.rndymi.almacentracker.application.result;

public final class ExportWarehouseItemsResult {

    public enum Status {
        SUCCESS,
        EMPTY_DATABASE,
        INVALID_DESTINATION,
        READ_ERROR,
        SERIALIZATION_ERROR,
        WRITE_ERROR,
        UNKNOWN_ERROR
    }

    private final Status status;
    private final int exportedCount;

    private ExportWarehouseItemsResult(
            Status status,
            int exportedCount
    ) {
        this.status = status;
        this.exportedCount = exportedCount;
    }

    public static ExportWarehouseItemsResult success(int exportedCount) {
        return new ExportWarehouseItemsResult(
                Status.SUCCESS,
                exportedCount
        );
    }

    public static ExportWarehouseItemsResult of(Status status) {
        return new ExportWarehouseItemsResult(status, 0);
    }

    public Status getStatus() {
        return status;
    }

    public int getExportedCount() {
        return exportedCount;
    }
}