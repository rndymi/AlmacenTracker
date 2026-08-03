package com.rndymi.almacentracker.data.document.onnx.detection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public final class TextProbabilityMapAnalyzerTest {

    private final PaddleTextDetectorConfiguration
            configuration =
            new PaddleTextDetectorConfiguration(
                    "x",
                    "fetch_name_0",
                    960,
                    32,
                    0.30f,
                    0.55f,
                    2,
                    1.0f,
                    0.80f,
                    0.50f
            );

    private final TextProbabilityMapAnalyzer analyzer =
            new TextProbabilityMapAnalyzer(
                    configuration
            );

    @Test
    public void analyze_emptyMapReturnsNoCandidates()
            throws Exception {
        float[][] map = new float[5][5];

        List<TextProbabilityMapAnalyzer.Candidate>
                result = analyzer.analyze(map);

        assertTrue(result.isEmpty());
    }

    @Test
    public void analyze_connectedPixelsCreateCandidate()
            throws Exception {
        float[][] map = new float[6][8];

        map[2][2] = 0.80f;
        map[2][3] = 0.90f;
        map[3][2] = 0.70f;
        map[3][3] = 0.80f;

        List<TextProbabilityMapAnalyzer.Candidate>
                result = analyzer.analyze(map);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getLeft());
        assertEquals(2, result.get(0).getTop());
        assertEquals(4, result.get(0).getRight());
        assertEquals(4, result.get(0).getBottom());
        assertEquals(
                0.80f,
                result.get(0).getConfidence(),
                0.001f
        );
    }

    @Test
    public void analyze_lowConfidenceComponentIsDiscarded()
            throws Exception {
        float[][] map = new float[5][5];

        map[1][1] = 0.31f;
        map[1][2] = 0.31f;
        map[2][1] = 0.31f;
        map[2][2] = 0.31f;

        List<TextProbabilityMapAnalyzer.Candidate>
                result = analyzer.analyze(map);

        assertTrue(result.isEmpty());
    }

    @Test
    public void analyze_smallNoiseIsDiscarded()
            throws Exception {
        float[][] map = new float[5][5];

        map[2][2] = 0.95f;

        List<TextProbabilityMapAnalyzer.Candidate>
                result = analyzer.analyze(map);

        assertTrue(result.isEmpty());
    }
}
