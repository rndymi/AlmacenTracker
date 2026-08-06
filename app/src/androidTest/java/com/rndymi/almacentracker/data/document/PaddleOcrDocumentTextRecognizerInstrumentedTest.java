package com.rndymi.almacentracker.data.document;

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

import com.rndymi.almacentracker.core.document.DocumentImageSource;
import com.rndymi.almacentracker.core.document.DocumentRecognitionCallback;
import com.rndymi.almacentracker.core.document.RecognizedDocument;
import com.rndymi.almacentracker.data.document.onnx.OnnxModelAssetLoader;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrModelConfiguration;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeInitializer;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeProvider;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrSessionMetadataValidator;
import com.rndymi.almacentracker.data.document.onnx.detection.PaddleTextDetector;
import com.rndymi.almacentracker.data.document.onnx.detection.PaddleTextDetectorConfiguration;
import com.rndymi.almacentracker.data.document.onnx.detection.PaddleTextDetectorPostProcessor;
import com.rndymi.almacentracker.data.document.onnx.detection.PaddleTextDetectorPreprocessor;
import com.rndymi.almacentracker.data.document.onnx.recognition.PaddleTextRecognizer;
import com.rndymi.almacentracker.data.document.onnx.recognition.PaddleTextRecognizerConfiguration;
import com.rndymi.almacentracker.data.document.onnx.recognition.PaddleTextRecognizerPreprocessor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import ai.onnxruntime.OrtEnvironment;

