package com.rndymi.almacentracker.application.result;

import java.util.Objects;

public final class ShareWarehouseItemsResult {

    public enum Status {
        SUCCESS,
        EMPTY_DATABASE,
        READ_ERROR,
        SERIALIZATION_ERROR,
        TEMP_FILE_ERROR,
        FILE_PROVIDER_ERROR,
        UNKNOWN_ERROR
    }

    private final Status status;
    private final ShareableCsvFile shareableFile;

    private ShareWarehouseItemsResult(
            Status status,
            ShareableCsvFile shareableFile
    ) {
        this.status = Objects.requireNonNull(status);
        this.shareableFile = shareableFile;
    }

    public static ShareWarehouseItemsResult success(
            ShareableCsvFile shareableFile
    ) {
        return new ShareWarehouseItemsResult(
                Status.SUCCESS,
                Objects.requireNonNull(shareableFile)
        );
    }

    public static ShareWarehouseItemsResult of(Status status) {
        if (status == Status.SUCCESS) {
            throw new IllegalArgumentException(
                    "Use success() for a successful result"
            );
        }

        return new ShareWarehouseItemsResult(status, null);
    }

    public Status getStatus() {
        return status;
    }

    public ShareableCsvFile getShareableFile() {
        return shareableFile;
    }
}