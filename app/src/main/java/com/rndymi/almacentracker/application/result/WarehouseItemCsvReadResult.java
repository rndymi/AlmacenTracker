package com.rndymi.almacentracker.application.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WarehouseItemCsvReadResult {

    private final List<WarehouseItemCsvRow> rows;
    private final int totalRows;
    private final int invalidRowCount;

    public WarehouseItemCsvReadResult(
            List<WarehouseItemCsvRow> rows,
            int totalRows,
            int invalidRowCount
    ) {
        Objects.requireNonNull(rows);

        if (totalRows < 0 || invalidRowCount < 0) {
            throw new IllegalArgumentException(
                    "CSV counters cannot be negative"
            );
        }

        this.rows = Collections.unmodifiableList(
                new ArrayList<>(rows)
        );
        this.totalRows = totalRows;
        this.invalidRowCount = invalidRowCount;
    }

    public List<WarehouseItemCsvRow> getRows() {
        return rows;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getInvalidRowCount() {
        return invalidRowCount;
    }
}