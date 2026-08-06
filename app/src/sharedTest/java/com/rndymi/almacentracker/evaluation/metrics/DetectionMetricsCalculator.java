package com.rndymi.almacentracker.evaluation.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DetectionMetricsCalculator {

    private final double minimumIou;

    public DetectionMetricsCalculator(
            double minimumIou
    ) {
        if (!Double.isFinite(minimumIou)
                || minimumIou <= 0.0d
                || minimumIou > 1.0d) {
            throw new IllegalArgumentException(
                    "minimumIou must be greater than 0 and at most 1"
            );
        }

        this.minimumIou = minimumIou;
    }

    public DetectionMetrics calculate(
            List<NormalizedBox> expected,
            List<NormalizedBox> detected
    ) {
        Objects.requireNonNull(expected, "expected");
        Objects.requireNonNull(detected, "detected");

        List<NormalizedBox> expectedBoxes =
                requireBoxes(expected, "expected");

        List<NormalizedBox> detectedBoxes =
                requireBoxes(detected, "detected");

        boolean[] expectedMatched =
                new boolean[expectedBoxes.size()];

        boolean[] detectedMatched =
                new boolean[detectedBoxes.size()];

        int matched = matchOneToOne(
                expectedBoxes,
                detectedBoxes,
                expectedMatched,
                detectedMatched
        );

        int merged = countMergedRegions(
                expectedBoxes,
                detectedBoxes
        );

        int split = countSplitRegions(
                expectedBoxes,
                detectedBoxes
        );

        return new DetectionMetrics(
                expectedBoxes.size(),
                detectedBoxes.size(),
                matched,
                expectedBoxes.size() - matched,
                detectedBoxes.size() - matched,
                merged,
                split
        );
    }

    private int matchOneToOne(
            List<NormalizedBox> expected,
            List<NormalizedBox> detected,
            boolean[] expectedMatched,
            boolean[] detectedMatched
    ) {
        int matched = 0;

        while (true) {
            int bestExpected = -1;
            int bestDetected = -1;
            double bestIou = minimumIou;

            for (int expectedIndex = 0;
                 expectedIndex < expected.size();
                 expectedIndex++) {
                if (expectedMatched[expectedIndex]) {
                    continue;
                }

                for (int detectedIndex = 0;
                     detectedIndex < detected.size();
                     detectedIndex++) {
                    if (detectedMatched[detectedIndex]) {
                        continue;
                    }

                    double iou =
                            expected.get(expectedIndex)
                                    .intersectionOverUnion(
                                            detected.get(detectedIndex)
                                    );

                    if (iou >= bestIou) {
                        bestExpected = expectedIndex;
                        bestDetected = detectedIndex;
                        bestIou = iou;
                    }
                }
            }

            if (bestExpected < 0
                    || bestDetected < 0) {
                break;
            }

            expectedMatched[bestExpected] = true;
            detectedMatched[bestDetected] = true;
            matched++;
        }

        return matched;
    }

    private int countMergedRegions(
            List<NormalizedBox> expected,
            List<NormalizedBox> detected
    ) {
        int merged = 0;

        for (NormalizedBox detectedBox : detected) {
            int overlappingExpected = 0;

            for (NormalizedBox expectedBox : expected) {
                if (detectedBox.overlaps(
                        expectedBox,
                        minimumIou
                )) {
                    overlappingExpected++;
                }
            }

            if (overlappingExpected > 1) {
                merged++;
            }
        }

        return merged;
    }

    private int countSplitRegions(
            List<NormalizedBox> expected,
            List<NormalizedBox> detected
    ) {
        int split = 0;

        for (NormalizedBox expectedBox : expected) {
            int overlappingDetected = 0;

            for (NormalizedBox detectedBox : detected) {
                if (expectedBox.overlaps(
                        detectedBox,
                        minimumIou
                )) {
                    overlappingDetected++;
                }
            }

            if (overlappingDetected > 1) {
                split++;
            }
        }

        return split;
    }

    private List<NormalizedBox> requireBoxes(
            List<NormalizedBox> source,
            String fieldName
    ) {
        List<NormalizedBox> result =
                new ArrayList<>(source.size());

        for (NormalizedBox box : source) {
            result.add(
                    Objects.requireNonNull(
                            box,
                            fieldName
                                    + " cannot contain null"
                    )
            );
        }

        return result;
    }
}
