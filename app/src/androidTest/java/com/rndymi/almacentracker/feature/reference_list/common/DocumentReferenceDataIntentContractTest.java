package com.rndymi.almacentracker.feature.reference_list.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rndymi.almacentracker.domain.reference.DocumentReferenceData;
import com.rndymi.almacentracker.domain.reference.DocumentReferenceAllocation;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public final class DocumentReferenceDataIntentContractTest {

    @Test
    public void roundTripPreservesVersionThreeDocumentData() {
        WarehouseReference confirmed =
                new WarehouseReference("MR", "8665");
        WarehouseReference observed =
                new WarehouseReference("MK", "866 S");
        DocumentReferenceData value =
                new DocumentReferenceData(
                        confirmed,
                        observed,
                        1,
                        "P",
                        4,
                        "MK866S - 1 P - ①②",
                        Collections.emptyList(),
                        Arrays.asList("①", "②"),
                        Arrays.asList(
                                new DocumentReferenceAllocation(
                                        2,
                                        "P",
                                        "Tienda 2"
                                ),
                                new DocumentReferenceAllocation(
                                        1,
                                        "P",
                                        "Tienda 1"
                                )
                        )
                );
        Intent intent = new Intent();

        DocumentReferenceDataIntentContract
                .putDocumentReferences(
                        intent,
                        Collections.singletonList(value)
                );

        List<DocumentReferenceData> result =
                DocumentReferenceDataIntentContract
                        .getDocumentReferences(intent);

        assertEquals(1, result.size());
        DocumentReferenceData decoded = result.get(0);
        assertEquals(confirmed, decoded.getReference());
        assertEquals(observed, decoded.getObservedReference());
        assertEquals(Integer.valueOf(1), decoded.getQuantity());
        assertEquals("P", decoded.getUnit());
        assertEquals(4, decoded.getSourceLineIndex());
        assertEquals(
                "MK866S - 1 P - ①②",
                decoded.getSourceText()
        );
        assertEquals(
                Arrays.asList("①", "②"),
                decoded.getDestinations()
        );
        assertEquals(
                "Tienda 2",
                decoded.getAllocations()
                        .get(0)
                        .getDestination()
        );
        assertEquals(
                1,
                decoded.getAllocations()
                        .get(1)
                        .getQuantity()
        );
    }

    @Test
    public void decodeSupportsLegacyFiveFieldFormat() {
        Intent intent = new Intent();
        ArrayList<String> encoded = new ArrayList<>();
        encoded.add(
                "MR\u001F21570\u001F5\u001FPCS\u001F3"
        );
        intent.putStringArrayListExtra(
                DocumentReferenceDataIntentContract
                        .EXTRA_DOCUMENT_REFERENCES,
                encoded
        );

        DocumentReferenceData result =
                DocumentReferenceDataIntentContract
                        .getDocumentReferences(intent)
                        .get(0);

        assertEquals(
                new WarehouseReference("MR", "21570"),
                result.getReference()
        );
        assertEquals(
                result.getReference(),
                result.getObservedReference()
        );
        assertEquals(Integer.valueOf(5), result.getQuantity());
        assertEquals("PCS", result.getUnit());
        assertEquals(3, result.getSourceLineIndex());
        assertNull(result.getSourceText());
        assertEquals(
                Collections.emptyList(),
                result.getDestinations()
        );
    }

    @Test
    public void decodeSupportsPreviousVersionTwoFormat() {
        Intent intent = new Intent();
        ArrayList<String> encoded = new ArrayList<>();
        encoded.add(
                "2\u001FM\u001F873-9\u001FM\u001F873-9"
                        + "\u001F1\u001FP\u001F"
                        + "\u001F2\u001FM873-9%20-%201P"
        );
        intent.putStringArrayListExtra(
                DocumentReferenceDataIntentContract
                        .EXTRA_DOCUMENT_REFERENCES,
                encoded
        );

        DocumentReferenceData result =
                DocumentReferenceDataIntentContract
                        .getDocumentReferences(intent)
                        .get(0);

        assertEquals("M", result.getReference().getCategory());
        assertEquals("873-9", result.getReference().getCode());
        assertEquals(Integer.valueOf(1), result.getQuantity());
        assertEquals("P", result.getUnit());
        assertEquals(2, result.getSourceLineIndex());
        assertEquals(
                Collections.emptyList(),
                result.getAllocations()
        );
    }
}
