package com.rndymi.almacentracker.application.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class WarehouseItemCsvReadResult {

    private final List<WarehouseItemCsvRow> rows;
    private final int totalRows;
    private final List<ImportWarehouseItemIssue> parsingIssues;

    public WarehouseItemCsvReadResult(
            List<WarehouseItemCsvRow> rows,
            int totalRows,
            List<ImportWarehouseItemIssue> parsingIssues
    ) {
        Objects.requireNonNull(rows);
        Objects.requireNonNull(parsingIssues);

        if (totalRows < 0) {
            throw new IllegalArgumentException(
                    "CSV total row count cannot be negative"
            );
        }

        this.rows = Collections.unmodifiableList(
                new ArrayList<>(rows)
        );

        this.totalRows = totalRows;

        List<ImportWarehouseItemIssue> sortedIssues =
                new ArrayList<>(parsingIssues);

        sortedIssues.sort(
                (first, second) -> Integer.compare(
                        first.getRowNumber(),
                        second.getRowNumber()
                )
        );

        this.parsingIssues = Collections.unmodifiableList(
                sortedIssues
        );
    }

    public List<WarehouseItemCsvRow> getRows() {
        return rows;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public List<ImportWarehouseItemIssue>
    getParsingIssues() {
        return parsingIssues;
    }

    public int getInvalidRowCount() {
        Set<Integer> invalidRows = new HashSet<>();

        for (ImportWarehouseItemIssue issue
                : parsingIssues) {
            if (issue.isInvalid()) {
                invalidRows.add(issue.getRowNumber());
            }
        }

        return invalidRows.size();
    }
}