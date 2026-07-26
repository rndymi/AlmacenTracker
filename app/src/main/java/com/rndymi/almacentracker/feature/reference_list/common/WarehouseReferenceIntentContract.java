package com.rndymi.almacentracker.feature.reference_list.common;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.rndymi.almacentracker.domain.reference.WarehouseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WarehouseReferenceIntentContract {

    public static final String EXTRA_REFERENCES =
            "com.rndymi.almacentracker.extra."
                    + "CONFIRMED_REFERENCES";

    private static final String SEPARATOR =
            "\u001F";

    private WarehouseReferenceIntentContract() {
    }

    public static void putReferences(
            Intent intent,
            List<WarehouseReference> references
    ) {
        ArrayList<String> encodedReferences =
                new ArrayList<>();

        if (references != null) {
            for (
                    WarehouseReference reference
                    : references
            ) {
                if (reference == null) {
                    continue;
                }

                encodedReferences.add(
                        reference.getCategory()
                                + SEPARATOR
                                + reference.getCode()
                );
            }
        }

        intent.putStringArrayListExtra(
                EXTRA_REFERENCES,
                encodedReferences
        );
    }

    public static List<WarehouseReference>
    getReferences(
            @Nullable Intent intent
    ) {
        if (intent == null) {
            return Collections.emptyList();
        }

        ArrayList<String> encodedReferences =
                intent.getStringArrayListExtra(
                        EXTRA_REFERENCES
                );

        if (encodedReferences == null) {
            return Collections.emptyList();
        }

        List<WarehouseReference> references =
                new ArrayList<>();

        Set<String> identities =
                new HashSet<>();

        for (
                String encodedReference
                : encodedReferences
        ) {
            if (encodedReference == null) {
                continue;
            }

            String[] parts =
                    encodedReference.split(
                            SEPARATOR,
                            -1
                    );

            if (parts.length != 2) {
                continue;
            }

            String category =
                    parts[0].trim();

            String code =
                    parts[1].trim();

            if (category.isEmpty()
                    || code.isEmpty()) {
                continue;
            }

            WarehouseReference reference =
                    new WarehouseReference(
                            category,
                            code
                    );

            if (identities.add(
                    reference.identityKey()
            )) {
                references.add(reference);
            }
        }

        return Collections.unmodifiableList(
                references
        );
    }
}