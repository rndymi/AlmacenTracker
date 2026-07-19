package com.rndymi.almacentracker.application.result;

import java.util.Objects;

public final class ImportWarehouseItemIssue {

    private final int rowNumber;
    private final ImportIssueType type;
    private final String category;
    private final String code;
    private final String message;
    private final Integer relatedRowNumber;

    public ImportWarehouseItemIssue(
            int rowNumber,
            ImportIssueType type,
            String category,
            String code,
            String message,
            Integer relatedRowNumber
    ) {
        if (rowNumber < 2) {
            throw new IllegalArgumentException(
                    "Import issue row number must identify a data row"
            );
        }

        if (relatedRowNumber != null
                && relatedRowNumber < 2) {
            throw new IllegalArgumentException(
                    "Related row number must identify a data row"
            );
        }

        this.rowNumber = rowNumber;
        this.type = Objects.requireNonNull(type);
        this.category = normalizeDisplayValue(category);
        this.code = normalizeDisplayValue(code);
        this.message = Objects.requireNonNull(message);
        this.relatedRowNumber = relatedRowNumber;
    }

    public static ImportWarehouseItemIssue invalidColumnCount(
            int rowNumber,
            int actualColumnCount,
            int expectedColumnCount
    ) {
        return new ImportWarehouseItemIssue(
                rowNumber,
                ImportIssueType.INVALID_COLUMN_COUNT,
                "",
                "",
                "La fila contiene "
                        + actualColumnCount
                        + " columnas; se esperaban "
                        + expectedColumnCount
                        + ".",
                null
        );
    }

    public static ImportWarehouseItemIssue missingCategory(
            int rowNumber,
            String code
    ) {
        return new ImportWarehouseItemIssue(
                rowNumber,
                ImportIssueType.MISSING_CATEGORY,
                "",
                code,
                "La categoría es obligatoria.",
                null
        );
    }

    public static ImportWarehouseItemIssue missingCode(
            int rowNumber,
            String category
    ) {
        return new ImportWarehouseItemIssue(
                rowNumber,
                ImportIssueType.MISSING_CODE,
                category,
                "",
                "El código es obligatorio.",
                null
        );
    }

    public static ImportWarehouseItemIssue missingSite(
            int rowNumber,
            String category,
            String code
    ) {
        return new ImportWarehouseItemIssue(
                rowNumber,
                ImportIssueType.MISSING_SITE,
                category,
                code,
                "El sitio es obligatorio.",
                null
        );
    }

    public static ImportWarehouseItemIssue duplicateExisting(
            int rowNumber,
            String category,
            String code
    ) {
        return new ImportWarehouseItemIssue(
                rowNumber,
                ImportIssueType.DUPLICATE_EXISTING,
                category,
                code,
                buildIdentity(category, code)
                        + " ya existe en AlmacenTracker.",
                null
        );
    }

    public static ImportWarehouseItemIssue duplicateInFile(
            int rowNumber,
            String category,
            String code,
            int relatedRowNumber
    ) {
        return new ImportWarehouseItemIssue(
                rowNumber,
                ImportIssueType.DUPLICATE_IN_FILE,
                category,
                code,
                buildIdentity(category, code)
                        + " ya apareció en la fila "
                        + relatedRowNumber
                        + ".",
                relatedRowNumber
        );
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public ImportIssueType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Integer getRelatedRowNumber() {
        return relatedRowNumber;
    }

    public boolean isDuplicate() {
        return type == ImportIssueType.DUPLICATE_EXISTING
                || type == ImportIssueType.DUPLICATE_IN_FILE;
    }

    public boolean isInvalid() {
        return type == ImportIssueType.INVALID_COLUMN_COUNT
                || type == ImportIssueType.MISSING_CATEGORY
                || type == ImportIssueType.MISSING_CODE
                || type == ImportIssueType.MISSING_SITE;
    }

    private static String buildIdentity(
            String category,
            String code
    ) {
        String normalizedCategory =
                normalizeDisplayValue(category);

        String normalizedCode =
                normalizeDisplayValue(code);

        if (normalizedCategory.isEmpty()
                && normalizedCode.isEmpty()) {
            return "La combinación";
        }

        if (normalizedCategory.isEmpty()) {
            return normalizedCode;
        }

        if (normalizedCode.isEmpty()) {
            return normalizedCategory;
        }

        return normalizedCategory + " + " + normalizedCode;
    }

    private static String normalizeDisplayValue(
            String value
    ) {
        return value == null
                ? ""
                : value.trim();
    }
}