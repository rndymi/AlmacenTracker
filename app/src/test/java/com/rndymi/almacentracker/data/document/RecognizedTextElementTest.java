package com.rndymi.almacentracker.core.document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class RecognizedTextElementTest {

    @Test
    public void elementCalculatesDimensionsAndCenter() {
        RecognizedTextElement element =
                new RecognizedTextElement(
                        "MR",
                        10,
                        20,
                        50,
                        60
                );

        assertEquals(40, element.getWidth());
        assertEquals(40, element.getHeight());
        assertEquals(
                40.0f,
                element.getCenterY(),
                0.001f
        );

        assertTrue(element.hasText());
    }

    @Test(expected = IllegalArgumentException.class)
    public void elementRejectsInvalidHorizontalBounds() {
        new RecognizedTextElement(
                "MR",
                50,
                20,
                10,
                60
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void elementRejectsInvalidVerticalBounds() {
        new RecognizedTextElement(
                "MR",
                10,
                60,
                50,
                20
        );
    }
}