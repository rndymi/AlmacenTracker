package com.rndymi.almacentracker.domain.reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class DocumentUnitNormalizer {

    private static final Set<String>
            SAFE_CORRECTION_TARGETS =
            Collections.unmodifiableSet(
                    new HashSet<>(
                            Arrays.asList(
                                    "PCS",
                                    "PQT",
                                    "PQTS",
                                    "CAJA",
                                    "CAJAS"
                            )
                    )
            );

    public String normalize(
            String observedValue
    ) {
        if (observedValue == null) {
            return null;
        }

        String normalized =
                observedValue
                        .replace('\u00A0', ' ')
                        .replaceAll(
                                "[\\p{Z}\\s]+",
                                " "
                        )
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.matches(
                "[\\p{L}]+"
                        + "(?: [\\p{L}]+){0,2}"
        )) {
            return normalized;
        }

        String compact =
                normalized.replace(" ", "");

        String corrected =
                correctCommonOcrCharacters(
                        compact
                );

        return SAFE_CORRECTION_TARGETS
                .contains(corrected)
                ? corrected
                : null;
    }

    private String correctCommonOcrCharacters(
            String value
    ) {
        StringBuilder corrected =
                new StringBuilder(
                        value.length()
                );

        boolean containsDigit =
                value.chars()
                        .anyMatch(
                                Character::isDigit
                        );

        for (int index = 0;
             index < value.length();
             index++) {

            char character =
                    value.charAt(index);

            switch (character) {
                case '5':
                    corrected.append('S');
                    break;

                case '9':
                    corrected.append('Q');
                    break;

                case '0':
                    corrected.append('O');
                    break;

                case '1':
                    corrected.append('I');
                    break;

                case 'F':
                    corrected.append(
                            containsDigit
                                    ? 'T'
                                    : 'F'
                    );
                    break;

                default:
                    corrected.append(character);
                    break;
            }
        }

        return corrected.toString();
    }
}
