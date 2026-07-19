package com.rndymi.almacentracker.application.result;

import java.util.Objects;

public final class CreateWarehouseBackupResult {

    public enum Status {
        SUCCESS,
        INVALID_DESTINATION,
        READ_ERROR,
        INVALID_DATA,
        SERIALIZATION_ERROR,
        WRITE_ERROR,
        UNKNOWN_ERROR
    }

    private final Status status;
    private final int backedUpCount;

    private CreateWarehouseBackupResult(
            Status status,
            int backedUpCount
    ) {
        this.status = Objects.requireNonNull(status);
        this.backedUpCount = backedUpCount;
    }

    public static CreateWarehouseBackupResult success(
            int backedUpCount
    ) {
        if (backedUpCount < 0) {
            throw new IllegalArgumentException(
                    "Backed up count cannot be negative"
            );
        }

        return new CreateWarehouseBackupResult(
                Status.SUCCESS,
                backedUpCount
        );
    }

    public static CreateWarehouseBackupResult of(
            Status status
    ) {
        if (status == Status.SUCCESS) {
            throw new IllegalArgumentException(
                    "Use success() for successful results"
            );
        }

        return new CreateWarehouseBackupResult(
                status,
                0
        );
    }

    public Status getStatus() {
        return status;
    }

    public int getBackedUpCount() {
        return backedUpCount;
    }
}