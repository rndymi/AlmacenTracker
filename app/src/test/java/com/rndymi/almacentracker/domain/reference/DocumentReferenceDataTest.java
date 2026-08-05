package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DocumentReferenceDataTest {

    @Test
    public void legacyConstructorUsesReferenceAsObservedReference() {
        WarehouseReference reference =
                new WarehouseReference("MR", "21570");

        DocumentReferenceData data =
                new DocumentReferenceData(
                        reference,
                        5,
                        "PCS",
                        0,
                        "MR21570-5PCS"
                );

        assertEquals(reference, data.getObservedReference());
        assertEquals(
                Collections.emptyList(),
                data.getDestinations()
        );
    }

    @Test
    public void constructorPreservesObservedReferenceAndNormalizesDestinations() {
        WarehouseReference confirmed =
                new WarehouseReference("MR", "8665");
        WarehouseReference observed =
                new WarehouseReference("MK", "866S");
        List<String> destinations =
                new ArrayList<>(
                        Arrays.asList(
                                " ① ",
                                "②",
                                "①",
                                " ",
                                null
                        )
                );

        DocumentReferenceData data =
                new DocumentReferenceData(
                        confirmed,
                        observed,
                        1,
                        "P",
                        3,
                        "MK866S-1P-①②",
                        Collections.emptyList(),
                        destinations
                );

        destinations.add("③");

        assertEquals(observed, data.getObservedReference());
        assertEquals(
                Arrays.asList("①", "②"),
                data.getDestinations()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> data.getDestinations().add("④")
        );
    }
}
