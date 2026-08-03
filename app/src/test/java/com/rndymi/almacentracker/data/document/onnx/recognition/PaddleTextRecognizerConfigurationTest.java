package com.rndymi.almacentracker.data.document.onnx.recognition;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class PaddleTextRecognizerConfigurationTest {

    @Test
    public void constructor_preservesValidatedValues() {
        PaddleTextRecognizerConfiguration configuration =
                new PaddleTextRecognizerConfiguration(
                        "x",
                        "fetch_name_0",
                        48,
                        32,
                        2048,
                        8,
                        0,
                        18385,
                        1,
                        0.45f,
                        2.50f
                );

        assertEquals(
                "x",
                configuration.getInputName()
        );
        assertEquals(
                "fetch_name_0",
                configuration.getOutputName()
        );
        assertEquals(
                48,
                configuration.getFixedHeight()
        );
        assertEquals(
                18385,
                configuration.getClassCount()
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_rejectsBlankIndexOutsideClassRange() {
        new PaddleTextRecognizerConfiguration(
                "x",
                "fetch_name_0",
                48,
                32,
                2048,
                8,
                18385,
                18385,
                1,
                0.45f,
                2.50f
        );
    }
}
