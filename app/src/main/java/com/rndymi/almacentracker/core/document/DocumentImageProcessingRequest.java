package com.rndymi.almacentracker.core.document;

import java.util.Objects;

public final class DocumentImageProcessingRequest {

    private final String imageUri;
    private final int manualRotationDegrees;

    public DocumentImageProcessingRequest(
            String imageUri,
            int manualRotationDegrees
    ) {
        String normalizedImageUri =
                Objects.requireNonNull(
                        imageUri,
                        "imageUri"
                ).trim();

        if (normalizedImageUri.isEmpty()) {
            throw new IllegalArgumentException(
                    "imageUri cannot be empty"
            );
        }

        this.imageUri = normalizedImageUri;
        this.manualRotationDegrees =
                DocumentImageRotation.normalize(
                        manualRotationDegrees
                );
    }

    public String getImageUri() {
        return imageUri;
    }

    public int getManualRotationDegrees() {
        return manualRotationDegrees;
    }
}
