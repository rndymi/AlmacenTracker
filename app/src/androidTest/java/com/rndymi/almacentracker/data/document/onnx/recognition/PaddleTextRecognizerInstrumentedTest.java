package com.rndymi.almacentracker.data.document.onnx.recognition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rndymi.almacentracker.data.document.onnx.OnnxModelAssetLoader;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrModelConfiguration;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeInitializer;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeProvider;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrSessionMetadataValidator;
import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextPolygon;
import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextRegion;
import com.rndymi.almacentracker.data.document.onnx.model.TextRecognitionResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import ai.onnxruntime.OrtEnvironment;

@RunWith(AndroidJUnit4.class)
public final class PaddleTextRecognizerInstrumentedTest {

    private ExecutorService executorService;
    private PaddleOcrRuntimeProvider runtimeProvider;

    @Before
    public void setUp() throws Exception {
        Context context =
                ApplicationProvider.getApplicationContext();

        PaddleOcrModelConfiguration modelConfiguration =
                PaddleOcrModelConfiguration.bundled();

        OnnxModelAssetLoader assetLoader =
                new OnnxModelAssetLoader(
                        context.getAssets()
                );

        PaddleOcrRuntimeInitializer initializer =
                new PaddleOcrRuntimeInitializer(
                        OrtEnvironment.getEnvironment(),
                        assetLoader,
                        modelConfiguration,
                        new PaddleOcrSessionMetadataValidator()
                );

        executorService =
                Executors.newSingleThreadExecutor();

        runtimeProvider =
                new PaddleOcrRuntimeProvider(
                        executorService,
                        initializer
                );

        CountDownLatch latch =
                new CountDownLatch(1);

        AtomicReference<Throwable> failure =
                new AtomicReference<>();

        runtimeProvider.initialize(result -> {
            if (!result.isReady()) {
                failure.set(
                        new AssertionError(
                                "Runtime initialization failed: "
                                        + result.getError()
                        )
                );
            }

            latch.countDown();
        });

        assertTrue(
                latch.await(
                        30L,
                        TimeUnit.SECONDS
                )
        );

        if (failure.get() != null) {
            throw new AssertionError(
                    failure.get()
            );
        }
    }

    @After
    public void tearDown() {
        if (runtimeProvider != null) {
            runtimeProvider.close();
        }

        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @Test
    public void recognize_runsRealModelAndPreservesSourceRegion()
            throws Exception {
        Bitmap bitmap =
                createTextBitmap("MR 1210");

        DetectedTextRegion region =
                new DetectedTextRegion(
                        DetectedTextPolygon.rectangle(
                                0.0f,
                                0.0f,
                                bitmap.getWidth(),
                                bitmap.getHeight()
                        ),
                        0.95f,
                        3
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

        PaddleTextRecognizer recognizer =
                new PaddleTextRecognizer(
                        runtimeProvider,
                        configuration,
                        new PaddleTextRecognizerPreprocessor(
                                OrtEnvironment.getEnvironment(),
                                configuration
                        )
                );

        TextRecognitionResult result =
                recognizer.recognize(
                        bitmap,
                        region
                );

        assertNotNull(result);
        assertNotNull(result.getText());
        assertEquals(
                3,
                result.getSourceRegion()
                        .getSourceOrder()
        );
        assertTrue(
                result.getConfidence() >= 0.0f
        );
        assertTrue(
                result.getConfidence() <= 1.0f
        );
        assertFalse(bitmap.isRecycled());

        bitmap.recycle();
    }

    private Bitmap createTextBitmap(
            String text
    ) {
        Bitmap bitmap = Bitmap.createBitmap(
                480,
                96,
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0xFFFFFFFF);

        Paint paint = new Paint(
                Paint.ANTI_ALIAS_FLAG
        );

        paint.setColor(0xFF000000);
        paint.setTextSize(58.0f);
        paint.setTypeface(
                Typeface.create(
                        Typeface.DEFAULT,
                        Typeface.BOLD
                )
        );

        canvas.drawText(
                text,
                16.0f,
                68.0f,
                paint
        );

        return bitmap;
    }
}
