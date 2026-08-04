package com.rndymi.almacentracker.core.document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public final class DocumentImageRotationTest {

    @Test
    public void normalizeKeepsSupportedRotations() {
        assertEquals(
                0,
                DocumentImageRotation.normalize(0)
        );
        assertEquals(
                90,
                DocumentImageRotation.normalize(90)
        );
        assertEquals(
                180,
                DocumentImageRotation.normalize(180)
        );
        assertEquals(
                270,
                DocumentImageRotation.normalize(270)
        );
    }

    @Test
    public void normalizeWrapsPositiveAndNegativeRotations() {
        assertEquals(
                0,
                DocumentImageRotation.normalize(360)
        );
        assertEquals(
                90,
                DocumentImageRotation.normalize(450)
        );
        assertEquals(
                270,
                DocumentImageRotation.normalize(-90)
        );
    }

    @Test
    public void normalizeRejectsUnsupportedRotation() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DocumentImageRotation.normalize(45)
        );
    }

    @Test
    public void rotateLeftWrapsFromZero() {
        assertEquals(
                270,
                DocumentImageRotation.rotateLeft(0)
        );
    }

    @Test
    public void rotateRightWrapsFromTwoHundredSeventy() {
        assertEquals(
                0,
                DocumentImageRotation.rotateRight(270)
        );
    }

    @Test
    public void combineExifAndManualRotation() {
        assertEquals(
                180,
                DocumentImageRotation.combine(
                        90,
                        90
                )
        );

        assertEquals(
                0,
                DocumentImageRotation.combine(
                        90,
                        270
                )
        );
    }

    @Test
    public void fourRightRotationsReturnToInitialValue() {
        int rotation = 0;

        for (int index = 0; index < 4; index++) {
            rotation =
                    DocumentImageRotation.rotateRight(
                            rotation
                    );
        }

        assertEquals(0, rotation);
    }
}
