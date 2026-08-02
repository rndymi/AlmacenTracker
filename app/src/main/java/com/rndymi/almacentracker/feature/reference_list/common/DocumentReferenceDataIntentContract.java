package com.rndymi.almacentracker.feature.reference_list.common;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.rndymi.almacentracker.domain.reference.DocumentReferenceData;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DocumentReferenceDataIntentContract {

    public static final String EXTRA_DOCUMENT_REFERENCES =
            "com.rndymi.almacentracker.extra."
                    + "DOCUMENT_REFERENCE_DATA";

    private static final String FIELD_SEPARATOR =
            "\u001F";

    private DocumentReferenceDataIntentContract() {
    }

    public static void putDocumentReferences(
            Intent intent,
            List<DocumentReferenceData> values
    ) {
        ArrayList<String> encodedValues =
                new ArrayList<>();

        if (values != null) {
            for (DocumentReferenceData value : values) {
                if (value == null) {
                    continue;
                }

                encodedValues.add(
                        encode(value)
                );
            }
        }

        intent.putStringArrayListExtra(
                EXTRA_DOCUMENT_REFERENCES,
                encodedValues
        );
    }

    public static List<DocumentReferenceData>
    getDocumentReferences(
            @Nullable Intent intent
    ) {
        if (intent == null) {
            return Collections.emptyList();
        }

        ArrayList<String> encodedValues =
                intent.getStringArrayListExtra(
                        EXTRA_DOCUMENT_REFERENCES
                );

        if (encodedValues == null) {
            return Collections.emptyList();
        }

        List<DocumentReferenceData> result =
                new ArrayList<>();

        for (String encoded : encodedValues) {
            DocumentReferenceData decoded =
                    decode(encoded);

            if (decoded != null) {
                result.add(decoded);
            }
        }

        return Collections.unmodifiableList(
                result
        );
    }

    private static String encode(
            DocumentReferenceData value
    ) {
        String quantity =
                value.getQuantity() == null
                        ? ""
                        : String.valueOf(
                        value.getQuantity()
                );

        String unit =
                value.getUnit() == null
                        ? ""
                        : value.getUnit();

        return value.getReference().getCategory()
                + FIELD_SEPARATOR
                + value.getReference().getCode()
                + FIELD_SEPARATOR
                + quantity
                + FIELD_SEPARATOR
                + unit
                + FIELD_SEPARATOR
                + value.getSourceLineIndex();
    }

    private static DocumentReferenceData decode(
            String encoded
    ) {
        if (encoded == null) {
            return null;
        }

        String[] parts =
                encoded.split(
                        FIELD_SEPARATOR,
                        -1
                );

        if (parts.length != 5) {
            return null;
        }

        String category = parts[0].trim();
        String code = parts[1].trim();

        if (category.isEmpty() || code.isEmpty()) {
            return null;
        }

        Integer quantity =
                parseQuantity(parts[2]);

        String unit =
                parts[3].trim().isEmpty()
                        ? null
                        : parts[3].trim();

        int sourceLineIndex;

        try {
            sourceLineIndex =
                    Integer.parseInt(parts[4]);
        } catch (NumberFormatException exception) {
            return null;
        }

        try {
            return new DocumentReferenceData(
                    new WarehouseReference(
                            category,
                            code
                    ),
                    quantity,
                    unit,
                    sourceLineIndex,
                    null
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static Integer parseQuantity(
            String value
    ) {
        if (value == null
                || value.trim().isEmpty()) {
            return null;
        }

        try {
            int quantity =
                    Integer.parseInt(value.trim());

            return quantity > 0
                    ? quantity
                    : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}