@RunWith(AndroidJUnit4.class)
public final class
PaddleOcrDocumentTextRecognizerInstrumentedTest {

    private static final int SEQUENTIAL_RUN_COUNT = 5;
    private static final long RECOGNITION_TIMEOUT_SECONDS = 60L;

    private ExecutorService ocrExecutor;
    private PaddleOcrRuntimeProvider runtimeProvider;
    private PaddleOcrDocumentTextRecognizer textRecognizer;

    @Before
    public void setUp() {
        Context context =
                ApplicationProvider
                        .getApplicationContext();

        OrtEnvironment environment =
                OrtEnvironment.getEnvironment();

        ocrExecutor =
                Executors.newSingleThreadExecutor();

        PaddleOcrRuntimeInitializer initializer =
                new PaddleOcrRuntimeInitializer(
                        environment,
                        new OnnxModelAssetLoader(
                                context.getAssets()
                        ),
                        PaddleOcrModelConfiguration
                                .bundled(),
                        new PaddleOcrSessionMetadataValidator()
                );

        runtimeProvider =
                new PaddleOcrRuntimeProvider(
                        ocrExecutor,
                        initializer
                );

        PaddleTextDetectorConfiguration
                detectorConfiguration =
                PaddleTextDetectorConfiguration
                        .defaultConfiguration();

        PaddleTextDetector detector =
                new PaddleTextDetector(
                        runtimeProvider,
                        detectorConfiguration,
                        new PaddleTextDetectorPreprocessor(
                                environment,
                                detectorConfiguration
                        ),
                        new PaddleTextDetectorPostProcessor(
                                detectorConfiguration
                        )
                );

        PaddleTextRecognizerConfiguration
                recognizerConfiguration =
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
                        recognizerConfiguration,
                        new PaddleTextRecognizerPreprocessor(
                                environment,
                                recognizerConfiguration
                        )
                );

        textRecognizer =
                new PaddleOcrDocumentTextRecognizer(
                        ocrExecutor,
                        runtimeProvider,
                        detector,
                        recognizer,
                        new DocumentLineReconstructor(),
                        () -> 1_722_640_000_000L
                );
    }

    @After
    public void tearDown() {
        if (textRecognizer != null) {
            textRecognizer.close();
        }

        if (runtimeProvider != null) {
            runtimeProvider.close();
        }

        if (ocrExecutor != null) {
            ocrExecutor.shutdownNow();
        }
    }

    @Test
    public void recognize_processesCompleteImageAndClosesIt()
            throws Exception {
        Bitmap bitmap =
                createDocumentBitmap();

        AndroidDocumentImage documentImage =
                new AndroidDocumentImage(
                        bitmap,
                        bitmap.getWidth(),
                        bitmap.getHeight(),
                        0
                );

        CountDownLatch latch =
                new CountDownLatch(1);

        AtomicReference<RecognizedDocument> document =
                new AtomicReference<>();

        AtomicReference<String> error =
                new AtomicReference<>();

        AtomicInteger callbackCount =
                new AtomicInteger();

        textRecognizer.recognize(
                documentImage,
                DocumentImageSource.PHOTO_PICKER,
                new DocumentRecognitionCallback() {

                    @Override
                    public void onSuccess(
                            RecognizedDocument result
                    ) {
                        callbackCount.incrementAndGet();
                        document.set(result);
                        latch.countDown();
                    }

                    @Override
                    public void onImageOpenError() {
                        callbackCount.incrementAndGet();
                        error.set("image");
                        latch.countDown();
                    }

                    @Override
                    public void onRecognitionError() {
                        callbackCount.incrementAndGet();
                        error.set("recognition");
                        latch.countDown();
                    }
                }
        );

        assertTrue(
                latch.await(
                        60L,
                        TimeUnit.SECONDS
                )
        );

        assertEquals(null, error.get());
        assertEquals(1, callbackCount.get());
        assertNotNull(document.get());
        assertEquals(
                DocumentImageSource.PHOTO_PICKER,
                document.get().getSourceType()
        );
        assertEquals(
                1_722_640_000_000L,
                document.get().getRecognizedAt()
        );
        assertTrue(documentImage.isClosed());
        assertTrue(bitmap.isRecycled());
    }

    @Test
    public void close_isIdempotentAndRejectsLateCallbacks()
            throws Exception {
        Bitmap bitmap =
                createDocumentBitmap();

        AndroidDocumentImage documentImage =
                new AndroidDocumentImage(
                        bitmap,
                        bitmap.getWidth(),
                        bitmap.getHeight(),
                        0
                );

        CountDownLatch callbackLatch =
                new CountDownLatch(1);

        AtomicInteger callbackCount =
                new AtomicInteger();

        textRecognizer.recognize(
                documentImage,
                DocumentImageSource.CAMERA,
                new DocumentRecognitionCallback() {

                    @Override
                    public void onSuccess(
                            RecognizedDocument document
                    ) {
                        callbackCount.incrementAndGet();
                        callbackLatch.countDown();
                    }

                    @Override
                    public void onImageOpenError() {
                        callbackCount.incrementAndGet();
                        callbackLatch.countDown();
                    }

                    @Override
                    public void onRecognitionError() {
                        callbackCount.incrementAndGet();
                        callbackLatch.countDown();
                    }
                }
        );

        textRecognizer.close();
        textRecognizer.close();

        assertFalse(
                callbackLatch.await(
                        2L,
                        TimeUnit.SECONDS
                )
        );

        assertEquals(0, callbackCount.get());
        assertTrue(documentImage.isClosed());
    }

    @Test
    public void recognize_nullImageReportsImageOpenError() {
        AtomicReference<String> callback =
                new AtomicReference<>();

        textRecognizer.recognize(
                null,
                DocumentImageSource.PHOTO_PICKER,
                new DocumentRecognitionCallback() {
                    @Override
                    public void onSuccess(
                            RecognizedDocument document
                    ) {
                        callback.set("success");
                    }

                    @Override
                    public void onImageOpenError() {
                        callback.set("image");
                    }

                    @Override
                    public void onRecognitionError() {
                        callback.set("recognition");
                    }
                }
        );

        assertEquals("image", callback.get());
    }

    @Test
    public void recognize_nullSourceReportsErrorAndClosesImage() {
        Bitmap bitmap = createDocumentBitmap();
        AndroidDocumentImage documentImage =
                new AndroidDocumentImage(
                        bitmap,
                        bitmap.getWidth(),
                        bitmap.getHeight(),
                        0
                );
        AtomicReference<String> callback =
                new AtomicReference<>();

        textRecognizer.recognize(
                documentImage,
                null,
                new DocumentRecognitionCallback() {
                    @Override
                    public void onSuccess(
                            RecognizedDocument document
                    ) {
                        callback.set("success");
                    }

                    @Override
                    public void onImageOpenError() {
                        callback.set("image");
                    }

                    @Override
                    public void onRecognitionError() {
                        callback.set("recognition");
                    }
                }
        );

        assertEquals("recognition", callback.get());
        assertTrue(documentImage.isClosed());
        assertTrue(bitmap.isRecycled());
    }

    @Test
    public void recognize_supportsSeveralSequentialExecutions()
            throws Exception {
        for (int runIndex = 0;
             runIndex < SEQUENTIAL_RUN_COUNT;
             runIndex++) {

            Bitmap bitmap =
                    createDocumentBitmap();

            AndroidDocumentImage documentImage =
                    new AndroidDocumentImage(
                            bitmap,
                            bitmap.getWidth(),
                            bitmap.getHeight(),
                            0
                    );

            CountDownLatch latch =
                    new CountDownLatch(1);

            AtomicReference<RecognizedDocument>
                    document =
                    new AtomicReference<>();

            AtomicReference<String> error =
                    new AtomicReference<>();

            AtomicInteger callbackCount =
                    new AtomicInteger();

            textRecognizer.recognize(
                    documentImage,
                    DocumentImageSource.PHOTO_PICKER,
                    new DocumentRecognitionCallback() {

                        @Override
                        public void onSuccess(
                                RecognizedDocument result
                        ) {
                            callbackCount.incrementAndGet();
                            document.set(result);
                            latch.countDown();
                        }

                        @Override
                        public void onImageOpenError() {
                            callbackCount.incrementAndGet();
                            error.set("image");
                            latch.countDown();
                        }

                        @Override
                        public void onRecognitionError() {
                            callbackCount.incrementAndGet();
                            error.set("recognition");
                            latch.countDown();
                        }
                    }
            );

            assertTrue(
                    "OCR run timed out: "
                            + runIndex,
                    latch.await(
                            RECOGNITION_TIMEOUT_SECONDS,
                            TimeUnit.SECONDS
                    )
            );

            assertEquals(
                    "Unexpected error on OCR run "
                            + runIndex,
                    null,
                    error.get()
            );

            assertEquals(
                    "Each run must emit one terminal callback",
                    1,
                    callbackCount.get()
            );

            assertNotNull(
                    "Each run must return a document",
                    document.get()
            );

            assertTrue(
                    "DocumentImage must be closed after run "
                            + runIndex,
                    documentImage.isClosed()
            );

            assertTrue(
                    "Owned bitmap must be recycled after run "
                            + runIndex,
                    bitmap.isRecycled()
            );
        }
    }

    private Bitmap createDocumentBitmap() {
        Bitmap bitmap =
                Bitmap.createBitmap(
                        960,
                        480,
                        Bitmap.Config.ARGB_8888
                );

        Canvas canvas =
                new Canvas(bitmap);

        canvas.drawColor(
                android.graphics.Color.WHITE
        );

        Paint paint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(
                android.graphics.Color.BLACK
        );

        paint.setTextSize(88.0f);
        paint.setTypeface(
                Typeface.create(
                        Typeface.DEFAULT,
                        Typeface.BOLD
                )
        );

        canvas.drawText(
                "MR 1210",
                70.0f,
                170.0f,
                paint
        );

        canvas.drawText(
                "MZ 1300A",
                70.0f,
                330.0f,
                paint
        );

        return bitmap;
    }
}
