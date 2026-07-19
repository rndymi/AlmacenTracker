package com.rndymi.almacentracker.application.result;

import java.util.Objects;

public final class WarehouseBackupCsvRow {

    private final int rowNumber;
    private final String formatVersion;
    private final String category;
    private final String code;
    private final String site;
    private final String position;
    private final String observations;
    private final String createdAt;
    private final String updatedAt;

    public WarehouseBackupCsvRow(
            int rowNumber,
            String formatVersion,
            String category,
            String code,
            String site,
            String position,
            String observations,
            String createdAt,
            String updatedAt
    ) {
        if (rowNumber < 2) {
            throw new IllegalArgumentException(
                    "Backup data row number must be at least 2"
            );
        }

        this.rowNumber = rowNumber;
        this.formatVersion = Objects.requireNonNull(formatVersion);
        this.category = Objects.requireNonNull(category);
        this.code = Objects.requireNonNull(code);
        this.site = Objects.requireNonNull(site);
        this.position = position;
        this.observations = observations;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public String getFormatVersion() {
        return formatVersion;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getSite() {
        return site;
    }

    public String getPosition() {
        return position;
    }

    public String getObservations() {
        return observations;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}