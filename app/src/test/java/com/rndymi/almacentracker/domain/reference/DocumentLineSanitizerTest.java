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
}
