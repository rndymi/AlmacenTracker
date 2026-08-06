package com.rndymi.almacentracker.evaluation.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public final class DetectionMetricsCalculatorTest {

    private DetectionMetricsCalculator calculator;

    @Before
    public void setUp() {
        calculator =
                new DetectionMetricsCalculator(
                        0.50d
                );
    }

    @Test
    public void matchingRegionsProducePerfectMetrics() {
        NormalizedBox first =
                new NormalizedBox(
                        0.10d,
                        0.10d,
                        0.30d,
                        0.20d
                );

        NormalizedBox second =
                new NormalizedBox(
                        0.60d,
                        0.10d,
                        0.80d,
                        0.20d
                );

        DetectionMetrics result =
                calculator.calculate(
                        Arrays.asList(first, second),
                        Arrays.asList(first, second)
                );

        assertEquals(
                2,
                result.getMatchedRegionCount()
        );

        assertEquals(
                1.0d,
                result.getPrecision()
                        .getValueOrZero(),
                0.000001d
        );

        assertEquals(
                1.0d,
                result.getRecall()
                        .getValueOrZero(),
                0.000001d
        );

        assertEquals(
                1.0d,
                result.getF1(),
                0.000001d
        );
    }

    @Test
    public void noDetectionsDoesNotReportPerfectPrecision() {
        DetectionMetrics result =
                calculator.calculate(
                        Collections.singletonList(
                                new NormalizedBox(
                                        0.10d,
                                        0.10d,
                                        0.30d,
                                        0.20d
                                )
                        ),
                        Collections.emptyList()
                );

        assertFalse(
                result.getPrecision().isDefined()
        );

        assertEquals(
                0.0d,
                result.getPrecision()
                        .getValueOrZero(),
                0.000001d
        );

        assertEquals(
                0.0d,
                result.getF1(),
                0.000001d
        );
    }

    @Test
    public void extraRegionReducesPrecision() {
        NormalizedBox expected =
                new NormalizedBox(
                        0.10d,
                        0.10d,
                        0.30d,
                        0.20d
                );

        DetectionMetrics result =
                calculator.calculate(
                        Collections.singletonList(
                                expected
                        ),
                        Arrays.asList(
                                expected,
                                new NormalizedBox(
                                        0.70d,
                                        0.70d,
                                        0.90d,
                                        0.80d
                                )
                        )
                );

        assertEquals(
                1,
                result.getExtraRegionCount()
        );

        assertEquals(
                0.50d,
                result.getPrecision()
                        .getValueOrZero(),
                0.000001d
        );
    }

    @Test
    public void missedRegionReducesRecall() {
        NormalizedBox first =
                new NormalizedBox(
                        0.10d,
                        0.10d,
                        0.30d,
                        0.20d
                );

        NormalizedBox second =
                new NormalizedBox(
                        0.60d,
                        0.10d,
                        0.80d,
                        0.20d
                );

        DetectionMetrics result =
                calculator.calculate(
                        Arrays.asList(first, second),
                        Collections.singletonList(first)
                );

        assertEquals(
                1,
                result.getMissedRegionCount()
        );

        assertEquals(
                0.50d,
                result.getRecall()
                        .getValueOrZero(),
                0.000001d
        );
    }
}
