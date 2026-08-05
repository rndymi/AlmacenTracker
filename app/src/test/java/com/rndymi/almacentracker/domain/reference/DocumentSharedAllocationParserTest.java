package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class DocumentSharedAllocationParserTest {

    private final DocumentSharedAllocationParser parser =
            new DocumentSharedAllocationParser();

    @Test
    public void parsesQuantityUnitAndStore() {
        DocumentReferenceAllocation result =
                parser.parse("2P - tienda 2");

        assertEquals(2, result.getQuantity());
        assertEquals("P", result.getUnit());
        assertEquals("Tienda 2", result.getDestination());
    }

    @Test
    public void defaultsMissingQuantityBeforeUnitToOne() {
        DocumentReferenceAllocation result =
                parser.parse("P - tienda 1");

        assertEquals(1, result.getQuantity());
        assertEquals("P", result.getUnit());
        assertEquals("Tienda 1", result.getDestination());
    }

    @Test
    public void rejectsOrdinaryDocumentTitles() {
        assertNull(parser.parse("DA KE"));
        assertNull(parser.parse("REPARTO PORTO"));
    }
}
