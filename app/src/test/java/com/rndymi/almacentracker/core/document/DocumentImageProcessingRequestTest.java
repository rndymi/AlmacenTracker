package com.rndymi.almacentracker.core.document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public final class DocumentImageProcessingRequestTest {

    @Test
    public void constructorNormalizesUriAndRotation() {
        DocumentImageProcessingRequest request =
                new DocumentImageProcessingRequest(
                        " content://images/document ",
                        -90
                );

        assertEquals(
                "content://images/document",
                request.getImageUri()
        );

        assertEquals(
                270,
                request.getManualRotationDegrees()
        );
    }

    @Test
    public void constructorRejectsEmptyUri() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DocumentImageProcessingRequest(
                        "   ",
                        0
                )
        );
    }

    @Test
    public void constructorRejectsUnsupportedRotation() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DocumentImageProcessingRequest(
                        "content://images/document",
                        45
                )
        );
    }
}
