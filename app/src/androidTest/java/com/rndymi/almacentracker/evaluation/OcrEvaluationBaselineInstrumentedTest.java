package com.rndymi.almacentracker.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.rndymi.almacentracker.app.di.OcrEvaluationComponentFactory;
import com.rndymi.almacentracker.app.di.OcrEvaluationComponents;
import com.rndymi.almacentracker.core.document.DocumentImage;
import com.rndymi.almacentracker.core.document.DocumentImageProcessingCallback;
import com.rndymi.almacentracker.core.document.DocumentImageProcessingRequest;
import com.rndymi.almacentracker.core.document.DocumentImageSource;
import com.rndymi.almacentracker.core.document.DocumentRecognitionCallback;
import com.rndymi.almacentracker.core.document.RecognizedDocument;
import com.rndymi.almacentracker.data.document.AndroidDocumentImageProcessor;
import com.rndymi.almacentracker.data.document.PaddleOcrDocumentTextRecognizer;
import com.rndymi.almacentracker.data.document.onnx.OnnxModelAssetLoader;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrInitializationResult;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrModelConfiguration;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeInitializer;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeProvider;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrSessionMetadataValidator;
import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextRegion;
import com.rndymi.almacentracker.data.document.onnx.model.TextDetectionResult;
import com.rndymi.almacentracker.data.document.onnx.model.TextRecognitionResult;
import com.rndymi.almacentracker.data.document.onnx.pipeline.TextRegionDetector;
import com.rndymi.almacentracker.data.document.onnx.pipeline.TextRegionRecognizer;
import com.rndymi.almacentracker.domain.reference.DocumentReferenceData;
import com.rndymi.almacentracker.domain.reference.DocumentReferenceDataParser;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceMatch;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceParser;
import com.rndymi.almacentracker.evaluation.io.OcrEvaluationCorpus;
import com.rndymi.almacentracker.evaluation.io.OcrEvaluationCorpusLoader;
import com.rndymi.almacentracker.evaluation.io.OcrExpectedResultLoader;
import com.rndymi.almacentracker.evaluation.metrics.CharacterErrorRateCalculator;
import com.rndymi.almacentracker.evaluation.metrics.DetectionMetrics;
import com.rndymi.almacentracker.evaluation.metrics.DetectionMetricsCalculator;
import com.rndymi.almacentracker.evaluation.metrics.EditDistanceResult;
import com.rndymi.almacentracker.evaluation.metrics.InterpretationMetrics;
import com.rndymi.almacentracker.evaluation.metrics.NormalizedBox;
import com.rndymi.almacentracker.evaluation.metrics.ReconstructionMetrics;
import com.rndymi.almacentracker.evaluation.metrics.ReconstructionMetricsCalculator;
import com.rndymi.almacentracker.evaluation.metrics.TextNormalizationPolicy;
import com.rndymi.almacentracker.evaluation.model.OcrEvaluationCase;
import com.rndymi.almacentracker.evaluation.model.OcrEvaluationResult;
import com.rndymi.almacentracker.evaluation.model.OcrExpectedLine;
import com.rndymi.almacentracker.evaluation.model.OcrExpectedReference;
import com.rndymi.almacentracker.evaluation.model.OcrExpectedRegion;
import com.rndymi.almacentracker.evaluation.model.OcrExpectedResult;
import com.rndymi.almacentracker.evaluation.performance.MemorySampler;
import com.rndymi.almacentracker.evaluation.performance.MemorySnapshot;
import com.rndymi.almacentracker.evaluation.performance.PerformanceMetrics;
import com.rndymi.almacentracker.evaluation.report.OcrEvaluationReportWriter;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import ai.onnxruntime.OrtEnvironment;

@RunWith(AndroidJUnit4.class)
public final class OcrEvaluationBaselineInstrumentedTest {

    private static final long INITIALIZATION_TIMEOUT_SECONDS = 120L;
    private static final long IMAGE_TIMEOUT_SECONDS = 30L;
    private static final long OCR_TIMEOUT_SECONDS = 180L;
    private static final double DETECTION_MINIMUM_IOU = 0.50d;

