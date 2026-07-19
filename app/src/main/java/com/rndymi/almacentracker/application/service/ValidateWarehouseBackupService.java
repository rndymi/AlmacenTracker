package com.rndymi.almacentracker.application.service;

import com.rndymi.almacentracker.application.port.in.ValidateWarehouseBackupUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseBackupCsvReader;
import com.rndymi.almacentracker.application.result.WarehouseBackupCsvRow;
import com.rndymi.almacentracker.application.result.WarehouseBackupReadResult;
import com.rndymi.almacentracker.application.result.WarehouseBackupValidationResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class ValidateWarehouseBackupService
        implements ValidateWarehouseBackupUseCase {

    private final WarehouseBackupCsvReader backupCsvReader;

    public ValidateWarehouseBackupService(
            WarehouseBackupCsvReader backupCsvReader
    ) {
        this.backupCsvReader =
                Objects.requireNonNull(backupCsvReader);
    }

    @Override
    public void validateBackup(
            String sourceReference,
            Callback callback
    ) {
        Objects.requireNonNull(callback);

        if (sourceReference == null
                || sourceReference.trim().isEmpty()) {
            callback.onResult(
                    WarehouseBackupValidationResult.failure(
                            WarehouseBackupValidationResult
                                    .Status.INVALID_SOURCE,
                            0,
                            "Backup source is invalid",
                            null
                    )
            );
            return;
        }

        backupCsvReader.readBackup(
                sourceReference,
                readResult ->
                        handleReadResult(
                                readResult,
                                callback
                        )
        );
    }

    private void handleReadResult(
            WarehouseBackupReadResult readResult,
            Callback callback
    ) {
        switch (readResult.getStatus()) {
            case SUCCESS:
                validateRows(
                        readResult.getRows(),
                        callback
                );
                break;

            case INCOMPATIBLE_VERSION:
                callback.onResult(
                        WarehouseBackupValidationResult.failure(
                                WarehouseBackupValidationResult
                                        .Status
                                        .INCOMPATIBLE_VERSION,
                                0,
                                "Unsupported backup version",
                                null
                        )
                );
                break;

            case INVALID_FORMAT:
                callback.onResult(
                        WarehouseBackupValidationResult.failure(
                                WarehouseBackupValidationResult
                                        .Status.INVALID_FORMAT,
                                0,
                                "Invalid backup format",
                                readResult.getCause()
                        )
                );
                break;

            case READ_ERROR:
            default:
                callback.onResult(
                        WarehouseBackupValidationResult.failure(
                                WarehouseBackupValidationResult
                                        .Status.READ_ERROR,
                                0,
                                "Backup could not be read",
                                readResult.getCause()
                        )
                );
                break;
        }
    }

    private void validateRows(
            List<WarehouseBackupCsvRow> rows,
            Callback callback
    ) {
        try {
            List<WarehouseItem> warehouseItems =
                    new ArrayList<>(rows.size());

            Set<String> functionalIdentities =
                    new HashSet<>();

            for (WarehouseBackupCsvRow row : rows) {
                ValidationValues values =
                        validateAndNormalize(row);

                String identity =
                        values.category
                                + '\u0000'
                                + values.code;

                if (!functionalIdentities.add(identity)) {
                    callback.onResult(
                            WarehouseBackupValidationResult.failure(
                                    WarehouseBackupValidationResult
                                            .Status.DUPLICATE_DATA,
                                    row.getRowNumber(),
                                    values.category
                                            + " + "
                                            + values.code,
                                    null
                            )
                    );
                    return;
                }

                warehouseItems.add(
                        new WarehouseItem(
                                0L,
                                values.category,
                                values.code,
                                values.site,
                                values.position,
                                values.observations,
                                values.createdAt,
                                values.updatedAt
                        )
                );
            }

            callback.onResult(
                    WarehouseBackupValidationResult.valid(
                            warehouseItems
                    )
            );
        } catch (InvalidBackupRowException exception) {
            callback.onResult(
                    WarehouseBackupValidationResult.failure(
                            WarehouseBackupValidationResult
                                    .Status.INVALID_DATA,
                            exception.rowNumber,
                            exception.getMessage(),
                            exception
                    )
            );
        } catch (RuntimeException exception) {
            callback.onResult(
                    WarehouseBackupValidationResult.failure(
                            WarehouseBackupValidationResult
                                    .Status.UNKNOWN_ERROR,
                            0,
                            "Unexpected validation error",
                            exception
                    )
            );
        }
    }

    private ValidationValues validateAndNormalize(
            WarehouseBackupCsvRow row
    ) {
        String category = normalizeRequired(
                row.getCategory(),
                "Category",
                row.getRowNumber()
        );

        String code = normalizeRequired(
                row.getCode(),
                "Code",
                row.getRowNumber()
        );

        String site = normalizeRequired(
                row.getSite(),
                "Site",
                row.getRowNumber()
        );

        long createdAt = parsePositiveDate(
                row.getCreatedAt(),
                "created_at",
                row.getRowNumber()
        );

        long updatedAt = parsePositiveDate(
                row.getUpdatedAt(),
                "updated_at",
                row.getRowNumber()
        );

        if (updatedAt < createdAt) {
            throw new InvalidBackupRowException(
                    row.getRowNumber(),
                    "updated_at cannot be earlier than created_at"
            );
        }

        return new ValidationValues(
                category,
                code,
                site,
                normalizeOptional(row.getPosition()),
                normalizeOptional(row.getObservations()),
                createdAt,
                updatedAt
        );
    }

    private String normalizeRequired(
            String value,
            String fieldName,
            int rowNumber
    ) {
        String normalized =
                value == null ? "" : value.trim();

        if (normalized.isEmpty()) {
            throw new InvalidBackupRowException(
                    rowNumber,
                    fieldName + " cannot be empty"
            );
        }

        return normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private long parsePositiveDate(
            String value,
            String fieldName,
            int rowNumber
    ) {
        final long parsedValue;

        try {
            parsedValue = Long.parseLong(
                    value == null ? "" : value.trim()
            );
        } catch (NumberFormatException exception) {
            throw new InvalidBackupRowException(
                    rowNumber,
                    fieldName + " must be a valid number"
            );
        }

        if (parsedValue <= 0) {
            throw new InvalidBackupRowException(
                    rowNumber,
                    fieldName + " must be greater than zero"
            );
        }

        return parsedValue;
    }

    private static final class ValidationValues {

        private final String category;
        private final String code;
        private final String site;
        private final String position;
        private final String observations;
        private final long createdAt;
        private final long updatedAt;

        private ValidationValues(
                String category,
                String code,
                String site,
                String position,
                String observations,
                long createdAt,
                long updatedAt
        ) {
            this.category = category;
            this.code = code;
            this.site = site;
            this.position = position;
            this.observations = observations;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
    }

    private static final class InvalidBackupRowException
            extends IllegalArgumentException {

        private final int rowNumber;

        private InvalidBackupRowException(
                int rowNumber,
                String message
        ) {
            super(message);
            this.rowNumber = rowNumber;
        }
    }
}