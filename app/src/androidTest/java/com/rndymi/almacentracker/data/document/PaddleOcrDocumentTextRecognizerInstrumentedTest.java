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
import android.os.Debug;
import android.os.SystemClock;

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

    @Test
    public void recognize_rejectsSecondConcurrentRequest()
            throws Exception {
        Bitmap firstBitmap =
                createDocumentBitmap();

        AndroidDocumentImage firstImage =
                new AndroidDocumentImage(
                        firstBitmap,
                        firstBitmap.getWidth(),
                        firstBitmap.getHeight(),
                        0
                );

        Bitmap secondBitmap =
                createDocumentBitmap();

        AndroidDocumentImage secondImage =
                new AndroidDocumentImage(
                        secondBitmap,
                        secondBitmap.getWidth(),
                        secondBitmap.getHeight(),
                        0
                );

        CountDownLatch firstLatch =
                new CountDownLatch(1);

        CountDownLatch secondLatch =
                new CountDownLatch(1);

        AtomicReference<String> firstError =
                new AtomicReference<>();

        AtomicReference<String> secondError =
                new AtomicReference<>();

        AtomicInteger firstCallbackCount =
                new AtomicInteger();

        AtomicInteger secondCallbackCount =
                new AtomicInteger();

        textRecognizer.recognize(
                firstImage,
                DocumentImageSource.PHOTO_PICKER,
                new DocumentRecognitionCallback() {

                    @Override
                    public void onSuccess(
                            RecognizedDocument document
                    ) {
                        firstCallbackCount.incrementAndGet();
                        firstLatch.countDown();
                    }

                    @Override
                    public void onImageOpenError() {
                        firstCallbackCount.incrementAndGet();
                        firstError.set("image");
                        firstLatch.countDown();
                    }

                    @Override
                    public void onRecognitionError() {
                        firstCallbackCount.incrementAndGet();
                        firstError.set("recognition");
                        firstLatch.countDown();
                    }
                }
        );

        textRecognizer.recognize(
                secondImage,
                DocumentImageSource.PHOTO_PICKER,
                new DocumentRecognitionCallback() {

                    @Override
                    public void onSuccess(
                            RecognizedDocument document
                    ) {
                        secondCallbackCount.incrementAndGet();
                        secondLatch.countDown();
                    }

                    @Override
                    public void onImageOpenError() {
                        secondCallbackCount.incrementAndGet();
                        secondError.set("image");
                        secondLatch.countDown();
                    }

                    @Override
                    public void onRecognitionError() {
                        secondCallbackCount.incrementAndGet();
                        secondError.set("recognition");
                        secondLatch.countDown();
                    }
                }
        );

        assertTrue(
                secondLatch.await(
                        5L,
                        TimeUnit.SECONDS
                )
        );

        assertEquals(
                "recognition",
                secondError.get()
        );

        assertEquals(
                1,
                secondCallbackCount.get()
        );

        assertTrue(
                secondImage.isClosed()
        );

        assertTrue(
                secondBitmap.isRecycled()
        );

        assertTrue(
                firstLatch.await(
                        RECOGNITION_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                )
        );

        assertEquals(
                null,
                firstError.get()
        );

        assertEquals(
                1,
                firstCallbackCount.get()
        );

        assertTrue(
                firstImage.isClosed()
        );

        assertTrue(
                firstBitmap.isRecycled()
        );
    }

    @Test
    public void recognize_warmRunsDoNotRetainUnboundedMemory()
            throws Exception {
        recognizeAndAwaitSuccess();

        forceBestEffortCollection();

        long memoryAfterWarmUp =
                approximateUsedMemoryBytes();

        for (int runIndex = 0;
             runIndex < SEQUENTIAL_RUN_COUNT;
             runIndex++) {
            recognizeAndAwaitSuccess();
        }

        forceBestEffortCollection();

        long memoryAfterRepeatedRuns =
                approximateUsedMemoryBytes();

        long retainedGrowth =
                Math.max(
                        0L,
                        memoryAfterRepeatedRuns
                                - memoryAfterWarmUp
                );

        long diagnosticLimit =
                64L * 1024L * 1024L;

        assertTrue(
                "Approximate retained memory after warm-up grew by "
                        + retainedGrowth
                        + " bytes. "
                        + "memoryAfterWarmUp="
                        + memoryAfterWarmUp
                        + ", memoryAfterRepeatedRuns="
                        + memoryAfterRepeatedRuns,
                retainedGrowth <= diagnosticLimit
        );
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

    private void recognizeAndAwaitSuccess()
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

        AtomicReference<RecognizedDocument>
                document =
                new AtomicReference<>();

        AtomicReference<String> error =
                new AtomicReference<>();

        textRecognizer.recognize(
                documentImage,
                DocumentImageSource.PHOTO_PICKER,
                new DocumentRecognitionCallback() {

                    @Override
                    public void onSuccess(
                            RecognizedDocument result
                    ) {
                        document.set(result);
                        latch.countDown();
                    }

                    @Override
                    public void onImageOpenError() {
                        error.set("image");
                        latch.countDown();
                    }

                    @Override
                    public void onRecognitionError() {
                        error.set("recognition");
                        latch.countDown();
                    }
                }
        );

        assertTrue(
                latch.await(
                        RECOGNITION_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                )
        );

        assertEquals(
                null,
                error.get()
        );

        assertNotNull(
                document.get()
        );

        assertTrue(
                documentImage.isClosed()
        );

        assertTrue(
                bitmap.isRecycled()
        );
    }

    private void forceBestEffortCollection() {
        Runtime.getRuntime().gc();
        System.runFinalization();
        SystemClock.sleep(250L);

        Runtime.getRuntime().gc();
        SystemClock.sleep(250L);
    }

    private long approximateUsedMemoryBytes() {
        Runtime runtime =
                Runtime.getRuntime();

        long javaUsed =
                runtime.totalMemory()
                        - runtime.freeMemory();

        long nativeUsed =
                Debug.getNativeHeapAllocatedSize();

        return javaUsed + nativeUsed;
    }
}
