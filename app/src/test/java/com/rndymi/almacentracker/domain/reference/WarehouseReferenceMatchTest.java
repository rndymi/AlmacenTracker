package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class WarehouseReferenceMatchTest {

    @Test
    public void constructorWithoutResolutionUsesObservedReference() {
        WarehouseReference observed =
                new WarehouseReference(
                        "MK",
                        "866S"
                );

        WarehouseReferenceMatch match =
                new WarehouseReferenceMatch(
                        observed,
                        0,
                        "MK866S",
                        0
                );

        assertEquals(
                observed,
                match.getReference()
        );

        assertEquals(
                observed,
                match.getObservedReference()
        );

        assertFalse(
                match.hasResolvedReference()
        );
    }

    @Test
    public void withResolvedReferencePreservesObservedReference() {
        WarehouseReference observed =
                new WarehouseReference(
                        "MK",
                        "866S"
                );

        WarehouseReference resolved =
                new WarehouseReference(
                        "MR",
                        "8665"
                );

        WarehouseReferenceMatch match =
                new WarehouseReferenceMatch(
                        observed,
                        0,
                        "MK866S",
                        0
                ).withResolvedReference(resolved);

        assertEquals(
                observed,
                match.getObservedReference()
        );

        assertEquals(
                resolved,
                match.getResolvedReference()
        );

        assertEquals(
                resolved,
                match.getReference()
        );

        assertTrue(
                match.hasResolvedReference()
        );
    }
}
