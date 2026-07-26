package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public final class WarehouseReferenceParserTest {

    private WarehouseReferenceParser parser;

    @Before
    public void setUp() {
        parser =
                new WarehouseReferenceParser();
    }

    @Test
    public void parseLine_extractsCompactReference() {
        List<WarehouseReferenceMatch> matches =
                parser.parseLine(
                        0,
                        "MR1210"
                );

        assertEquals(
                1,
                matches.size()
        );

        assertEquals(
                "MR",
                matches.get(0)
                        .getReference()
                        .getCategory()
        );

        assertEquals(
                "1210",
                matches.get(0)
                        .getReference()
                        .getCode()
        );
    }

    @Test
    public void parseLine_extractsReferenceWithSpaces() {
        List<WarehouseReferenceMatch> matches =
                parser.parseLine(
                        0,
                        "MR 1210 A"
                );

        assertEquals(
                1,
                matches.size()
        );

        assertEquals(
                "1210A",
                matches.get(0)
                        .getReference()
                        .getCode()
        );
    }

    @Test
    public void parseLine_preservesLeadingZeros() {
        List<WarehouseReferenceMatch> matches =
                parser.parseLine(
                        0,
                        "MR001210"
                );

        assertEquals(
                "001210",
                matches.get(0)
                        .getReference()
                        .getCode()
        );
    }

    @Test
    public void parseLine_extractsMultipleReferencesInOrder() {
        List<WarehouseReferenceMatch> matches =
                parser.parseLine(
                        0,
                        "MR1210A / MZ1300C"
                );

        assertEquals(
                2,
                matches.size()
        );

        assertEquals(
                "MR",
                matches.get(0)
                        .getReference()
                        .getCategory()
        );

        assertEquals(
                "MZ",
                matches.get(1)
                        .getReference()
                        .getCategory()
        );
    }

    @Test
    public void parseLine_ignoresSingleLetterPrefix() {
        assertTrue(
                parser.parseLine(
                        0,
                        "M1210"
                ).isEmpty()
        );
    }

    @Test
    public void parseLine_doesNotExtractPartialThreeLetterPrefix() {
        assertTrue(
                parser.parseLine(
                        0,
                        "MRA1210"
                ).isEmpty()
        );
    }

    @Test
    public void parseLine_ignoresReferenceEmbeddedInLargerValue() {
        assertTrue(
                parser.parseLine(
                        0,
                        "XMR1210AY"
                ).isEmpty()
        );
    }

    @Test
    public void parseLine_doesNotCorrectAmbiguousOcrCharacters() {
        assertTrue(
                parser.parseLine(
                        0,
                        "MR 1O50"
                ).isEmpty()
        );
    }

    @Test
    public void parseInput_normalizesCaseAndSpaces() {
        WarehouseReference reference =
                parser.parseInput(
                        " mr ",
                        " 001210 a "
                );

        assertEquals(
                "MR",
                reference.getCategory()
        );

        assertEquals(
                "001210A",
                reference.getCode()
        );
    }

    @Test
    public void parseInput_rejectsInvalidCategory() {
        assertNull(
                parser.parseInput(
                        "M",
                        "1210"
                )
        );

        assertFalse(
                parser.isValidCategory(
                        "M"
                )
        );
    }

    @Test
    public void parseInput_rejectsInvalidCode() {
        assertNull(
                parser.parseInput(
                        "MR",
                        "12AB"
                )
        );

        assertFalse(
                parser.isValidCode(
                        "12AB"
                )
        );
    }
}