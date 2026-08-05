package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class DocumentUnitNormalizerTest {

    private final DocumentUnitNormalizer normalizer =
            new DocumentUnitNormalizer();

    @Test
    public void normalizePreservesCleanUnits() {
        assertEquals(
                "P",
                normalizer.normalize("p")
        );

        assertEquals(
                "PCS",
                normalizer.normalize("pcs")
        );

        assertEquals(
                "PQT",
                normalizer.normalize("pqt")
        );

        assertEquals(
                "PQTS",
                normalizer.normalize("pqts")
        );
    }

    @Test
    public void normalizeCorrectsPc5() {
        assertEquals(
                "PCS",
                normalizer.normalize("pc5")
        );
    }

    @Test
    public void normalizeCorrectsP9fs() {
        assertEquals(
                "PQTS",
                normalizer.normalize("p9fs")
        );
    }

    @Test
    public void normalizeDoesNotPluralizePqt() {
        assertEquals(
                "PQT",
                normalizer.normalize("pqt")
        );
    }

    @Test
    public void normalizePreservesOtherAlphabeticUnits() {
        assertEquals(
                "BULTOS",
                normalizer.normalize("bultos")
        );
    }

    @Test
    public void normalizeRejectsUnknownAlphanumericUnit() {
        assertNull(
                normalizer.normalize("abc7")
        );
    }
}
