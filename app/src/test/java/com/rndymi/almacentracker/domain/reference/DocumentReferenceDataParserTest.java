package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class DocumentReferenceDataParserTest {

    private final DocumentReferenceDataParser parser =
            new DocumentReferenceDataParser();

    @Test
    public void parseExtractsCleanQuantityAndUnit() {
        DocumentReferenceData result =
                parser.parse(
                        match(
                                "MR",
                                "21570",
                                "MR 21570 - 5 pcs"
                        )
                );

        assertEquals(
                Integer.valueOf(5),
                result.getQuantity()
        );

        assertEquals(
                "PCS",
                result.getUnit()
        );
    }

    @Test
    public void parseRecoversPc5() {
        DocumentReferenceData result =
                parser.parse(
                        match(
                                "MR",
                                "21570",
                                "M221570-5pc5"
                        )
                );

        assertEquals(
                Integer.valueOf(5),
                result.getQuantity()
        );

        assertEquals(
                "PCS",
                result.getUnit()
        );
    }

    @Test
    public void parseRecoversP9fs() {
        DocumentReferenceData result =
                parser.parse(
                        match(
                                "ML",
                                "3923",
                                "ML3923-4p9fs"
                        )
                );

        assertEquals(
                Integer.valueOf(4),
                result.getQuantity()
        );

        assertEquals(
                "PQTS",
                result.getUnit()
        );
    }

    @Test
    public void parsePreservesSingularPqt() {
        DocumentReferenceData result =
                parser.parse(
                        match(
                                "MR",
                                "21211",
                                "MR21211-1pqt"
                        )
                );

        assertEquals(
                Integer.valueOf(1),
                result.getQuantity()
        );

        assertEquals(
                "PQT",
                result.getUnit()
        );
    }

    @Test
    public void parseSupportsMissingDash() {
        DocumentReferenceData result =
                parser.parse(
                        match(
                                "MR",
                                "21571",
                                "MR 21571 1 pcs"
                        )
                );

        assertEquals(
                Integer.valueOf(1),
                result.getQuantity()
        );

        assertEquals(
                "PCS",
                result.getUnit()
        );
    }

    @Test
    public void parsePreservesQualifiedReferenceWithoutDash() {
        DocumentReferenceData result =
                parser.parse(
                        match(
                                "ML",
                                "4170 YELLOW",
                                "ML 4170 Yellow 2 pqts"
                        )
                );

        assertEquals(
                "ML 4170 YELLOW",
                result.getReference()
                        .displayValue()
        );

        assertEquals(
                Integer.valueOf(2),
                result.getQuantity()
        );

        assertEquals(
                "PQTS",
                result.getUnit()
        );
    }

    @Test
    public void parseDoesNotInferIsolatedNumberWithoutDash() {
        DocumentReferenceData result =
                parser.parse(
                        match(
                                "MR",
                                "21571",
                                "MR 21571 2026"
                        )
                );

        assertNull(result.getQuantity());
        assertNull(result.getUnit());
    }

    @Test
    public void parseRejectsMultipleDocumentValues() {
        DocumentReferenceData result =
                parser.parse(
                        match(
                                "MR",
                                "21570",
                                "MR 21570 - 5 pcs 2 pqts"
                        )
                );

        assertNull(result.getQuantity());
        assertNull(result.getUnit());
    }

    private WarehouseReferenceMatch match(
            String category,
            String code,
            String source
    ) {
        return new WarehouseReferenceMatch(
                new WarehouseReference(
                        category,
                        code
                ),
                0,
                source,
                0
        );
    }
}