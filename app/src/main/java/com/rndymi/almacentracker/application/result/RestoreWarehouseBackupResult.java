package com.rndymi.almacentracker.application.result;

public final class RestoreWarehouseBackupResult {

    public enum Status {
        SUCCESS,
        INVALID_BACKUP,
        DUPLICATE_DATA,
        PERSISTENCE_ERROR,
        UNKNOWN_ERROR
    }

    private final Status status;
    private final int restoredCount;
    private final Throwable cause;

    private RestoreWarehouseBackupResult(
            Status status,
            int restoredCount,
            Throwable cause
    ) {
        this.status = status;
        this.restoredCount = restoredCount;
        this.cause = cause;
    }

    public static RestoreWarehouseBackupResult success(
            int restoredCount
    ) {
        return new RestoreWarehouseBackupResult(
                Status.SUCCESS,
                restoredCount,
                null
        );
    }

    public static RestoreWarehouseBackupResult failure(
            Status status,
            Throwable cause
    ) {
        if (status == Status.SUCCESS) {
            throw new IllegalArgumentException(
                    "Use success() for successful restoration"
            );
        }

        return new RestoreWarehouseBackupResult(
                status,
                0,
                cause
        );
    }

    public Status getStatus() {
        return status;
    }

    public int getRestoredCount() {
        return restoredCount;
    }

    public Throwable getCause() {
        return cause;
    }
}