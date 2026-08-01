package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class DocumentReferenceDataParserTest {

    private final DocumentReferenceDataParser parser =
            new DocumentReferenceDataParser();

    @Test
    public void parse_extractsQuantityAndKnownUnit() {
        WarehouseReferenceMatch match =
                new WarehouseReferenceMatch(
                        new WarehouseReference("MR", "1210"),
                        0,
                        "MR 1210 - 20 PCS",
                        0
                );

        DocumentReferenceData result =
                parser.parse(match);

        assertEquals(
                Integer.valueOf(20),
                result.getQuantity()
        );

        assertEquals(
                "PCS",
                result.getUnit()
        );
    }

    @Test
    public void parse_doesNotInferQuantityWithoutKnownUnit() {
        WarehouseReferenceMatch match =
                new WarehouseReferenceMatch(
                        new WarehouseReference("MR", "1210"),
                        0,
                        "MR 1210 2026",
                        0
                );

        DocumentReferenceData result =
                parser.parse(match);

        assertNull(result.getQuantity());
        assertNull(result.getUnit());
    }

    @Test
    public void parse_rejectsAmbiguousMultipleQuantities() {
        WarehouseReferenceMatch match =
                new WarehouseReferenceMatch(
                        new WarehouseReference("MR", "1210"),
                        0,
                        "MR 1210 4 CAJAS 2 PCS",
                        0
                );

        DocumentReferenceData result =
                parser.parse(match);

        assertNull(result.getQuantity());
        assertNull(result.getUnit());
    }
}