package com.rndymi.almacentracker.feature.inventory.list;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class FastScrollPositionCalculatorTest {

    @Test
    public void viewportFractionRepresentsCurrentListPosition() {
        assertEquals(
                0f,
                FastScrollPositionCalculator
                        .fractionForViewport(
                                0,
                                9,
                                100
                        ),
                0.001f
        );
        assertEquals(
                0.5f,
                FastScrollPositionCalculator
                        .fractionForViewport(
                                45,
                                54,
                                100
                        ),
                0.001f
        );
        assertEquals(
                1f,
                FastScrollPositionCalculator
                        .fractionForViewport(
                                90,
                                99,
                                100
                        ),
                0.001f
        );
    }

    @Test
    public void draggedFractionMapsToFirstVisiblePosition() {
        assertEquals(
                0,
                FastScrollPositionCalculator
                        .positionForFraction(
                                0f,
                                1897,
                                8
                        )
        );
        assertEquals(
                945,
                FastScrollPositionCalculator
                        .positionForFraction(
                                0.5f,
                                1897,
                                8
                        )
        );
        assertEquals(
                1889,
                FastScrollPositionCalculator
                        .positionForFraction(
                                1f,
                                1897,
                                8
                        )
        );
    }

    @Test
    public void calculationsClampOutOfRangeValues() {
        assertEquals(
                0,
                FastScrollPositionCalculator
                        .positionForFraction(
                                -1f,
                                100,
                                10
                        )
        );
        assertEquals(
                90,
                FastScrollPositionCalculator
                        .positionForFraction(
                                2f,
                                100,
                                10
                        )
        );
    }
}
