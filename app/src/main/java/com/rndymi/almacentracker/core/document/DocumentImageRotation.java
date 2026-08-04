package com.rndymi.almacentracker.core.document;

public final class DocumentImageRotation {

    private static final int FULL_ROTATION_DEGREES = 360;
    private static final int QUARTER_ROTATION_DEGREES = 90;

    private DocumentImageRotation() {
    }

    public static int normalize(int rotationDegrees) {
        int normalized =
                (
                        rotationDegrees
                                % FULL_ROTATION_DEGREES
                                + FULL_ROTATION_DEGREES
                )
                        % FULL_ROTATION_DEGREES;

        if (normalized
                % QUARTER_ROTATION_DEGREES
                != 0) {
            throw new IllegalArgumentException(
                    "Rotation must be a multiple of 90 degrees"
            );
        }

        return normalized;
    }

    public static int rotateLeft(int currentRotationDegrees) {
        return normalize(
                currentRotationDegrees
                        - QUARTER_ROTATION_DEGREES
        );
    }

    public static int rotateRight(int currentRotationDegrees) {
        return normalize(
                currentRotationDegrees
                        + QUARTER_ROTATION_DEGREES
        );
    }

    public static int combine(
            int exifRotationDegrees,
            int manualRotationDegrees
    ) {
        return normalize(
                normalize(exifRotationDegrees)
                        + normalize(manualRotationDegrees)
        );
    }
}
