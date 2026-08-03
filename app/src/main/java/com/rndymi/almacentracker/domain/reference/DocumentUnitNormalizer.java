package com.rndymi.almacentracker.domain.reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class DocumentUnitNormalizer {

    private static final Set<String>SAFE_CORRECTION_TARGETS =
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

    private static final int
            MAXIMUM_CORRECTION_DISTANCE = 2;

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
            String compactAlphabetic =
                    normalized.replace(" ", "");

            String correctedKnownUnit =
                    findUniqueKnownCorrection(
                            compactAlphabetic
                    );

            return correctedKnownUnit != null
                    ? correctedKnownUnit
                    : normalized;
        }

        String compact =
                normalized.replace(" ", "");

        String characterCorrected =
                correctCommonOcrCharacters(
                        compact
                );

        String correctedKnownUnit =
                findUniqueKnownCorrection(
                        characterCorrected
                );

        return correctedKnownUnit;
    }

    private String findUniqueKnownCorrection(
            String observed
    ) {
        if (SAFE_CORRECTION_TARGETS.contains(
                observed
        )) {
            return observed;
        }

        String uniqueCandidate = null;
        int bestDistance =
                Integer.MAX_VALUE;

        for (String expected
                : SAFE_CORRECTION_TARGETS) {

            int distance =
                    levenshteinDistance(
                            observed,
                            expected
                    );

            if (distance
                    > MAXIMUM_CORRECTION_DISTANCE) {
                continue;
            }

            if (distance < bestDistance) {
                uniqueCandidate = expected;
                bestDistance = distance;
                continue;
            }

            if (distance == bestDistance
                    && uniqueCandidate != null
                    && !uniqueCandidate.equals(expected)) {
                uniqueCandidate = null;
            }
        }

        return uniqueCandidate;
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

                case '4':
                    corrected.append(
                            containsDigit
                                    ? 'Q'
                                    : '4'
                    );
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

    private int levenshteinDistance(
            String left,
            String right
    ) {
        int[] previous =
                new int[right.length() + 1];

        int[] current =
                new int[right.length() + 1];

        for (int index = 0;
             index <= right.length();
             index++) {
            previous[index] = index;
        }

        for (int leftIndex = 1;
             leftIndex <= left.length();
             leftIndex++) {

            current[0] = leftIndex;

            for (int rightIndex = 1;
                 rightIndex <= right.length();
                 rightIndex++) {

                int replacementCost =
                        left.charAt(leftIndex - 1)
                                == right.charAt(
                                rightIndex - 1
                        )
                                ? 0
                                : 1;

                current[rightIndex] =
                        Math.min(
                                Math.min(
                                        current[rightIndex - 1]
                                                + 1,
                                        previous[rightIndex]
                                                + 1
                                ),
                                previous[rightIndex - 1]
                                        + replacementCost
                        );
            }

            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[right.length()];
    }
}