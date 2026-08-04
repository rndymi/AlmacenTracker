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

    private static final String LIST_SEPARATOR =
            "\u001E";

    private static final String FORMAT_VERSION =
            "2";

    private static final int LEGACY_FIELD_COUNT = 5;
    private static final int CURRENT_FIELD_COUNT = 10;

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

        WarehouseReference observed =
                value.getObservedReference();

        return FORMAT_VERSION
                + FIELD_SEPARATOR
                + encodeField(
                value.getReference().getCategory()
        )
                + FIELD_SEPARATOR
                + encodeField(
                value.getReference().getCode()
        )
                + FIELD_SEPARATOR
                + encodeField(
                observed.getCategory()
        )
                + FIELD_SEPARATOR
                + encodeField(
                observed.getCode()
        )
                + FIELD_SEPARATOR
                + quantity
                + FIELD_SEPARATOR
                + encodeField(unit)
                + FIELD_SEPARATOR
                + encodeDestinations(
                value.getDestinations()
        )
                + FIELD_SEPARATOR
                + value.getSourceLineIndex()
                + FIELD_SEPARATOR
                + encodeField(
                value.getSourceText()
        );
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

        if (parts.length == LEGACY_FIELD_COUNT) {
            return decodeLegacy(parts);
        }

        if (parts.length != CURRENT_FIELD_COUNT
                || !FORMAT_VERSION.equals(parts[0])) {
            return null;
        }

        String category =
                decodeField(parts[1]);

        String code =
                decodeField(parts[2]);

        String observedCategory =
                decodeField(parts[3]);

        String observedCode =
                decodeField(parts[4]);

        if (isBlank(category)
                || isBlank(code)
                || isBlank(observedCategory)
                || isBlank(observedCode)) {
            return null;
        }

        Integer quantity =
                parseQuantity(parts[5]);

        String unit =
                emptyToNull(
                        decodeField(parts[6])
                );

        List<String> destinations =
                decodeDestinations(parts[7]);

        int sourceLineIndex;

        try {
            sourceLineIndex =
                    Integer.parseInt(parts[8]);
        } catch (NumberFormatException exception) {
            return null;
        }

        String sourceText =
                emptyToNull(
                        decodeField(parts[9])
                );

        try {
            return new DocumentReferenceData(
                    new WarehouseReference(
                            category,
                            code
                    ),
                    new WarehouseReference(
                            observedCategory,
                            observedCode
                    ),
                    quantity,
                    unit,
                    sourceLineIndex,
                    sourceText,
                    Collections.emptyList(),
                    destinations
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static DocumentReferenceData decodeLegacy(
            String[] parts
    ) {
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

    private static String encodeField(
            String value
    ) {
        return value == null
                ? ""
                : android.net.Uri.encode(value);
    }

    private static String decodeField(
            String value
    ) {
        return value == null
                ? null
                : android.net.Uri.decode(value);
    }

    private static String encodeDestinations(
            List<String> values
    ) {
        if (values == null || values.isEmpty()) {
            return "";
        }

        List<String> encoded =
                new ArrayList<>();

        for (String value : values) {
            if (value != null
                    && !value.trim().isEmpty()) {
                encoded.add(
                        encodeField(value.trim())
                );
            }
        }

        return String.join(
                LIST_SEPARATOR,
                encoded
        );
    }

    private static List<String> decodeDestinations(
            String value
    ) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }

        String[] parts =
                value.split(
                        LIST_SEPARATOR,
                        -1
                );

        List<String> result =
                new ArrayList<>();

        for (String part : parts) {
            String decoded =
                    decodeField(part);

            if (decoded != null
                    && !decoded.trim().isEmpty()
                    && !result.contains(
                    decoded.trim()
            )) {
                result.add(decoded.trim());
            }
        }

        return result;
    }

    private static boolean isBlank(String value) {
        return value == null
                || value.trim().isEmpty();
    }

    private static String emptyToNull(String value) {
        return isBlank(value)
                ? null
                : value.trim();
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
