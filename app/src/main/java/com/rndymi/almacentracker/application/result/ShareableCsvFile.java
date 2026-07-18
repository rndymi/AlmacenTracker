package com.rndymi.almacentracker.application.result;

import java.util.Objects;

public final class ShareableCsvFile {

    private final String contentReference;
    private final String fileName;
    private final String mimeType;
    private final int recordCount;

    public ShareableCsvFile(
            String contentReference,
            String fileName,
            String mimeType,
            int recordCount
    ) {
        this.contentReference = requireText(
                contentReference,
                "contentReference"
        );
        this.fileName = requireText(fileName, "fileName");
        this.mimeType = requireText(mimeType, "mimeType");

        if (recordCount <= 0) {
            throw new IllegalArgumentException(
                    "recordCount must be greater than zero"
            );
        }

        this.recordCount = recordCount;
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        Objects.requireNonNull(value, fieldName);

        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }

        return value;
    }

    public String getContentReference() {
        return contentReference;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getRecordCount() {
        return recordCount;
    }
}