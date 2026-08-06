package com.rndymi.almacentracker.app.di;

import android.content.Context;

import com.rndymi.almacentracker.data.document.DocumentLineReconstructor;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeProvider;
import com.rndymi.almacentracker.data.document.onnx.detection.PaddleTextDetector;
import com.rndymi.almacentracker.data.document.onnx.detection.PaddleTextDetectorConfiguration;
import com.rndymi.almacentracker.data.document.onnx.detection.PaddleTextDetectorPostProcessor;
import com.rndymi.almacentracker.data.document.onnx.detection.PaddleTextDetectorPreprocessor;
import com.rndymi.almacentracker.data.document.onnx.recognition.PaddleTextRecognizer;
import com.rndymi.almacentracker.data.document.onnx.recognition.PaddleTextRecognizerConfiguration;
import com.rndymi.almacentracker.data.document.onnx.recognition.PaddleTextRecognizerPreprocessor;

import java.util.concurrent.ExecutorService;
import java.util.Objects;

import ai.onnxruntime.OrtEnvironment;

// androidTest factory.
// Reuse exactly the same manifest, configuration and runtime provider
// values that ReferenceListModule uses in production.
public final class OcrEvaluationComponentFactory {

    private OcrEvaluationComponentFactory() {
    }

    public static OcrEvaluationComponents create(
            Context context,
            ExecutorService executor,
            PaddleOcrRuntimeProvider runtimeProvider
    ) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(executor, "executor");
        PaddleOcrRuntimeProvider checkedRuntimeProvider =
                Objects.requireNonNull(
                        runtimeProvider,
                        "runtimeProvider"
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

        PaddleTextDetector detector =
                new PaddleTextDetector(
                        checkedRuntimeProvider,
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

        PaddleTextRecognizer recognizer =
                new PaddleTextRecognizer(
                        checkedRuntimeProvider,
                        recognizerConfiguration,
                        recognizerPreprocessor
                );

        return new OcrEvaluationComponents(
                detector,
                recognizer,
                checkedRuntimeProvider,
                new DocumentLineReconstructor()
        );
    }
}
