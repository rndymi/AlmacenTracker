package com.rndymi.almacentracker.data.document.onnx.detection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextPolygon;
import com.rndymi.almacentracker.data.document.onnx.model.TextPoint;

import org.junit.Test;

public final class DetectorImageTransformTest {

    @Test
    public void restorePoint_removesPaddingAndScale() {
        DetectorImageTransform transform =
                new DetectorImageTransform(
                        1000,
                        500,
                        800,
                        400,
                        800,
                        416,
                        0,
                        0,
                        0,
                        16
                );

        TextPoint restored = transform.restorePoint(
                400.0f,
                200.0f
        );

        assertEquals(
                500.0f,
                restored.getX(),
                0.001f
        );
        assertEquals(
                250.0f,
                restored.getY(),
                0.001f
        );
    }

    @Test
    public void restoreRectangle_clampsToSourceBounds() {
        DetectorImageTransform transform =
                new DetectorImageTransform(
                        100,
                        50,
                        100,
                        50,
                        128,
                        64,
                        0,
                        0,
                        28,
                        14
                );

        DetectedTextPolygon polygon =
                transform.restoreRectangle(
                        -10.0f,
                        -10.0f,
                        130.0f,
                        70.0f
                );

        assertEquals(
                0.0f,
                polygon.getLeft(),
                0.001f
        );
        assertEquals(
                0.0f,
                polygon.getTop(),
                0.001f
        );
        assertEquals(
                100.0f,
                polygon.getRight(),
                0.001f
        );
        assertEquals(
                50.0f,
                polygon.getBottom(),
                0.001f
        );
    }

    @Test
    public void isInsideContent_rejectsPadding() {
        DetectorImageTransform transform =
                new DetectorImageTransform(
                        100,
                        50,
                        100,
                        50,
                        128,
                        64,
                        0,
                        0,
                        28,
                        14
                );

        assertTrue(
                transform.isInsideContent(
                        99.0f,
                        49.0f
                )
        );
        assertFalse(
                transform.isInsideContent(
                        120.0f,
                        55.0f
                )
        );
    }
}