    @Test
    public void executeCorpusAndWriteBaselineReports()
            throws Exception {
        Context testContext = InstrumentationRegistry
                .getInstrumentation().getContext();
        Context targetContext = InstrumentationRegistry
                .getInstrumentation().getTargetContext();

        OcrEvaluationCorpus corpus = new OcrEvaluationCorpusLoader(
                testContext.getAssets()
        ).load("ocr/evaluation/corpus_manifest.json");
        OcrExpectedResultLoader expectedLoader =
                new OcrExpectedResultLoader(testContext.getAssets());
        OcrEvaluationReportWriter reportWriter = reportWriter(targetContext);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        AndroidDocumentImageProcessor imageProcessor =
                new AndroidDocumentImageProcessor(targetContext);
        PaddleOcrRuntimeProvider runtimeProvider = null;
        PaddleOcrDocumentTextRecognizer documentRecognizer = null;

        try {
            runtimeProvider = runtimeProvider(targetContext, executor);
            OcrEvaluationComponents components =
                    OcrEvaluationComponentFactory.create(
                            targetContext,
                            executor,
                            runtimeProvider
                    );
            CapturingDetector detector = new CapturingDetector(
                    components.getDetector()
            );
            CapturingRecognizer recognizer = new CapturingRecognizer(
                    components.getRecognizer()
            );
            documentRecognizer = new PaddleOcrDocumentTextRecognizer(
                    executor,
                    runtimeProvider,
                    detector,
                    recognizer,
                    components.getLineReconstructor(),
                    System::currentTimeMillis
            );

            boolean coldStart = true;
            for (OcrEvaluationCase evaluationCase : corpus.getCases()) {
                OcrExpectedResult expected = expectedLoader.load(
                        evaluationCase.getExpectedPath()
                );
                assertEquals(evaluationCase.getId(), expected.getCaseId());

                OcrEvaluationResult result = executeCase(
                        testContext,
                        targetContext,
                        evaluationCase,
                        expected,
                        imageProcessor,
                        documentRecognizer,
                        runtimeProvider,
                        detector,
                        recognizer,
                        corpus.getCorpusVersion(),
                        coldStart
                );
                coldStart = false;

                File json = reportWriter.writeJson(result);
                File markdown = reportWriter.writeMarkdown(result);
                assertReport(json, evaluationCase.getId(), "\"status\": \"SUCCESS\"");
                assertReport(markdown, evaluationCase.getId(), "## Performance");
            }

            File[] reports = reportWriter.getOutputDirectory().listFiles();
            assertNotNull("Cannot list generated OCR reports", reports);
            assertEquals(
                    "Every corpus case must create JSON and Markdown",
                    corpus.getCases().size() * 2,
                    reports.length
            );
            android.util.Log.i(
                    "OcrEvaluation",
                    "OCR baseline reports: "
                            + reportWriter.getOutputDirectory()
                            .getAbsolutePath()
            );
        } finally {
            if (documentRecognizer != null) {
                documentRecognizer.close();
            }
            imageProcessor.close();
            if (runtimeProvider != null) {
                runtimeProvider.close();
                assertEquals(
                        PaddleOcrRuntimeProvider.State.CLOSED,
                        runtimeProvider.getState()
                );
            }
            executor.shutdownNow();
            assertTrue(
                    "OCR executor did not terminate",
                    executor.awaitTermination(10L, TimeUnit.SECONDS)
            );
        }
    }

