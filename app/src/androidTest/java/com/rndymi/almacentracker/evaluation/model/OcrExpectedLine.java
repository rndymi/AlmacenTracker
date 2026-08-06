package com.rndymi.almacentracker.evaluation.model;

import java.util.Objects;

public final class OcrExpectedLine {

    private final int lineIndex;
    private final String text;
    private final Integer columnIndex;
    private final boolean globalLine;

    public OcrExpectedLine(
            int lineIndex,
            String text,
            Integer columnIndex,
            boolean globalLine
    ) {
        if (lineIndex < 0) {
            throw new IllegalArgumentException(
                    "lineIndex cannot be negative"
            );
        }

        if (columnIndex != null
                && columnIndex < 0) {
            throw new IllegalArgumentException(
                    "columnIndex cannot be negative"
            );
        }

        this.lineIndex = lineIndex;
        this.text = requireText(text, "text");
        this.columnIndex = columnIndex;
        this.globalLine = globalLine;
    }

    public int getLineIndex() {
        return lineIndex;
    }

    public String getText() {
        return text;
    }

    public Integer getColumnIndex() {
        return columnIndex;
    }

    public boolean isGlobalLine() {
        return globalLine;
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        Objects.requireNonNull(value, fieldName);

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }

        return trimmed;
    }
}
