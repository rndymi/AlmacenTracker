package com.rndymi.almacentracker.app.di;

import android.content.Context;
import android.graphics.Bitmap;

import com.rndymi.almacentracker.core.document.DocumentImageLoader;
import com.rndymi.almacentracker.core.document.DocumentImageProcessor;
import com.rndymi.almacentracker.core.document.DocumentTextRecognizer;
import com.rndymi.almacentracker.data.document.AndroidDocumentImageLoader;
import com.rndymi.almacentracker.data.document.AndroidDocumentImageProcessor;
import com.rndymi.almacentracker.data.document.DocumentLineReconstructor;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrModelManifest;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeProvider;
import com.rndymi.almacentracker.data.document.onnx.detection.PaddleTextDetector;
import com.rndymi.almacentracker.data.document.onnx.detection.PaddleTextDetectorConfiguration;
import com.rndymi.almacentracker.data.document.onnx.detection.PaddleTextDetectorPostProcessor;
import com.rndymi.almacentracker.data.document.onnx.detection.PaddleTextDetectorPreprocessor;
import com.rndymi.almacentracker.data.document.onnx.recognition.PaddleTextRecognizer;
import com.rndymi.almacentracker.data.document.onnx.recognition.PaddleTextRecognizerConfiguration;
import com.rndymi.almacentracker.data.document.onnx.recognition.PaddleTextRecognizerPreprocessor;
import com.rndymi.almacentracker.data.document.PaddleOcrDocumentTextRecognizer;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.reference.DocumentReferenceDataParser;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceParser;
import com.rndymi.almacentracker.feature.reference_list.capture.ReferenceListCaptureViewModelFactory;
import com.rndymi.almacentracker.feature.reference_list.review.ReferenceListReviewViewModelFactory;

import java.util.concurrent.ExecutorService;
import java.util.Objects;

import ai.onnxruntime.OrtEnvironment;

public final class ReferenceListModule {

    private final Context applicationContext;
    private final DocumentImageLoader<Bitmap> imageLoader;
    private final WarehouseItemRepository repository;
    private final ExecutorService ocrExecutor;
    private final PaddleOcrRuntimeProvider paddleOcrRuntimeProvider;
    private final PaddleTextDetector paddleTextDetector;
    private final PaddleTextRecognizer paddleTextRecognizer;

    public ReferenceListModule(
            Context context,
            WarehouseItemRepository repository,
            ExecutorService ocrExecutor,
            PaddleOcrRuntimeProvider paddleOcrRuntimeProvider
    ) {
        applicationContext =
                Objects.requireNonNull(
                        context,
                        "context"
                ).getApplicationContext();

        imageLoader =
                new AndroidDocumentImageLoader(
                        applicationContext
                );

        this.repository =
                Objects.requireNonNull(
                        repository,
                        "repository"
                );

        this.ocrExecutor =
                Objects.requireNonNull(
                        ocrExecutor,
                        "ocrExecutor"
                );

        this.paddleOcrRuntimeProvider =
                Objects.requireNonNull(
                        paddleOcrRuntimeProvider,
                        "paddleOcrRuntimeProvider"
                );

        PaddleTextDetectorConfiguration detectorConfiguration =
                PaddleTextDetectorConfiguration
                        .defaultConfiguration();

        PaddleTextDetectorPreprocessor detectorPreprocessor =
                new PaddleTextDetectorPreprocessor(
                        OrtEnvironment.getEnvironment(),
                        detectorConfiguration
                );

        PaddleTextDetectorPostProcessor detectorPostProcessor =
                new PaddleTextDetectorPostProcessor(
                        detectorConfiguration
                );

        paddleTextDetector =
                new PaddleTextDetector(
                        this.paddleOcrRuntimeProvider,
                        detectorConfiguration,
                        detectorPreprocessor,
                        detectorPostProcessor
                );

        PaddleTextRecognizerConfiguration recognizerConfiguration =
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

        PaddleTextRecognizerPreprocessor recognizerPreprocessor =
                new PaddleTextRecognizerPreprocessor(
                        OrtEnvironment.getEnvironment(),
                        recognizerConfiguration
                );

        paddleTextRecognizer =
                new PaddleTextRecognizer(
                        this.paddleOcrRuntimeProvider,
                        recognizerConfiguration,
                        recognizerPreprocessor
                );
    }

    public PaddleOcrRuntimeProvider
    providePaddleOcrRuntimeProvider() {
        return paddleOcrRuntimeProvider;
    }

    public PaddleTextDetector providePaddleTextDetector() {
        return paddleTextDetector;
    }

    public PaddleTextRecognizer providePaddleTextRecognizer() {
        return paddleTextRecognizer;
    }

    public ReferenceListCaptureViewModelFactory
    provideReferenceListCaptureViewModelFactory() {
        DocumentImageProcessor imageProcessor =
                new AndroidDocumentImageProcessor(
                        applicationContext
                );

        DocumentTextRecognizer recognizer =
                new PaddleOcrDocumentTextRecognizer(
                        ocrExecutor,
                        paddleOcrRuntimeProvider,
                        paddleTextDetector,
                        paddleTextRecognizer,
                        new DocumentLineReconstructor(),
                        System::currentTimeMillis
                );

        return new ReferenceListCaptureViewModelFactory(
                imageProcessor,
                recognizer
        );
    }

    public ReferenceListReviewViewModelFactory
    provideReferenceListReviewViewModelFactory() {
        return new ReferenceListReviewViewModelFactory(
                new WarehouseReferenceParser(),
                repository,
                new DocumentReferenceDataParser()
        );
    }

    public DocumentImageLoader<Bitmap>
    provideDocumentImageLoader() {
        return imageLoader;
    }
}