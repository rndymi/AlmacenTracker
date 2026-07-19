package com.rndymi.almacentracker.adapter.out.file.backup.csv;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Arrays;
import java.util.List;

public final class WarehouseBackupCsvMapper {

    public static final String FORMAT_VERSION = "1";

    public List<String> toColumns(
            WarehouseItem warehouseItem
    ) {
        if (warehouseItem == null) {
            throw new IllegalArgumentException(
                    "WarehouseItem cannot be null"
            );
        }

        validateDates(warehouseItem);

        return Arrays.asList(
                FORMAT_VERSION,
                required(
                        warehouseItem.getCategory(),
                        "category"
                ),
                required(
                        warehouseItem.getCode(),
                        "code"
                ),
                required(
                        warehouseItem.getSite(),
                        "site"
                ),
                optional(warehouseItem.getPosition()),
                optional(warehouseItem.getObservations()),
                String.valueOf(
                        warehouseItem.getCreatedAt()
                ),
                String.valueOf(
                        warehouseItem.getUpdatedAt()
                )
        );
    }

    private void validateDates(
            WarehouseItem warehouseItem
    ) {
        long createdAt = warehouseItem.getCreatedAt();
        long updatedAt = warehouseItem.getUpdatedAt();

        if (createdAt <= 0) {
            throw new IllegalArgumentException(
                    "createdAt must be greater than zero"
            );
        }

        if (updatedAt <= 0) {
            throw new IllegalArgumentException(
                    "updatedAt must be greater than zero"
            );
        }

        if (updatedAt < createdAt) {
            throw new IllegalArgumentException(
                    "updatedAt cannot be earlier than createdAt"
            );
        }
    }

    private String required(
            String value,
            String fieldName
    ) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be empty"
            );
        }

        return value;
    }

    private String optional(String value) {
        return value == null ? "" : value;
    }
}