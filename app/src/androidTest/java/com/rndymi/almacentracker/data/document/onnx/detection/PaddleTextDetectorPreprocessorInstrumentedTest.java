package com.rndymi.almacentracker.data.document.onnx.detection;

import static org.junit.Assert.assertEquals;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.FloatBuffer;

import ai.onnxruntime.OrtEnvironment;

@RunWith(AndroidJUnit4.class)
public final class
PaddleTextDetectorPreprocessorInstrumentedTest {

    private Bitmap bitmap;

    @Before
    public void setUp() {
        bitmap = Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
        );

        bitmap.setPixel(
                0,
                0,
                Color.RED
        );
    }

    @After
    public void tearDown() {
        if (bitmap != null
                && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    @Test
    public void createNchwBuffer_writesBlackPaddingWithoutBitmapCopy() {
        PaddleTextDetectorConfiguration configuration =
                new PaddleTextDetectorConfiguration(
                        "x",
                        "fetch_name_0",
                        2,
                        2,
                        0.30f,
                        0.55f,
                        1,
                        1.50f,
                        0.80f,
                        0.50f
                );

        PaddleTextDetectorPreprocessor preprocessor =
                new PaddleTextDetectorPreprocessor(
                        OrtEnvironment.getEnvironment(),
                        configuration
                );

        DetectorImageTransform transform =
                preprocessor.createTransform(
                        1,
                        1
                );

        FloatBuffer buffer =
                preprocessor.createNchwBuffer(
                        bitmap,
                        transform
                );

        float[] values =
                new float[buffer.remaining()];

        buffer.get(values);

        assertEquals(
                12,
                values.length
        );

        // Canal rojo:
        // píxel rojo normalizado = 1
        // padding negro normalizado = -1
        assertEquals(
                1.0f,
                values[0],
                0.0001f
        );

        assertEquals(
                -1.0f,
                values[1],
                0.0001f
        );

        assertEquals(
                -1.0f,
                values[2],
                0.0001f
        );

        assertEquals(
                -1.0f,
                values[3],
                0.0001f
        );

        // Canal verde completo.
        for (int index = 4;
             index < 8;
             index++) {
            assertEquals(
                    -1.0f,
                    values[index],
                    0.0001f
            );
        }

        // Canal azul completo.
        for (int index = 8;
             index < 12;
             index++) {
            assertEquals(
                    -1.0f,
                    values[index],
                    0.0001f
            );
        }
    }
}