    @Test
    public void reportWriterFailsWhenExternalStorageIsUnavailable() {
        Context targetContext = InstrumentationRegistry
                .getInstrumentation().getTargetContext();
        Context unavailableExternalStorage = new ContextWrapper(
                targetContext
        ) {
            @Override
            public File getExternalFilesDir(String type) {
                return null;
            }
        };

        try {
            new OcrEvaluationReportWriter(unavailableExternalStorage);
            fail("Writer accepted an unavailable external files directory");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("unavailable"));
        }
    }

    private OcrEvaluationResult executeCase(
            Context testContext,
            Context targetContext,
            OcrEvaluationCase evaluationCase,
            OcrExpectedResult expected,
            AndroidDocumentImageProcessor imageProcessor,
            PaddleOcrDocumentTextRecognizer documentRecognizer,
            PaddleOcrRuntimeProvider runtimeProvider,
            CapturingDetector detector,
            CapturingRecognizer recognizer,
            String corpusVersion,
            boolean coldStart
    ) throws Exception {
        detector.reset();
        recognizer.reset();
        MemorySampler memorySampler = new MemorySampler();
        MemorySnapshot memoryBefore = memorySampler.capture();
        memorySampler.start();
        long totalStartedAt = SystemClock.elapsedRealtime();
        DocumentImage image = null;

        try {
            long initializationMs = coldStart
                    ? initializeRuntime(runtimeProvider)
                    : 0L;
            ProcessedImage processedImage = processImage(
                    testContext,
                    targetContext,
                    imageProcessor,
                    evaluationCase
            );
            image = processedImage.image;
            int imageWidth = image.getProcessedWidth();
            int imageHeight = image.getProcessedHeight();

            RecognizedDocument document = recognize(
                    documentRecognizer,
                    image,
                    sourceType(evaluationCase)
            );
            assertTrue(
                    "Pipeline did not close image for "
                            + evaluationCase.getId(),
                    image.isClosed()
            );

            TextDetectionResult detectionResult = detector.getResult();
            assertNotNull(
                    "Detector produced no result for "
                            + evaluationCase.getId(),
                    detectionResult
            );
            long interpretationStartedAt = SystemClock.elapsedRealtime();
            EvaluationMetrics metrics = metrics(
                    expected,
                    document,
                    detectionResult,
                    imageWidth,
                    imageHeight
            );
            long interpretationMs = SystemClock.elapsedRealtime()
                    - interpretationStartedAt;
            memorySampler.close();
            MemorySnapshot memoryPeak = memorySampler.getPeak();
            MemorySnapshot memoryAfter = memorySampler.capture();
            long totalMs = SystemClock.elapsedRealtime() - totalStartedAt;

            PerformanceMetrics performance = new PerformanceMetrics(
                    coldStart,
                    initializationMs,
                    processedImage.durationMs,
                    detectionResult.getPreprocessDurationMs(),
                    detectionResult.getInferenceDurationMs(),
                    detectionResult.getPostprocessDurationMs(),
                    recognizer.getPreprocessDurationMs(),
                    recognizer.getInferenceDurationMs(),
                    recognizer.getDecodeDurationMs(),
                    0L,
                    interpretationMs,
                    totalMs,
                    memoryBefore,
                    memoryPeak,
                    memoryAfter
            );

            List<String> limitations = new ArrayList<>();
            limitations.add(
                    "Document reconstruction duration is not exposed "
                            + "separately by the production recognizer."
            );
            limitations.add(
                    "Room recovery is reported as zero because the public "
                            + "corpus does not seed the application database."
            );
            if (coldStart) {
                limitations.add(
                        "The corpus contains one case, so this run records "
                                + "a cold execution but no statistical warm sample."
                );
            }

            return new OcrEvaluationResult(
                    evaluationCase.getId(),
                    corpusVersion,
                    imageWidth,
                    imageHeight,
                    document.getReconstructedLines(),
                    metrics.parsedReferences,
                    expected.getExpectedTitle(),
                    expected.getExpectedBuyerOrStore(),
                    expected.getExpectedGlobalDestination(),
                    metrics.detection,
                    metrics.recognition,
                    metrics.reconstruction,
                    metrics.interpretation,
                    performance,
                    limitations
            );
        } finally {
            memorySampler.close();
            if (image != null && !image.isClosed()) {
                image.close();
            }
        }
    }

    private EvaluationMetrics metrics(
            OcrExpectedResult expected,
            RecognizedDocument document,
            TextDetectionResult detectionResult,
            int imageWidth,
            int imageHeight
    ) {
        List<NormalizedBox> expectedBoxes = new ArrayList<>();
        for (OcrExpectedRegion region : expected.getRegions()) {
            if (!region.isOptional()) {
                expectedBoxes.add(region.getBox());
            }
        }
        List<NormalizedBox> detectedBoxes = new ArrayList<>();
        for (DetectedTextRegion region : detectionResult.getRegions()) {
            double left = clamp(region.getLeft() / imageWidth);
            double top = clamp(region.getTop() / imageHeight);
            double right = clamp(region.getRight() / imageWidth);
            double bottom = clamp(region.getBottom() / imageHeight);
            if (right > left && bottom > top) {
                detectedBoxes.add(new NormalizedBox(
                        left,
                        top,
                        right,
                        bottom
                ));
            }
        }
        DetectionMetrics detection = new DetectionMetricsCalculator(
                DETECTION_MINIMUM_IOU
        ).calculate(expectedBoxes, detectedBoxes);

        List<String> expectedLines = new ArrayList<>();
        List<Integer> globalIndexes = new ArrayList<>();
        for (int index = 0; index < expected.getLines().size(); index++) {
            OcrExpectedLine line = expected.getLines().get(index);
            expectedLines.add(line.getText());
            if (line.isGlobalLine()) {
                globalIndexes.add(index);
            }
        }
        List<String> actualLines = document.getReconstructedLines();
        EditDistanceResult recognition = new CharacterErrorRateCalculator()
                .calculate(
                        String.join("\n", expectedLines),
                        String.join("\n", actualLines),
                        TextNormalizationPolicy.FUNCTIONAL
                );
        ReconstructionMetrics reconstruction =
                new ReconstructionMetricsCalculator().calculate(
                        expectedLines,
                        actualLines,
                        globalIndexes
                );
        InterpretationResult interpretation = interpretation(
                expected,
                actualLines
        );
        return new EvaluationMetrics(
                detection,
                recognition,
                reconstruction,
                interpretation.metrics,
                interpretation.parsedReferences
        );
    }

    private InterpretationResult interpretation(
            OcrExpectedResult expected,
            List<String> actualLines
    ) {
        WarehouseReferenceParser referenceParser =
                new WarehouseReferenceParser();
        DocumentReferenceDataParser dataParser =
                new DocumentReferenceDataParser();
        List<DocumentReferenceData> parsedData = new ArrayList<>();
        List<String> parsedReferences = new ArrayList<>();

        for (int index = 0; index < actualLines.size(); index++) {
            List<WarehouseReferenceMatch> matches = referenceParser
                    .parseOcrLine(index, actualLines.get(index),
                            Collections.emptyList());
            for (WarehouseReferenceMatch match : matches) {
                DocumentReferenceData data = dataParser.parse(match);
                parsedData.add(data);
                parsedReferences.add(data.getReference().displayValue());
            }
        }

        Map<String, OcrExpectedReference> expectedByIdentity =
                new HashMap<>();
        for (OcrExpectedReference reference : expected.getReferences()) {
            expectedByIdentity.put(reference.getIdentity(), reference);
        }
        Set<String> matchedIdentities = new HashSet<>();
        int falsePositives = 0;
        int correctQuantities = 0;
        int correctUnits = 0;
        int correctDestinations = 0;

        for (DocumentReferenceData data : parsedData) {
            String identity = data.getReference().getCategory()
                    + data.getReference().getCode();
            OcrExpectedReference expectedReference =
                    expectedByIdentity.get(identity);
            if (expectedReference == null) {
                falsePositives++;
                continue;
            }
            matchedIdentities.add(identity);
            if (expectedReference.getExpectedQuantity() != null
                    && data.getQuantity() != null
                    && expectedReference.getExpectedQuantity().equals(
                    String.valueOf(data.getQuantity()))) {
                correctQuantities++;
            }
            if (expectedReference.getExpectedUnit() != null
                    && expectedReference.getExpectedUnit().equals(
                    data.getUnit())) {
                correctUnits++;
            }
            for (String destination :
                    expectedReference.getExpectedDestinations()) {
                if (data.getDestinations().contains(destination)) {
                    correctDestinations++;
                }
            }
        }

        int expectedQuantities = 0;
        int expectedUnits = 0;
        int expectedDestinations = 0;
        for (OcrExpectedReference reference : expected.getReferences()) {
            if (reference.getExpectedQuantity() != null) {
                expectedQuantities++;
            }
            if (reference.getExpectedUnit() != null) {
                expectedUnits++;
            }
            expectedDestinations += reference.getExpectedDestinations().size();
        }
        int missed = expected.getReferences().size()
                - matchedIdentities.size();
        InterpretationMetrics metrics = new InterpretationMetrics(
                expected.getReferences().size(),
                parsedData.size(),
                matchedIdentities.size(),
                0,
                0,
                0,
                missed,
                falsePositives,
                missed,
                correctQuantities,
                expectedQuantities,
                correctUnits,
                expectedUnits,
                correctDestinations,
                expectedDestinations,
                0,
                expected.getExpectedTitle() == null ? 0 : 1
        );
        return new InterpretationResult(metrics, parsedReferences);
    }

    private long initializeRuntime(PaddleOcrRuntimeProvider provider)
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<PaddleOcrInitializationResult> result =
                new AtomicReference<>();
        long startedAt = SystemClock.elapsedRealtime();
        provider.initialize(value -> {
            result.set(value);
            latch.countDown();
        });
        assertTrue(
                "PP-OCRv5 initialization timed out",
                latch.await(INITIALIZATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        );
        long duration = SystemClock.elapsedRealtime() - startedAt;
        assertNotNull("PP-OCRv5 returned no initialization result", result.get());
        assertTrue(
                "PP-OCRv5 initialization failed: "
                        + result.get().getError(),
                result.get().isReady()
        );
        return duration;
    }

    private ProcessedImage processImage(
            Context testContext,
            Context targetContext,
            AndroidDocumentImageProcessor processor,
            OcrEvaluationCase evaluationCase
    ) throws Exception {
        File input = copyAssetToCache(
                testContext,
                targetContext,
                evaluationCase
        );
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DocumentImage> image = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();
        long startedAt = SystemClock.elapsedRealtime();

        try {
            processor.process(
                    new DocumentImageProcessingRequest(
                            input.toURI().toString(),
                            evaluationCase.getExpectedOrientationDegrees()
                    ),
                    new DocumentImageProcessingCallback() {
                        @Override
                        public void onSuccess(DocumentImage value) {
                            image.set(value);
                            latch.countDown();
                        }

                        @Override
                        public void onImageOpenError() {
                            error.set("image-open-error");
                            latch.countDown();
                        }

                        @Override
                        public void onProcessingError() {
                            error.set("image-processing-error");
                            latch.countDown();
                        }
                    }
            );
            assertTrue(
                    "Image processing timed out for " + evaluationCase.getId(),
                    latch.await(IMAGE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            );
            if (error.get() != null) {
                fail("Cannot process corpus image " + evaluationCase.getId()
                        + ": " + error.get());
            }
            assertNotNull(
                    "Image processor returned no image for "
                            + evaluationCase.getId(),
                    image.get()
            );
            return new ProcessedImage(
                    image.get(),
                    SystemClock.elapsedRealtime() - startedAt
            );
        } finally {
            if (!input.delete() && input.exists()) {
                android.util.Log.w(
                        "OcrEvaluation",
                        "Cannot delete temporary input " + input
                );
            }
        }
    }

    private RecognizedDocument recognize(
            PaddleOcrDocumentTextRecognizer recognizer,
            DocumentImage image,
            DocumentImageSource sourceType
    ) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<RecognizedDocument> document = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();
        recognizer.recognize(image, sourceType, new DocumentRecognitionCallback() {
            @Override
            public void onSuccess(RecognizedDocument value) {
                document.set(value);
                latch.countDown();
            }

            @Override
            public void onImageOpenError() {
                error.set("image-open-error");
                latch.countDown();
            }

            @Override
            public void onRecognitionError() {
                error.set("recognition-error");
                latch.countDown();
            }
        });
        assertTrue(
                "OCR timed out after " + OCR_TIMEOUT_SECONDS + " seconds",
                latch.await(OCR_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        );
        if (error.get() != null) {
            fail("Full PP-OCRv5 pipeline failed: " + error.get());
        }
        assertNotNull("OCR callback returned no document", document.get());
        return document.get();
    }

    private PaddleOcrRuntimeProvider runtimeProvider(
            Context context,
            ExecutorService executor
    ) {
        return new PaddleOcrRuntimeProvider(
                executor,
                new PaddleOcrRuntimeInitializer(
                        OrtEnvironment.getEnvironment(),
                        new OnnxModelAssetLoader(context.getAssets()),
                        PaddleOcrModelConfiguration.bundled(),
                        new PaddleOcrSessionMetadataValidator()
                )
        );
    }

    private OcrEvaluationReportWriter reportWriter(Context targetContext) {
        Bundle arguments = InstrumentationRegistry.getArguments();
        String additionalOutput = arguments.getString(
                "additionalTestOutputDir"
        );
        if (additionalOutput != null && !additionalOutput.trim().isEmpty()) {
            return new OcrEvaluationReportWriter(
                    new File(additionalOutput, "ocr-evaluation")
            );
        }
        return new OcrEvaluationReportWriter(targetContext);
    }

    private File copyAssetToCache(
            Context testContext,
            Context targetContext,
            OcrEvaluationCase evaluationCase
    ) throws IOException {
        File directory = new File(
                targetContext.getCacheDir(),
                "ocr-evaluation-inputs"
        );
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException(
                    "Cannot create OCR evaluation input directory: "
                            + directory
            );
        }
        File output = new File(directory, evaluationCase.getId() + ".png");
        try (InputStream input = testContext.getAssets().open(
                evaluationCase.getImagePath());
             FileOutputStream stream = new FileOutputStream(output)) {
            byte[] buffer = new byte[8 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                stream.write(buffer, 0, read);
            }
        }
        return output;
    }

    private DocumentImageSource sourceType(OcrEvaluationCase value) {
        return "camera".equalsIgnoreCase(value.getSourceType())
                ? DocumentImageSource.CAMERA
                : DocumentImageSource.PHOTO_PICKER;
    }

    private void assertReport(File file, String caseId, String content)
            throws IOException {
        assertTrue("Report was not generated: " + file, file.isFile());
        assertTrue("Report is empty: " + file, file.length() > 0L);
        String text = new String(
                java.nio.file.Files.readAllBytes(file.toPath()),
                java.nio.charset.StandardCharsets.UTF_8
        );
        assertTrue("Report does not contain case id", text.contains(caseId));
        assertTrue("Report is missing required content", text.contains(content));
    }

    private double clamp(double value) {
        return Math.max(0.0d, Math.min(1.0d, value));
    }

    private static final class CapturingDetector
            implements TextRegionDetector {
        private final TextRegionDetector delegate;
        private final AtomicReference<TextDetectionResult> result =
                new AtomicReference<>();

        private CapturingDetector(TextRegionDetector delegate) {
            this.delegate = delegate;
        }

        @Override
        public TextDetectionResult detect(Bitmap bitmap)
                throws com.rndymi.almacentracker.data.document.onnx.detection.TextDetectionException {
            TextDetectionResult value = delegate.detect(bitmap);
            result.set(value);
            return value;
        }

        private TextDetectionResult getResult() {
            return result.get();
        }

        private void reset() {
            result.set(null);
        }
    }

    private static final class CapturingRecognizer
            implements TextRegionRecognizer {
        private final TextRegionRecognizer delegate;
        private final AtomicLong preprocessMs = new AtomicLong();
        private final AtomicLong inferenceMs = new AtomicLong();
        private final AtomicLong decodeMs = new AtomicLong();

        private CapturingRecognizer(TextRegionRecognizer delegate) {
            this.delegate = delegate;
        }

        @Override
        public TextRecognitionResult recognize(
                Bitmap bitmap,
                DetectedTextRegion region
        ) throws com.rndymi.almacentracker.data.document.onnx.recognition.TextRecognitionException {
            TextRecognitionResult value = delegate.recognize(bitmap, region);
            preprocessMs.addAndGet(value.getPreprocessDurationMs());
            inferenceMs.addAndGet(value.getInferenceDurationMs());
            decodeMs.addAndGet(value.getDecodeDurationMs());
            return value;
        }

        private long getPreprocessDurationMs() {
            return preprocessMs.get();
        }

        private long getInferenceDurationMs() {
            return inferenceMs.get();
        }

        private long getDecodeDurationMs() {
            return decodeMs.get();
        }

        private void reset() {
            preprocessMs.set(0L);
            inferenceMs.set(0L);
            decodeMs.set(0L);
        }
    }

    private static final class ProcessedImage {
        private final DocumentImage image;
        private final long durationMs;

        private ProcessedImage(DocumentImage image, long durationMs) {
            this.image = image;
            this.durationMs = durationMs;
        }
    }

    private static final class EvaluationMetrics {
        private final DetectionMetrics detection;
        private final EditDistanceResult recognition;
        private final ReconstructionMetrics reconstruction;
        private final InterpretationMetrics interpretation;
        private final List<String> parsedReferences;

        private EvaluationMetrics(
                DetectionMetrics detection,
                EditDistanceResult recognition,
                ReconstructionMetrics reconstruction,
                InterpretationMetrics interpretation,
                List<String> parsedReferences
        ) {
            this.detection = detection;
            this.recognition = recognition;
            this.reconstruction = reconstruction;
            this.interpretation = interpretation;
            this.parsedReferences = parsedReferences;
        }
    }

    private static final class InterpretationResult {
        private final InterpretationMetrics metrics;
        private final List<String> parsedReferences;

        private InterpretationResult(
                InterpretationMetrics metrics,
                List<String> parsedReferences
        ) {
            this.metrics = metrics;
            this.parsedReferences = parsedReferences;
        }
    }
}
