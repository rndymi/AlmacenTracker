package com.rndymi.almacentracker.data.document;

import android.graphics.Bitmap;

import com.rndymi.almacentracker.core.document.DocumentImage;
import com.rndymi.almacentracker.core.document.DocumentImageSource;
import com.rndymi.almacentracker.core.document.DocumentRecognitionCallback;
import com.rndymi.almacentracker.core.document.DocumentTextRecognizer;
import com.rndymi.almacentracker.core.document.RecognizedDocument;
import com.rndymi.almacentracker.core.document.RecognizedTextElement;
import com.rndymi.almacentracker.core.document.RecognizedTextLine;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrInitializationResult;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeProvider;
import com.rndymi.almacentracker.data.document.onnx.detection.TextDetectionException;
import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextRegion;
import com.rndymi.almacentracker.data.document.onnx.model.TextDetectionResult;
import com.rndymi.almacentracker.data.document.onnx.model.TextRecognitionResult;
import com.rndymi.almacentracker.data.document.onnx.pipeline.TextRegionDetector;
import com.rndymi.almacentracker.data.document.onnx.pipeline.TextRegionRecognizer;
import com.rndymi.almacentracker.data.document.onnx.recognition.TextRecognitionException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

public final class PaddleOcrDocumentTextRecognizer
        implements DocumentTextRecognizer {

    private final Object requestLock = new Object();

    private final ExecutorService ocrExecutor;
    private final PaddleOcrRuntimeProvider runtimeProvider;
    private final TextRegionDetector detector;
    private final TextRegionRecognizer recognizer;
    private final DocumentLineReconstructor lineReconstructor;
    private final LongSupplier clock;

    private final AtomicBoolean closed =
            new AtomicBoolean(false);

    private final AtomicLong requestSequence =
            new AtomicLong(0L);

    private RecognitionRequest activeRequest;

    public PaddleOcrDocumentTextRecognizer(
            ExecutorService ocrExecutor,
            PaddleOcrRuntimeProvider runtimeProvider,
            TextRegionDetector detector,
            TextRegionRecognizer recognizer,
            DocumentLineReconstructor lineReconstructor,
            LongSupplier clock
    ) {
        this.ocrExecutor =
                Objects.requireNonNull(
                        ocrExecutor,
                        "ocrExecutor"
                );

        this.runtimeProvider =
                Objects.requireNonNull(
                        runtimeProvider,
                        "runtimeProvider"
                );

        this.detector =
                Objects.requireNonNull(
                        detector,
                        "detector"
                );

        this.recognizer =
                Objects.requireNonNull(
                        recognizer,
                        "recognizer"
                );

        this.lineReconstructor =
                Objects.requireNonNull(
                        lineReconstructor,
                        "lineReconstructor"
                );

        this.clock =
                Objects.requireNonNull(
                        clock,
                        "clock"
                );
    }

    @Override
    public void recognize(
            DocumentImage documentImage,
            DocumentImageSource sourceType,
            DocumentRecognitionCallback callback
    ) {
        Objects.requireNonNull(
                callback,
                "callback"
        );

        if (documentImage == null) {
            callback.onImageOpenError();
            return;
        }

        if (sourceType == null) {
            documentImage.close();
            callback.onRecognitionError();
            return;
        }

        RecognitionRequest request;

        synchronized (requestLock) {
            if (closed.get()) {
                documentImage.close();
                return;
            }

            if (activeRequest != null
                    && !activeRequest.isCompleted()) {
                documentImage.close();
                callback.onRecognitionError();
                return;
            }

            request =
                    new RecognitionRequest(
                            requestSequence.incrementAndGet(),
                            documentImage,
                            sourceType,
                            callback
                    );

            activeRequest = request;
        }

        if (!(documentImage
                instanceof AndroidDocumentImage)) {
            completeImageError(request);
            return;
        }

        if (documentImage.isClosed()
                || documentImage.getProcessedWidth() <= 0
                || documentImage.getProcessedHeight() <= 0) {
            completeImageError(request);
            return;
        }

        runtimeProvider.initialize(
                result -> handleRuntimeResult(
                        request,
                        result
                )
        );
    }

    private void handleRuntimeResult(
            RecognitionRequest request,
            PaddleOcrInitializationResult result
    ) {
        if (!isRequestActive(request)) {
            completeSilently(request);
            return;
        }

        if (result == null || !result.isReady()) {
            completeRecognitionError(request);
            return;
        }

        try {
            ocrExecutor.execute(
                    () -> processDocument(request)
            );
        } catch (RejectedExecutionException exception) {
            completeRecognitionError(request);
        }
    }

    private void processDocument(
            RecognitionRequest request
    ) {
        if (!isRequestActive(request)) {
            completeSilently(request);
            return;
        }

        Bitmap bitmap;

        try {
            AndroidDocumentImage androidImage =
                    (AndroidDocumentImage)
                            request.getDocumentImage();

            bitmap =
                    androidImage.getRecognitionBitmap();
        } catch (IllegalStateException
                 | ClassCastException exception) {
            completeImageError(request);
            return;
        }

        if (bitmap == null
                || bitmap.isRecycled()
                || bitmap.getWidth() <= 0
                || bitmap.getHeight() <= 0) {
            completeImageError(request);
            return;
        }

        try {
            TextDetectionResult detectionResult =
                    detector.detect(bitmap);

            List<DetectedTextRegion> regions =
                    orderedRegions(detectionResult);

            if (regions.isEmpty()) {
                completeSuccess(
                        request,
                        createDocument(
                                request.getSourceType(),
                                new ArrayList<>()
                        )
                );
                return;
            }

            List<RecognizedElementResult> recognized =
                    recognizeRegions(
                            request,
                            bitmap,
                            regions
                    );

            if (!isRequestActive(request)) {
                completeSilently(request);
                return;
            }

            List<RecognizedTextLine> lines =
                    buildLines(
                            recognized,
                            bitmap.getWidth()
                    );

            completeSuccess(
                    request,
                    createDocument(
                            request.getSourceType(),
                            lines
                    )
            );
        } catch (TextDetectionException
                 | TextRecognitionException
                 | IllegalArgumentException
                 | IllegalStateException exception) {
            completeRecognitionError(request);
        } catch (OutOfMemoryError error) {
            completeRecognitionError(request);
        } catch (RuntimeException exception) {
            completeRecognitionError(request);
        }
    }

    private List<DetectedTextRegion> orderedRegions(
            TextDetectionResult detectionResult
    ) {
        if (detectionResult == null
                || detectionResult.getRegions() == null) {
            throw new IllegalStateException(
                    "Detection result cannot be null"
            );
        }

        List<DetectedTextRegion> result =
                new ArrayList<>(
                        detectionResult.getRegions()
                );

        result.sort(
                Comparator.comparingInt(
                        DetectedTextRegion::getSourceOrder
                )
        );

        return result;
    }

    private List<RecognizedElementResult>
    recognizeRegions(
            RecognitionRequest request,
            Bitmap bitmap,
            List<DetectedTextRegion> regions
    ) throws TextRecognitionException {
        List<RecognizedElementResult> result =
                new ArrayList<>();

        for (DetectedTextRegion region : regions) {
            if (!isRequestActive(request)) {
                return result;
            }

            TextRecognitionResult recognitionResult =
                    recognizer.recognize(
                            bitmap,
                            region
                    );

            if (recognitionResult == null) {
                throw new IllegalStateException(
                        "Recognition result cannot be null"
                );
            }

            String text =
                    recognitionResult.getText().trim();

            if (text.isEmpty()) {
                continue;
            }

            RecognizedTextElement element =
                    mapElement(
                            recognitionResult,
                            bitmap.getWidth(),
                            bitmap.getHeight()
                    );

            result.add(
                    new RecognizedElementResult(
                            recognitionResult
                                    .getSourceRegion()
                                    .getSourceOrder(),
                            element
                    )
            );
        }

        result.sort(
                Comparator.comparingInt(
                        RecognizedElementResult
                                ::getSourceOrder
                )
        );

        return result;
    }

    private RecognizedTextElement mapElement(
            TextRecognitionResult result,
            int bitmapWidth,
            int bitmapHeight
    ) {
        DetectedTextRegion region =
                result.getSourceRegion();

        int left =
                clamp(
                        (int) Math.floor(
                                region.getLeft()
                        ),
                        0,
                        bitmapWidth
                );

        int top =
                clamp(
                        (int) Math.floor(
                                region.getTop()
                        ),
                        0,
                        bitmapHeight
                );

        int right =
                clamp(
                        (int) Math.ceil(
                                region.getRight()
                        ),
                        0,
                        bitmapWidth
                );

        int bottom =
                clamp(
                        (int) Math.ceil(
                                region.getBottom()
                        ),
                        0,
                        bitmapHeight
                );

        if (right < left || bottom < top) {
            throw new IllegalArgumentException(
                    "Recognized region has invalid bounds"
            );
        }

        return new RecognizedTextElement(
                result.getText(),
                left,
                top,
                right,
                bottom
        );
    }

    private List<RecognizedTextLine> buildLines(
            List<RecognizedElementResult> recognized,
            int documentWidth
    ) {
        if (recognized.isEmpty()) {
            return new ArrayList<>();
        }

        List<RecognizedTextElement> elements =
                new ArrayList<>(recognized.size());

        for (RecognizedElementResult result : recognized) {
            elements.add(result.getElement());
        }

        List<RecognizedTextLine> reconstructed =
                lineReconstructor.reconstruct(
                        elements,
                        documentWidth
                );

        if (reconstructed != null
                && !reconstructed.isEmpty()) {
            return reconstructed;
        }

        return createFallbackLines(recognized);
    }

    private List<RecognizedTextLine>
    createFallbackLines(
            List<RecognizedElementResult> recognized
    ) {
        List<RecognizedTextLine> lines =
                new ArrayList<>(recognized.size());

        int index = 0;

        for (RecognizedElementResult result : recognized) {
            RecognizedTextElement element =
                    result.getElement();

            List<RecognizedTextElement> lineElements =
                    new ArrayList<>();

            lineElements.add(element);

            lines.add(
                    new RecognizedTextLine(
                            index++,
                            element.getRawText(),
                            element.getRawText(),
                            element.getLeft(),
                            element.getTop(),
                            element.getRight(),
                            element.getBottom(),
                            lineElements
                    )
            );
        }

        return lines;
    }

    private RecognizedDocument createDocument(
            DocumentImageSource sourceType,
            List<RecognizedTextLine> lines
    ) {
        long recognizedAt =
                clock.getAsLong();

        if (recognizedAt <= 0L) {
            throw new IllegalStateException(
                    "Recognition time must be positive"
            );
        }

        return new RecognizedDocument(
                sourceType,
                lines,
                recognizedAt
        );
    }

    private int clamp(
            int value,
            int minimum,
            int maximum
    ) {
        return Math.max(
                minimum,
                Math.min(maximum, value)
        );
    }

    private boolean isRequestActive(
            RecognitionRequest request
    ) {
        synchronized (requestLock) {
            return !closed.get()
                    && activeRequest == request
                    && !request.isCompleted();
        }
    }

    private void completeSuccess(
            RecognitionRequest request,
            RecognizedDocument document
    ) {
        if (!completeRequest(request)) {
            return;
        }

        request.closeImage();

        if (!closed.get()) {
            request.getCallback().onSuccess(document);
        }
    }

    private void completeImageError(
            RecognitionRequest request
    ) {
        if (!completeRequest(request)) {
            return;
        }

        request.closeImage();

        if (!closed.get()) {
            request.getCallback().onImageOpenError();
        }
    }

    private void completeRecognitionError(
            RecognitionRequest request
    ) {
        if (!completeRequest(request)) {
            return;
        }

        request.closeImage();

        if (!closed.get()) {
            request.getCallback().onRecognitionError();
        }
    }

    private void completeSilently(
            RecognitionRequest request
    ) {
        if (!completeRequest(request)) {
            return;
        }

        request.closeImage();
    }

    private boolean completeRequest(
            RecognitionRequest request
    ) {
        if (!request.complete()) {
            return false;
        }

        synchronized (requestLock) {
            if (activeRequest == request) {
                activeRequest = null;
            }
        }

        return true;
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        RecognitionRequest request;

        synchronized (requestLock) {
            request = activeRequest;
            activeRequest = null;
        }

        if (request != null && request.complete()) {
            request.closeImage();
        }
    }

    private static final class RecognizedElementResult {

        private final int sourceOrder;
        private final RecognizedTextElement element;

        private RecognizedElementResult(
                int sourceOrder,
                RecognizedTextElement element
        ) {
            this.sourceOrder = sourceOrder;
            this.element =
                    Objects.requireNonNull(
                            element,
                            "element"
                    );
        }

        private int getSourceOrder() {
            return sourceOrder;
        }

        private RecognizedTextElement getElement() {
            return element;
        }
    }

    private static final class RecognitionRequest {

        private final long id;
        private final DocumentImage documentImage;
        private final DocumentImageSource sourceType;
        private final DocumentRecognitionCallback callback;
        private final AtomicBoolean completed =
                new AtomicBoolean(false);

        private RecognitionRequest(
                long id,
                DocumentImage documentImage,
                DocumentImageSource sourceType,
                DocumentRecognitionCallback callback
        ) {
            this.id = id;
            this.documentImage =
                    Objects.requireNonNull(
                            documentImage,
                            "documentImage"
                    );
            this.sourceType =
                    Objects.requireNonNull(
                            sourceType,
                            "sourceType"
                    );
            this.callback =
                    Objects.requireNonNull(
                            callback,
                            "callback"
                    );
        }

        private long getId() {
            return id;
        }

        private DocumentImage getDocumentImage() {
            return documentImage;
        }

        private DocumentImageSource getSourceType() {
            return sourceType;
        }

        private DocumentRecognitionCallback
        getCallback() {
            return callback;
        }

        private boolean complete() {
            return completed.compareAndSet(
                    false,
                    true
            );
        }

        private boolean isCompleted() {
            return completed.get();
        }

        private void closeImage() {
            documentImage.close();
        }
    }
}
