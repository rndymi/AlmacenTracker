package com.rndymi.almacentracker.evaluation.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ReconstructionMetricsCalculator {

    public ReconstructionMetrics calculate(
            List<String> expectedLines,
            List<String> reconstructedLines,
            List<Integer> expectedGlobalLineIndexes
    ) {
        List<String> expected =
                normalize(expectedLines);

        List<String> actual =
                normalize(reconstructedLines);

        Objects.requireNonNull(
                expectedGlobalLineIndexes,
                "expectedGlobalLineIndexes"
        );

        int exactMatches = 0;
        int correctOrder = 0;

        int comparableCount =
                Math.min(
                        expected.size(),
                        actual.size()
                );

        for (int index = 0;
             index < comparableCount;
             index++) {
            if (expected.get(index)
                    .equals(actual.get(index))) {
                exactMatches++;
                correctOrder++;
            }
        }

        int mergedLines =
                countMergedLines(
                        expected,
                        actual
                );

        int splitLines =
                countSplitLines(
                        expected,
                        actual
                );

        int correctGlobalLines = 0;

        for (Integer index :
                expectedGlobalLineIndexes) {
            if (index == null
                    || index < 0
                    || index >= expected.size()) {
                throw new IllegalArgumentException(
                        "Invalid global line index"
                );
            }

            if (index < actual.size()
                    && expected.get(index)
                    .equals(actual.get(index))) {
                correctGlobalLines++;
            }
        }

        return new ReconstructionMetrics(
                expected.size(),
                actual.size(),
                exactMatches,
                mergedLines,
                splitLines,
                correctOrder,
                expectedGlobalLineIndexes.size(),
                correctGlobalLines
        );
    }

    private int countMergedLines(
            List<String> expected,
            List<String> actual
    ) {
        int count = 0;

        for (String actualLine : actual) {
            int containedExpectedLines = 0;

            for (String expectedLine : expected) {
                if (!expectedLine.isEmpty()
                        && actualLine.contains(
                        expectedLine
                )) {
                    containedExpectedLines++;
                }
            }

            if (containedExpectedLines > 1) {
                count++;
            }
        }

        return count;
    }

    private int countSplitLines(
            List<String> expected,
            List<String> actual
    ) {
        int count = 0;

        for (String expectedLine : expected) {
            StringBuilder joinedFragments =
                    new StringBuilder();

            int fragmentCount = 0;

            for (String actualLine : actual) {
                if (!actualLine.isEmpty()
                        && expectedLine.contains(
                        actualLine
                )) {
                    joinedFragments.append(actualLine);
                    fragmentCount++;
                }
            }

            if (fragmentCount > 1
                    && joinedFragments.length() > 0) {
                count++;
            }
        }

        return count;
    }

    private List<String> normalize(
            List<String> values
    ) {
        Objects.requireNonNull(values, "values");

        List<String> result =
                new ArrayList<>(values.size());

        for (String value : values) {
            Objects.requireNonNull(
                    value,
                    "values cannot contain null"
            );

            result.add(
                    value
                            .trim()
                            .toUpperCase(Locale.ROOT)
                            .replaceAll("\\s+", "")
            );
        }

        return result;
    }
}
