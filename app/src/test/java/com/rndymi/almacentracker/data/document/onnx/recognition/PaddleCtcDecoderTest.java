package com.rndymi.almacentracker.data.document.onnx.recognition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.data.document.onnx.PaddleOcrDictionary;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

public final class PaddleCtcDecoderTest {

    @Test
    public void decode_removesBlankAndCollapsesRepeatedTokens()
            throws Exception {
        PaddleOcrDictionary dictionary =
                PaddleOcrDictionary.fromUtf8(
                        "M\nR\n1\n2\n".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        PaddleTextRecognizerConfiguration configuration =
                configuration();

        PaddleCtcDecoder decoder =
                new PaddleCtcDecoder(
                        configuration,
                        dictionary
                );

        float[][] logits = new float[][]{
                logitsFor(0),
                logitsFor(1),
                logitsFor(1),
                logitsFor(0),
                logitsFor(2),
                logitsFor(3),
                logitsFor(3),
                logitsFor(4)
        };

        CtcDecodingResult result =
                decoder.decode(logits);

        assertEquals(
                "MR12",
                result.getText()
        );
        assertTrue(
                result.getConfidence() > 0.90f
        );
    }

    @Test
    public void decode_ignoresAdditionalSpecialToken()
            throws Exception {
        PaddleOcrDictionary dictionary =
                PaddleOcrDictionary.fromUtf8(
                        "M\nR\n1\n2\n".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        PaddleCtcDecoder decoder =
                new PaddleCtcDecoder(
                        configuration(),
                        dictionary
                );

        CtcDecodingResult result =
                decoder.decode(
                        new float[][]{
                                logitsFor(1),
                                logitsFor(5),
                                logitsFor(2)
                        }
                );

        assertEquals(
                "MR",
                result.getText()
        );
    }

    @Test
    public void decode_acceptsEmptyText()
            throws Exception {
        PaddleOcrDictionary dictionary =
                PaddleOcrDictionary.fromUtf8(
                        "M\nR\n1\n2\n".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        PaddleCtcDecoder decoder =
                new PaddleCtcDecoder(
                        configuration(),
                        dictionary
                );

        CtcDecodingResult result =
                decoder.decode(
                        new float[][]{
                                logitsFor(0),
                                logitsFor(0)
                        }
                );

        assertEquals(
                "",
                result.getText()
        );
        assertEquals(
                0.0f,
                result.getConfidence(),
                0.0f
        );
    }

    private PaddleTextRecognizerConfiguration
    configuration() {
        return new PaddleTextRecognizerConfiguration(
                "x",
                "fetch_name_0",
                48,
                8,
                64,
                8,
                0,
                6,
                1,
                0.45f,
                2.50f
        );
    }

    private float[] logitsFor(
            int selectedIndex
    ) {
        float[] logits =
                new float[]{
                        -10.0f,
                        -10.0f,
                        -10.0f,
                        -10.0f,
                        -10.0f,
                        -10.0f
                };

        logits[selectedIndex] = 10.0f;
        return logits;
    }
}
