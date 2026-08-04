package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Arrays;

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

    @Test
    public void parseRecoversFiveWhenSWasReadAsQuantity() {
        DocumentReferenceData result =
                parser.parse(
                        match(
                                "MR",
                                "21570",
                                "M221570-Spcs"
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
    public void parseRecoversFiveAndPackageUnitFromNoisyLine() {
        DocumentReferenceData result =
                parser.parse(
                        match(
                                "MA",
                                "901",
                                "MA901-SPgts"
                        )
                );

        assertEquals(
                Integer.valueOf(5),
                result.getQuantity()
        );

        assertEquals(
                "PQTS",
                result.getUnit()
        );
    }

    @Test
    public void parseRecoversPackageUnitFromP4ts() {
        DocumentReferenceData result =
                parser.parse(
                        match(
                                "MR",
                                "21211",
                                "MR21211-1P4t"
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
    public void parseMarksZeroSixQuantityAsAmbiguous() {
        DocumentReferenceData result =
                parser.parse(
                        match(
                                "MR",
                                "21570",
                                "MR21570-40pcs"
                        )
                );

        assertEquals(Integer.valueOf(40), result.getQuantity());
        assertEquals(
                Arrays.asList(46),
                result.getQuantitySuggestions()
        );
    }

    @Test
    public void parseSuggestsSixWhenZeroQuantityWasObserved() {
        DocumentReferenceData result =
                parser.parse(
                        match(
                                "MR",
                                "21570",
                                "MR21570-0pcs"
                        )
                );

        assertNull(result.getQuantity());
        assertEquals(
                Arrays.asList(6),
                result.getQuantitySuggestions()
        );
    }

    @Test
    public void parsePreservesObservedReferenceAndExtractsDestinations() {
        WarehouseReference observed =
                new WarehouseReference("MK", "866S");
        WarehouseReference resolved =
                new WarehouseReference("MR", "8665");
        WarehouseReferenceMatch match =
                new WarehouseReferenceMatch(
                        observed,
                        3,
                        "MK866S-1P-①②①",
                        0
                ).withResolvedReference(resolved);

        DocumentReferenceData result = parser.parse(match);

        assertEquals(resolved, result.getReference());
        assertEquals(observed, result.getObservedReference());
        assertEquals(Integer.valueOf(1), result.getQuantity());
        assertEquals("P", result.getUnit());
        assertEquals(
                Arrays.asList("①", "②"),
                result.getDestinations()
        );
        assertEquals(3, result.getSourceLineIndex());
        assertEquals(
                "MK866S-1P-①②①",
                result.getSourceText()
        );
    }

    @Test
    public void parseKeepsDestinationsWhenQuantityCannotBeParsed() {
        DocumentReferenceData result =
                parser.parse(
                        match(
                                "MR",
                                "21570",
                                "MR21570 - destino ③"
                        )
                );

        assertNull(result.getQuantity());
        assertNull(result.getUnit());
        assertEquals(
                Arrays.asList("③"),
                result.getDestinations()
        );
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
