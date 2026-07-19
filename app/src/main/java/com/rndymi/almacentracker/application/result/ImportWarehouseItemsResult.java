package com.rndymi.almacentracker.application.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ImportWarehouseItemsResult {

    public enum Status {
        SUCCESS,
        PARTIAL_SUCCESS,
        NO_VALID_ROWS,
        INVALID_SOURCE,
        INVALID_FORMAT,
        READ_ERROR,
        PERSISTENCE_ERROR,
        UNKNOWN_ERROR
    }

    private final Status status;
    private final int totalRows;
    private final int importedCount;
    private final List<ImportWarehouseItemIssue> issues;

    private ImportWarehouseItemsResult(
            Status status,
            int totalRows,
            int importedCount,
            List<ImportWarehouseItemIssue> issues
    ) {
        this.status = Objects.requireNonNull(status);

        if (totalRows < 0 || importedCount < 0) {
            throw new IllegalArgumentException(
                    "Import counters cannot be negative"
            );
        }

        if (importedCount > totalRows) {
            throw new IllegalArgumentException(
                    "Imported count cannot exceed total rows"
            );
        }

        Objects.requireNonNull(issues);

        List<ImportWarehouseItemIssue> sortedIssues =
                new ArrayList<>(issues);

        sortedIssues.sort(
                (first, second) -> {
                    int rowComparison = Integer.compare(
                            first.getRowNumber(),
                            second.getRowNumber()
                    );

                    if (rowComparison != 0) {
                        return rowComparison;
                    }

                    return first.getType().compareTo(
                            second.getType()
                    );
                }
        );

        this.totalRows = totalRows;
        this.importedCount = importedCount;
        this.issues = Collections.unmodifiableList(
                sortedIssues
        );
    }

    public static ImportWarehouseItemsResult completed(
            int totalRows,
            int importedCount,
            List<ImportWarehouseItemIssue> issues
    ) {
        Objects.requireNonNull(issues);

        Status status;

        if (importedCount == 0) {
            status = Status.NO_VALID_ROWS;
        } else if (issues.isEmpty()) {
            status = Status.SUCCESS;
        } else {
            status = Status.PARTIAL_SUCCESS;
        }

        return new ImportWarehouseItemsResult(
                status,
                totalRows,
                importedCount,
                issues
        );
    }

    public static ImportWarehouseItemsResult persistenceError(
            int totalRows,
            List<ImportWarehouseItemIssue> issues
    ) {
        return new ImportWarehouseItemsResult(
                Status.PERSISTENCE_ERROR,
                totalRows,
                0,
                issues
        );
    }

    public static ImportWarehouseItemsResult of(
            Status status
    ) {
        return new ImportWarehouseItemsResult(
                status,
                0,
                0,
                Collections.emptyList()
        );
    }

    public Status getStatus() {
        return status;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public List<ImportWarehouseItemIssue> getIssues() {
        return issues;
    }

    public int getIssueCount() {
        return issues.size();
    }

    public int getDuplicateCount() {
        Set<Integer> duplicateRows = new HashSet<>();

        for (ImportWarehouseItemIssue issue : issues) {
            if (issue.isDuplicate()) {
                duplicateRows.add(
                        issue.getRowNumber()
                );
            }
        }

        return duplicateRows.size();
    }

    public int getInvalidCount() {
        Set<Integer> invalidRows = new HashSet<>();

        for (ImportWarehouseItemIssue issue : issues) {
            if (issue.isInvalid()) {
                invalidRows.add(
                        issue.getRowNumber()
                );
            }
        }

        return invalidRows.size();
    }

    public boolean hasIssues() {
        return !issues.isEmpty();
    }
}