package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class DocumentLineSanitizerTest {

    @Test
    public void removesFormBleedBeforeHeaderAndStandaloneNoise() {
        List<String> result = new DocumentLineSanitizer(
                new WarehouseReferenceParser()
        ).sanitize(Arrays.asList(
                "om",
                "A8UTOA3",
                "DAKE",
                "m873-9-1p-①",
                "m873-①-p-①②",
                "mR21518-",
                "MR8-250-",
                "2p-tienda2",
                "mR20566-",
                "mR8251-",
                "p-tienda1",
                "mR20565-",
                "0",
                "mR9612-",
                "m20156-",
                "mr19786-",
                "mR21835-"
        ));

        assertEquals(14, result.size());
        assertEquals("DAKE", result.get(0));
        assertFalse(result.contains("om"));
        assertFalse(result.contains("A8UTOA3"));
        assertFalse(result.contains("0"));
    }

    @Test
    public void preservesEarlierOrdinaryReferencesAndTitle() {
        List<String> source = Arrays.asList(
                "UTOPYA",
                "MA710-4pcs",
                "MA900-3pcs",
                "MA901-5pqts",
                "MA930-2pqts",
                "MR21234-4pqts",
                "MR21232-2pqts",
                "MR21502-2pqts",
                "MR21505-1pqts",
                "MR21111-2pqts",
                "MR21211-1pqt"
        );

        List<String> result = new DocumentLineSanitizer(
                new WarehouseReferenceParser()
        ).sanitize(source);

        assertEquals(source, result);
    }

    @Test
    public void preservesUncertainReferencesBeforeFirstSafeMatch() {
        List<String> source = Arrays.asList(
                "Elena",
                "MR21570-5pcs",
                "MR21571-1pcs",
                "MS5008-3pcs",
                "ML3923-4pqts"
        );

        List<String> result = new DocumentLineSanitizer(
                new WarehouseReferenceParser()
        ).sanitize(source);

        assertEquals(source, result);
    }
}
