package com.rndymi.almacentracker.data.document.onnx.detection;

import static androidx.test.core.app.ApplicationProvider
        .getApplicationContext;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rndymi.almacentracker.data.document.onnx.OnnxModelAssetLoader;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrInitializationResult;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrModelConfiguration;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeInitializer;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeProvider;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrSessionBundle;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrSessionMetadataValidator;
import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextRegion;
import com.rndymi.almacentracker.data.document.onnx.model.TextDetectionResult;

import ai.onnxruntime.OrtEnvironment;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(AndroidJUnit4.class)
public final class PaddleTextDetectorInstrumentedTest {

    @Test
    public void bundledDetector_executesRealInference()
            throws Exception {
        Context context = getApplicationContext();

        PaddleOcrRuntimeInitializer initializer =
                new PaddleOcrRuntimeInitializer(
                        OrtEnvironment.getEnvironment(),
                        new OnnxModelAssetLoader(
                                context.getAssets()
                        ),
                        PaddleOcrModelConfiguration.bundled(),
                        new PaddleOcrSessionMetadataValidator()
                );

        PaddleOcrInitializationResult
                initializationResult =
                initializer.initialize();

        assertTrue(
                "Initialization failed with: "
                        + initializationResult.getError(),
                initializationResult.isReady()
        );

        PaddleOcrSessionBundle sessionBundle =
                initializationResult.getSessionBundle();

        assertNotNull(sessionBundle);

        ExecutorService executor =
                Executors.newSingleThreadExecutor();

        PaddleOcrRuntimeProvider provider =
                new PaddleOcrRuntimeProvider(
                        executor,
                        () -> initializationResult
                );

        provider.initialize(result -> {
        });

        waitUntilReady(provider);

        PaddleTextDetectorConfiguration configuration =
                PaddleTextDetectorConfiguration
                        .defaultConfiguration();

        PaddleTextDetector detector =
                new PaddleTextDetector(
                        provider,
                        configuration,
                        new PaddleTextDetectorPreprocessor(
                                OrtEnvironment.getEnvironment(),
                                configuration
                        ),
                        new PaddleTextDetectorPostProcessor(
                                configuration
                        )
                );

        Bitmap bitmap = createTestBitmap();

        try {
            TextDetectionResult result =
                    detector.detect(bitmap);

            assertNotNull(result);
            assertTrue(result.getSourceWidth() > 0);
            assertTrue(result.getSourceHeight() > 0);
            assertTrue(result.getInferenceWidth() > 0);
            assertTrue(result.getInferenceHeight() > 0);
            assertTrue(
                    result.getPreprocessDurationMs() >= 0
            );
            assertTrue(
                    result.getInferenceDurationMs() >= 0
            );
            assertTrue(
                    result.getPostprocessDurationMs() >= 0
            );

            for (DetectedTextRegion region
                    : result.getRegions()) {
                assertTrue(region.getLeft() >= 0.0f);
                assertTrue(region.getTop() >= 0.0f);
                assertTrue(
                        region.getRight()
                                <= bitmap.getWidth()
                );
                assertTrue(
                        region.getBottom()
                                <= bitmap.getHeight()
                );
                assertTrue(region.getWidth() > 0.0f);
                assertTrue(region.getHeight() > 0.0f);
            }

            assertFalse(sessionBundle.isClosed());

            TextDetectionResult secondResult =
                    detector.detect(bitmap);

            assertNotNull(secondResult);
            assertFalse(sessionBundle.isClosed());
        } finally {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }

            provider.close();
            executor.shutdownNow();
        }

        assertTrue(sessionBundle.isClosed());
    }

    private Bitmap createTestBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(
                640,
                320,
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint(
                Paint.ANTI_ALIAS_FLAG
        );
        paint.setColor(Color.BLACK);
        paint.setTextSize(72.0f);

        canvas.drawText(
                "MR 1210",
                40.0f,
                120.0f,
                paint
        );
        canvas.drawText(
                "4 CAJAS",
                40.0f,
                230.0f,
                paint
        );

        return bitmap;
    }

    private void waitUntilReady(
            PaddleOcrRuntimeProvider provider
    ) throws InterruptedException {
        long timeoutAt =
                System.currentTimeMillis() + 5_000L;

        while (provider.getState()
                == PaddleOcrRuntimeProvider.State
                .INITIALIZING
                && System.currentTimeMillis()
                < timeoutAt) {
            Thread.sleep(20L);
        }

        assertTrue(
                provider.getState()
                        == PaddleOcrRuntimeProvider.State.READY
        );
    }
}
