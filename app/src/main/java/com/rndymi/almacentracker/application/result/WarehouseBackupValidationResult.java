package com.rndymi.almacentracker.application.result;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WarehouseBackupValidationResult {

    public enum Status {
        VALID,
        INVALID_SOURCE,
        INVALID_FORMAT,
        INCOMPATIBLE_VERSION,
        INVALID_DATA,
        DUPLICATE_DATA,
        READ_ERROR,
        UNKNOWN_ERROR
    }

    private final Status status;
    private final List<WarehouseItem> warehouseItems;
    private final int invalidRowNumber;
    private final String detail;
    private final Throwable cause;

    private WarehouseBackupValidationResult(
            Status status,
            List<WarehouseItem> warehouseItems,
            int invalidRowNumber,
            String detail,
            Throwable cause
    ) {
        this.status = Objects.requireNonNull(status);
        this.warehouseItems = Collections.unmodifiableList(
                new ArrayList<>(
                        warehouseItems == null
                                ? Collections.emptyList()
                                : warehouseItems
                )
        );
        this.invalidRowNumber = invalidRowNumber;
        this.detail = detail;
        this.cause = cause;
    }

    public static WarehouseBackupValidationResult valid(
            List<WarehouseItem> warehouseItems
    ) {
        return new WarehouseBackupValidationResult(
                Status.VALID,
                warehouseItems,
                0,
                null,
                null
        );
    }

    public static WarehouseBackupValidationResult failure(
            Status status,
            int invalidRowNumber,
            String detail,
            Throwable cause
    ) {
        if (status == Status.VALID) {
            throw new IllegalArgumentException(
                    "Use valid() for a valid backup"
            );
        }

        return new WarehouseBackupValidationResult(
                status,
                Collections.emptyList(),
                invalidRowNumber,
                detail,
                cause
        );
    }

    public Status getStatus() {
        return status;
    }

    public List<WarehouseItem> getWarehouseItems() {
        return warehouseItems;
    }

    public int getRestorableCount() {
        return warehouseItems.size();
    }

    public int getInvalidRowNumber() {
        return invalidRowNumber;
    }

    public String getDetail() {
        return detail;
    }

    public Throwable getCause() {
        return cause;
    }

    public boolean isValid() {
        return status == Status.VALID;
    }
}