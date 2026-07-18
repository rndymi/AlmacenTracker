package com.rndymi.almacentracker.adapter.out.file.csv;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Arrays;
import java.util.List;

public final class WarehouseItemCsvMapper {

    public List<String> toColumns(WarehouseItem item) {
        if (item == null) {
            throw new IllegalArgumentException(
                    "WarehouseItem cannot be null"
            );
        }

        return Arrays.asList(
                required(item.getCategory(), "category"),
                required(item.getCode(), "code"),
                required(item.getSite(), "site"),
                optional(item.getPosition()),
                optional(item.getObservations())
        );
    }

    private String required(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null"
            );
        }

        return value;
    }

    private String optional(String value) {
        return value == null ? "" : value;
    }
}