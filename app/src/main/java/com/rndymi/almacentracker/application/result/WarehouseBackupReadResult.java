package com.rndymi.almacentracker.application.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WarehouseBackupReadResult {

    public enum Status {
        SUCCESS,
        INVALID_FORMAT,
        INCOMPATIBLE_VERSION,
        READ_ERROR
    }

    private final Status status;
    private final List<WarehouseBackupCsvRow> rows;
    private final Throwable cause;

    private WarehouseBackupReadResult(
            Status status,
            List<WarehouseBackupCsvRow> rows,
            Throwable cause
    ) {
        this.status = Objects.requireNonNull(status);
        this.rows = Collections.unmodifiableList(
                new ArrayList<>(
                        rows == null
                                ? Collections.emptyList()
                                : rows
                )
        );
        this.cause = cause;
    }

    public static WarehouseBackupReadResult success(
            List<WarehouseBackupCsvRow> rows
    ) {
        return new WarehouseBackupReadResult(
                Status.SUCCESS,
                rows,
                null
        );
    }

    public static WarehouseBackupReadResult invalidFormat(
            Throwable cause
    ) {
        return new WarehouseBackupReadResult(
                Status.INVALID_FORMAT,
                Collections.emptyList(),
                cause
        );
    }

    public static WarehouseBackupReadResult incompatibleVersion() {
        return new WarehouseBackupReadResult(
                Status.INCOMPATIBLE_VERSION,
                Collections.emptyList(),
                null
        );
    }

    public static WarehouseBackupReadResult readError(
            Throwable cause
    ) {
        return new WarehouseBackupReadResult(
                Status.READ_ERROR,
                Collections.emptyList(),
                cause
        );
    }

    public Status getStatus() {
        return status;
    }

    public List<WarehouseBackupCsvRow> getRows() {
        return rows;
    }

    public Throwable getCause() {
        return cause;
    }
}