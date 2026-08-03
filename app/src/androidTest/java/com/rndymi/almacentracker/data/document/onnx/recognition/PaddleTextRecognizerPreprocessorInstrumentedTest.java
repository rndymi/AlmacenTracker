package com.rndymi.almacentracker.data.document.onnx.recognition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextPolygon;
import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextRegion;

import org.junit.Test;
import org.junit.runner.RunWith;

import ai.onnxruntime.OrtEnvironment;

@RunWith(AndroidJUnit4.class)
public final class
PaddleTextRecognizerPreprocessorInstrumentedTest {

    @Test
    public void prepare_createsNchwTensorWithFixedHeight()
            throws Exception {
        Bitmap bitmap = Bitmap.createBitmap(
                320,
                120,
                Bitmap.Config.ARGB_8888
        );

        bitmap.eraseColor(0xFFFFFFFF);

        DetectedTextRegion region =
                new DetectedTextRegion(
                        DetectedTextPolygon.rectangle(
                                20.0f,
                                20.0f,
                                300.0f,
                                100.0f
                        ),
                        0.90f,
                        0
                );

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

        PaddleTextRecognizerPreprocessor preprocessor =
                new PaddleTextRecognizerPreprocessor(
                        OrtEnvironment.getEnvironment(),
                        configuration
                );

        try (RecognizerInput input =
                     preprocessor.prepare(
                             bitmap,
                             region
                     )) {
            long[] shape =
                    input.getTensor().getInfo().getShape();

            assertEquals(4, shape.length);
            assertEquals(1L, shape[0]);
            assertEquals(3L, shape[1]);
            assertEquals(48L, shape[2]);
            assertEquals(
                    input.getPaddedWidth(),
                    shape[3]
            );

            assertTrue(
                    input.getContentWidth() > 0
            );
            assertTrue(
                    input.getPaddedWidth()
                            >= input.getContentWidth()
            );
        }

        assertFalse(bitmap.isRecycled());
        bitmap.recycle();
    }
}
