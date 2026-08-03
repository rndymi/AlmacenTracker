package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class DocumentQuantityNormalizerTest {

    private final DocumentQuantityNormalizer normalizer =
            new DocumentQuantityNormalizer();

    @Test
    public void normalizePreservesNumericQuantity() {
        assertEquals(
                Integer.valueOf(5),
                normalizer.normalize("5")
        );

        assertEquals(
                Integer.valueOf(20),
                normalizer.normalize("20")
        );
    }

    @Test
    public void normalizeRecoversSAsFive() {
        assertEquals(
                Integer.valueOf(5),
                normalizer.normalize("S")
        );
    }

    @Test
    public void normalizeRecoversCommonDigitConfusions() {
        assertEquals(
                Integer.valueOf(1),
                normalizer.normalize("I")
        );

        assertEquals(
                Integer.valueOf(7),
                normalizer.normalize("J")
        );

        assertEquals(
                Integer.valueOf(10),
                normalizer.normalize("IO")
        );
    }

    @Test
    public void normalizeRejectsUnknownCharacters() {
        assertNull(
                normalizer.normalize("A")
        );

        assertNull(
                normalizer.normalize("5X")
        );
    }

    @Test
    public void normalizeRejectsZero() {
        assertNull(
                normalizer.normalize("O")
        );

        assertNull(
                normalizer.normalize("0")
        );
    }
}
