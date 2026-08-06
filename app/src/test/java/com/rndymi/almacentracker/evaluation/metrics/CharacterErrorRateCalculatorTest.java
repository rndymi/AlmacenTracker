package com.rndymi.almacentracker.evaluation.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public final class CharacterErrorRateCalculatorTest {

    private CharacterErrorRateCalculator calculator;

    @Before
    public void setUp() {
        calculator =
                new CharacterErrorRateCalculator();
    }

    @Test
    public void exactReferenceHasZeroCer() {
        EditDistanceResult result =
                calculator.calculate(
                        "MR8665",
                        "MR8665",
                        TextNormalizationPolicy.RAW
                );

        assertTrue(result.isExactMatch());
        assertEquals(
                0,
                result.getEditDistance()
        );
        assertEquals(
                0.0d,
                result.getCharacterErrorRate(),
                0.000001d
        );
    }

    @Test
    public void mk866sKeepsRecognitionErrorsVisible() {
        EditDistanceResult result =
                calculator.calculate(
                        "MR8665",
                        "MK866S",
                        TextNormalizationPolicy.RAW
                );

        assertFalse(result.isExactMatch());
        assertEquals(
                2,
                result.getSubstitutions()
        );
        assertEquals(
                2.0d / 6.0d,
                result.getCharacterErrorRate(),
                0.000001d
        );
    }

    @Test
    public void ma201RegistersMissingDigit() {
        EditDistanceResult result =
                calculator.calculate(
                        "MA1201",
                        "MA201",
                        TextNormalizationPolicy.RAW
                );

        assertEquals(
                1,
                result.getDeletions()
        );
        assertEquals(
                1.0d / 6.0d,
                result.getCharacterErrorRate(),
                0.000001d
        );
    }

    @Test
    public void mr215hDoesNotApplyFunctionalCorrection() {
        EditDistanceResult result =
                calculator.calculate(
                        "MR21571",
                        "MR215H",
                        TextNormalizationPolicy.FUNCTIONAL
                );

        assertFalse(result.isExactMatch());
        assertTrue(
                result.getCharacterErrorRate() > 0.0d
        );
    }

    @Test
    public void functionalPolicyNormalizesSpacingAndCaseOnly() {
        EditDistanceResult result =
                calculator.calculate(
                        "MA 870",
                        "ma   870",
                        TextNormalizationPolicy.FUNCTIONAL
                );

        assertTrue(result.isExactMatch());
    }

    @Test
    public void emptyExpectedAndActualHasZeroCer() {
        EditDistanceResult result =
                calculator.calculate(
                        "",
                        "",
                        TextNormalizationPolicy.RAW
                );

        assertEquals(
                0.0d,
                result.getCharacterErrorRate(),
                0.000001d
        );
    }

    @Test
    public void textWhenNothingExpectedHasFullError() {
        EditDistanceResult result =
                calculator.calculate(
                        "",
                        "NOISE",
                        TextNormalizationPolicy.RAW
                );

        assertEquals(
                1.0d,
                result.getCharacterErrorRate(),
                0.000001d
        );
    }
}
