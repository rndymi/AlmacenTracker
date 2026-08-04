package com.rndymi.almacentracker.feature.reference_list.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rndymi.almacentracker.domain.reference.DocumentReferenceData;
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
    public void roundTripPreservesVersionTwoDocumentData() {
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
                        Arrays.asList("①", "②")
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
}
