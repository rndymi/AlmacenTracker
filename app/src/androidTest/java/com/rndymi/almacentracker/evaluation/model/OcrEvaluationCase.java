package com.rndymi.almacentracker.evaluation.model;

import java.util.Objects;

public final class OcrEvaluationCase {

    private final String id;
    private final String imagePath;
    private final String expectedPath;
    private final String description;
    private final String sourceType;
    private final String documentType;
    private final String lighting;
    private final String perspective;
    private final String group;
    private final int expectedOrientationDegrees;
    private final int columnCount;
    private final boolean handwritten;
    private final boolean publicRepositoryAllowed;
    private final boolean containsPrivateData;
    private final String source;
    private final String license;

    public OcrEvaluationCase(
            String id,
            String imagePath,
            String expectedPath,
            String description,
            String sourceType,
            String documentType,
            String lighting,
            String perspective,
            String group,
            int expectedOrientationDegrees,
            int columnCount,
            boolean handwritten,
            boolean publicRepositoryAllowed,
            boolean containsPrivateData,
            String source,
            String license
    ) {
        this.id = requireText(id, "id");
        this.imagePath =
                requireText(
                        imagePath,
                        "imagePath"
                );
        this.expectedPath =
                requireText(
                        expectedPath,
                        "expectedPath"
                );
        this.description =
                requireText(
                        description,
                        "description"
                );
        this.sourceType =
                requireText(
                        sourceType,
                        "sourceType"
                );
        this.documentType =
                requireText(
                        documentType,
                        "documentType"
                );
        this.lighting =
                requireText(
                        lighting,
                        "lighting"
                );
        this.perspective =
                requireText(
                        perspective,
                        "perspective"
                );
        this.group = requireText(group, "group");

        if (expectedOrientationDegrees != 0
                && expectedOrientationDegrees != 90
                && expectedOrientationDegrees != 180
                && expectedOrientationDegrees != 270) {
            throw new IllegalArgumentException(
                    "expectedOrientationDegrees must be 0, 90, 180 or 270"
            );
        }

        if (columnCount < 1) {
            throw new IllegalArgumentException(
                    "columnCount must be positive"
            );
        }

        this.expectedOrientationDegrees =
                expectedOrientationDegrees;
        this.columnCount = columnCount;
        this.handwritten = handwritten;
        this.publicRepositoryAllowed =
                publicRepositoryAllowed;
        this.containsPrivateData =
                containsPrivateData;
        this.source = requireText(source, "source");
        this.license =
                requireText(license, "license");

        if (publicRepositoryAllowed
                && containsPrivateData) {
            throw new IllegalArgumentException(
                    "Public cases cannot contain private data"
            );
        }
    }

    public String getId() {
        return id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getExpectedPath() {
        return expectedPath;
    }

    public String getDescription() {
        return description;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getLighting() {
        return lighting;
    }

    public String getPerspective() {
        return perspective;
    }

    public String getGroup() {
        return group;
    }

    public int getExpectedOrientationDegrees() {
        return expectedOrientationDegrees;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public boolean isHandwritten() {
        return handwritten;
    }

    public boolean isPublicRepositoryAllowed() {
        return publicRepositoryAllowed;
    }

    public boolean containsPrivateData() {
        return containsPrivateData;
    }

    public String getSource() {
        return source;
    }

    public String getLicense() {
        return license;
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
