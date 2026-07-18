package com.rndymi.almacentracker.application.result;

public final class ImportWarehouseItemsResult {

    public enum Status {
        SUCCESS,
        PARTIAL_SUCCESS,
        NO_VALID_ROWS,
        INVALID_SOURCE,
        INVALID_FORMAT,
        READ_ERROR,
        PERSISTENCE_ERROR,
        UNKNOWN_ERROR
    }

    private final Status status;
    private final int totalRows;
    private final int importedCount;
    private final int duplicateCount;
    private final int invalidCount;

    private ImportWarehouseItemsResult(
            Status status,
            int totalRows,
            int importedCount,
            int duplicateCount,
            int invalidCount
    ) {
        this.status = status;
        this.totalRows = totalRows;
        this.importedCount = importedCount;
        this.duplicateCount = duplicateCount;
        this.invalidCount = invalidCount;
    }

    public static ImportWarehouseItemsResult success(
            int totalRows,
            int importedCount,
            int duplicateCount,
            int invalidCount
    ) {
        Status status =
                duplicateCount == 0 && invalidCount == 0
                        ? Status.SUCCESS
                        : Status.PARTIAL_SUCCESS;

        return new ImportWarehouseItemsResult(
                status,
                totalRows,
                importedCount,
                duplicateCount,
                invalidCount
        );
    }

    public static ImportWarehouseItemsResult noValidRows(
            int totalRows,
            int duplicateCount,
            int invalidCount
    ) {
        return new ImportWarehouseItemsResult(
                Status.NO_VALID_ROWS,
                totalRows,
                0,
                duplicateCount,
                invalidCount
        );
    }

    public static ImportWarehouseItemsResult of(
            Status status
    ) {
        return new ImportWarehouseItemsResult(
                status,
                0,
                0,
                0,
                0
        );
    }

    public Status getStatus() {
        return status;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public int getInvalidCount() {
        return invalidCount;
    }
}