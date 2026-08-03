package com.rndymi.almacentracker.data.document.onnx.detection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextRegion;

import org.junit.Test;

import java.util.List;

public final class PaddleTextDetectorPostProcessorTest {

    private PaddleTextDetectorPostProcessor
    createPostProcessor() {
        return new PaddleTextDetectorPostProcessor(
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
                )
        );
    }

    @Test
    public void process_emptyMapReturnsEmptyResult()
            throws Exception {
        float[][] map = new float[8][8];

        DetectorImageTransform transform =
                new DetectorImageTransform(
                        80,
                        80,
                        80,
                        80,
                        80,
                        80,
                        0,
                        0,
                        0,
                        0
                );

        List<DetectedTextRegion> result =
                createPostProcessor().process(
                        map,
                        transform
                );

        assertTrue(result.isEmpty());
    }

    @Test
    public void process_restoresCoordinates()
            throws Exception {
        float[][] map = new float[10][10];

        for (int y = 2; y < 5; y++) {
            for (int x = 3; x < 7; x++) {
                map[y][x] = 0.90f;
            }
        }

        DetectorImageTransform transform =
                new DetectorImageTransform(
                        200,
                        100,
                        100,
                        50,
                        100,
                        50,
                        0,
                        0,
                        0,
                        0
                );

        List<DetectedTextRegion> result =
                createPostProcessor().process(
                        map,
                        transform
                );

        assertEquals(1, result.size());

        DetectedTextRegion region =
                result.get(0);

        assertEquals(
                60.0f,
                region.getLeft(),
                0.01f
        );
        assertEquals(
                20.0f,
                region.getTop(),
                0.01f
        );
        assertEquals(
                140.0f,
                region.getRight(),
                0.01f
        );
        assertEquals(
                50.0f,
                region.getBottom(),
                0.01f
        );
        assertEquals(0, region.getSourceOrder());
    }

    @Test
    public void process_ordersSameLineFromLeftToRight()
            throws Exception {
        float[][] map = new float[10][20];

        fill(map, 2, 2, 5, 5, 0.90f);
        fill(map, 12, 2, 16, 5, 0.90f);

        DetectorImageTransform transform =
                new DetectorImageTransform(
                        200,
                        100,
                        200,
                        100,
                        200,
                        100,
                        0,
                        0,
                        0,
                        0
                );

        List<DetectedTextRegion> result =
                createPostProcessor().process(
                        map,
                        transform
                );

        assertEquals(2, result.size());
        assertTrue(
                result.get(0).getLeft()
                        < result.get(1).getLeft()
        );
        assertEquals(
                0,
                result.get(0).getSourceOrder()
        );
        assertEquals(
                1,
                result.get(1).getSourceOrder()
        );
    }

    private void fill(
            float[][] map,
            int left,
            int top,
            int right,
            int bottom,
            float value
    ) {
        for (int y = top; y < bottom; y++) {
            for (int x = left; x < right; x++) {
                map[y][x] = value;
            }
        }
    }
}
