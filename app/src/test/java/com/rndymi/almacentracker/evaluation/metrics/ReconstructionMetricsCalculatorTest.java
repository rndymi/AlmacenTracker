package com.rndymi.almacentracker.evaluation.metrics;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public final class ReconstructionMetricsCalculatorTest {

    private ReconstructionMetricsCalculator calculator;

    @Before
    public void setUp() {
        calculator =
                new ReconstructionMetricsCalculator();
    }

    @Test
    public void preservesFourColumnOrderAndHeader() {
        ReconstructionMetrics result =
                calculator.calculate(
                        Arrays.asList(
                                "AKE PORTO",
                                "MR22547",
                                "MR22139",
                                "MA871",
                                "MA987",
                                "MA988"
                        ),
                        Arrays.asList(
                                "AKE PORTO",
                                "MR22547",
                                "MR22139",
                                "MA871",
                                "MA987",
                                "MA988"
                        ),
                        Collections.singletonList(0)
                );

        assertEquals(
                1.0d,
                result.getColumnOrderAccuracy()
                        .getValueOrZero(),
                0.000001d
        );

        assertEquals(
                1.0d,
                result.getGlobalLineAccuracy()
                        .getValueOrZero(),
                0.000001d
        );
    }

    @Test
    public void identifiesMergedColumnReferences() {
        ReconstructionMetrics result =
                calculator.calculate(
                        Arrays.asList(
                                "MR22547",
                                "MA871"
                        ),
                        Collections.singletonList(
                                "MR22547MA871"
                        ),
                        Collections.emptyList()
                );

        assertEquals(
                1,
                result.getMergedLineCount()
        );
    }

    @Test
    public void movedHeaderReducesGlobalAccuracy() {
        ReconstructionMetrics result =
                calculator.calculate(
                        Arrays.asList(
                                "AKE PORTO",
                                "MR22547",
                                "MA871"
                        ),
                        Arrays.asList(
                                "MR22547",
                                "MA871",
                                "AKE PORTO"
                        ),
                        Collections.singletonList(0)
                );

        assertEquals(
                0.0d,
                result.getGlobalLineAccuracy()
                        .getValueOrZero(),
                0.000001d
        );
    }
}
