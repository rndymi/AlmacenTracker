package com.rndymi.almacentracker.domain.reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class DocumentQuantityUnitVocabulary {

    private static final Set<String> KNOWN_UNITS =
            Collections.unmodifiableSet(
                    new HashSet<>(
                            Arrays.asList(
                                    "PC",
                                    "PCS",
                                    "PZ",
                                    "PZA",
                                    "PZAS",
                                    "PIEZA",
                                    "PIEZAS",
                                    "PQT",
                                    "PQTS",
                                    "PAQUETE",
                                    "PAQUETES",
                                    "UD",
                                    "UDS",
                                    "UNIDAD",
                                    "UNIDADES",
                                    "CJ",
                                    "CJA",
                                    "CAJA",
                                    "CAJAS",
                                    "BTO",
                                    "BULTO",
                                    "BULTOS",
                                    "PACK",
                                    "PACKS",
                                    "BOX",
                                    "BOXES",
                                    "CTN",
                                    "CTNS"
                            )
                    )
            );

    private DocumentQuantityUnitVocabulary() {
    }

    public static boolean isKnownUnit(
            String value
    ) {
        if (value == null) {
            return false;
        }

        return KNOWN_UNITS.contains(
                normalize(value)
        );
    }

    public static String normalize(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }

    public static Set<String> all() {
        return KNOWN_UNITS;
    }
}