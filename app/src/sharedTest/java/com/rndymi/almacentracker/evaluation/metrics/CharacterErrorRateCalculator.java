package com.rndymi.almacentracker.evaluation.metrics;

import java.util.Locale;
import java.util.Objects;

public final class CharacterErrorRateCalculator {

    private static final byte MATCH = 0;
    private static final byte SUBSTITUTE = 1;
    private static final byte INSERT = 2;
    private static final byte DELETE = 3;

    public EditDistanceResult calculate(
            String expected,
            String actual,
            TextNormalizationPolicy policy
    ) {
        Objects.requireNonNull(expected, "expected");
        Objects.requireNonNull(actual, "actual");
        Objects.requireNonNull(policy, "policy");

        String normalizedExpected =
                normalize(expected, policy);

        String normalizedActual =
                normalize(actual, policy);

        int expectedLength =
                normalizedExpected.length();

        int actualLength =
                normalizedActual.length();

        int[][] distance =
                new int[expectedLength + 1]
                        [actualLength + 1];

        byte[][] operation =
                new byte[expectedLength + 1]
                        [actualLength + 1];

        for (int expectedIndex = 1;
             expectedIndex <= expectedLength;
             expectedIndex++) {
            distance[expectedIndex][0] =
                    expectedIndex;

            operation[expectedIndex][0] =
                    DELETE;
        }

        for (int actualIndex = 1;
             actualIndex <= actualLength;
             actualIndex++) {
            distance[0][actualIndex] =
                    actualIndex;

            operation[0][actualIndex] =
                    INSERT;
        }

        for (int expectedIndex = 1;
             expectedIndex <= expectedLength;
             expectedIndex++) {
            for (int actualIndex = 1;
                 actualIndex <= actualLength;
                 actualIndex++) {
                char expectedCharacter =
                        normalizedExpected.charAt(
                                expectedIndex - 1
                        );

                char actualCharacter =
                        normalizedActual.charAt(
                                actualIndex - 1
                        );

                if (expectedCharacter
                        == actualCharacter) {
                    distance[expectedIndex][actualIndex] =
                            distance[
                                    expectedIndex - 1
                                    ][
                                    actualIndex - 1
                                    ];

                    operation[expectedIndex][actualIndex] =
                            MATCH;

                    continue;
                }

                int substitution =
                        distance[
                                expectedIndex - 1
                                ][
                                actualIndex - 1
                                ] + 1;

                int insertion =
                        distance[
                                expectedIndex
                                ][
                                actualIndex - 1
                                ] + 1;

                int deletion =
                        distance[
                                expectedIndex - 1
                                ][
                                actualIndex
                                ] + 1;

                int minimum =
                        Math.min(
                                substitution,
                                Math.min(
                                        insertion,
                                        deletion
                                )
                        );

                distance[expectedIndex][actualIndex] =
                        minimum;

                if (minimum == substitution) {
                    operation[expectedIndex][actualIndex] =
                            SUBSTITUTE;
                } else if (minimum == deletion) {
                    operation[expectedIndex][actualIndex] =
                            DELETE;
                } else {
                    operation[expectedIndex][actualIndex] =
                            INSERT;
                }
            }
        }

        return backtrack(
                normalizedExpected,
                normalizedActual,
                operation
        );
    }

    private EditDistanceResult backtrack(
            String expected,
            String actual,
            byte[][] operation
    ) {
        int expectedIndex = expected.length();
        int actualIndex = actual.length();

        int substitutions = 0;
        int insertions = 0;
        int deletions = 0;
        int correctCharacters = 0;

        while (expectedIndex > 0
                || actualIndex > 0) {
            byte current =
                    operation[expectedIndex][actualIndex];

            if (expectedIndex > 0
                    && actualIndex > 0
                    && current == MATCH) {
                correctCharacters++;
                expectedIndex--;
                actualIndex--;
                continue;
            }

            if (expectedIndex > 0
                    && actualIndex > 0
                    && current == SUBSTITUTE) {
                substitutions++;
                expectedIndex--;
                actualIndex--;
                continue;
            }

            if (actualIndex > 0
                    && (
                    expectedIndex == 0
                            || current == INSERT
            )) {
                insertions++;
                actualIndex--;
                continue;
            }

            if (expectedIndex > 0) {
                deletions++;
                expectedIndex--;
            }
        }

        return new EditDistanceResult(
                expected,
                actual,
                substitutions,
                insertions,
                deletions,
                correctCharacters
        );
    }

    private String normalize(
            String value,
            TextNormalizationPolicy policy
    ) {
        if (policy == TextNormalizationPolicy.RAW) {
            return value;
        }

        return value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s\\u00A0]+", " ")
                .replaceAll("\\s*[-–—]\\s*", "-");
    }
}
