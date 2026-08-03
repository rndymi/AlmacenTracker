package com.rndymi.almacentracker.data.document.onnx.recognition;

import android.graphics.Bitmap;

import com.rndymi.almacentracker.data.document.onnx.PaddleOcrDictionary;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeProvider;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrSessionBundle;
import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextRegion;
import com.rndymi.almacentracker.data.document.onnx.model.TextRecognitionResult;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public final class PaddleTextRecognizer {

    private final Object inferenceLock =
            new Object();

    private final PaddleOcrRuntimeProvider runtimeProvider;
    private final PaddleTextRecognizerConfiguration configuration;
    private final PaddleTextRecognizerPreprocessor preprocessor;

    public PaddleTextRecognizer(
            PaddleOcrRuntimeProvider runtimeProvider,
            PaddleTextRecognizerConfiguration configuration,
            PaddleTextRecognizerPreprocessor preprocessor
    ) {
        this.runtimeProvider = Objects.requireNonNull(
                runtimeProvider,
                "runtimeProvider"
        );
        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration"
        );
        this.preprocessor = Objects.requireNonNull(
                preprocessor,
                "preprocessor"
        );
    }

    public TextRecognitionResult recognize(
            Bitmap bitmap,
            DetectedTextRegion region
    ) throws TextRecognitionException {
        validateBitmap(bitmap);
        Objects.requireNonNull(region, "region");

        synchronized (inferenceLock) {
            return recognizeSerially(
                    bitmap,
                    region
            );
        }
    }

    private TextRecognitionResult recognizeSerially(
            Bitmap bitmap,
            DetectedTextRegion region
    ) throws TextRecognitionException {
        PaddleOcrSessionBundle sessionBundle =
                requireReadySessions();

        OrtSession recognizerSession;

        try {
            recognizerSession =
                    sessionBundle.getRecognizerSession();
        } catch (IllegalStateException exception) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error
                            .SESSION_CLOSED,
                    "PP-OCRv5 recognizer session is closed",
                    exception
            );
        }

        PaddleOcrDictionary dictionary =
                sessionBundle.getDictionary();

        validateDictionary(dictionary);
        validateSessionNodes(recognizerSession);

        PaddleCtcDecoder decoder =
                new PaddleCtcDecoder(
                        configuration,
                        dictionary
                );

        long preprocessStartedAt =
                System.nanoTime();

        try (RecognizerInput input =
                     preprocessor.prepare(
                             bitmap,
                             region
                     )) {
            long preprocessDurationMs =
                    elapsedMilliseconds(
                            preprocessStartedAt
                    );

            Map<String, ai.onnxruntime.OnnxTensor> inputs =
                    Collections.singletonMap(
                            configuration.getInputName(),
                            input.getTensor()
                    );

            long inferenceStartedAt =
                    System.nanoTime();

            try (OrtSession.Result result =
                         recognizerSession.run(inputs)) {
                long inferenceDurationMs =
                        elapsedMilliseconds(
                                inferenceStartedAt
                        );

                float[][] logits =
                        readLogits(result);

                long decodeStartedAt =
                        System.nanoTime();

                CtcDecodingResult decodingResult =
                        decoder.decode(logits);

                long decodeDurationMs =
                        elapsedMilliseconds(
                                decodeStartedAt
                        );

                return new TextRecognitionResult(
                        decodingResult.getText(),
                        decodingResult.getConfidence(),
                        region,
                        preprocessDurationMs,
                        inferenceDurationMs,
                        decodeDurationMs
                );
            } catch (OrtException exception) {
                throw new TextRecognitionException(
                        TextRecognitionException.Error
                                .INFERENCE_ERROR,
                        "PP-OCRv5 recognition inference failed",
                        exception
                );
            }
        } catch (TextRecognitionException exception) {
            throw exception;
        } catch (OutOfMemoryError error) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error.MEMORY_ERROR,
                    "Insufficient memory during text recognition",
                    error
            );
        } catch (RuntimeException exception) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error
                            .INFERENCE_ERROR,
                    "Unexpected PP-OCRv5 recognition failure",
                    exception
            );
        }
    }

    private PaddleOcrSessionBundle
    requireReadySessions()
            throws TextRecognitionException {
        try {
            return runtimeProvider.requireReadySessions();
        } catch (IllegalStateException exception) {
            TextRecognitionException.Error error =
                    runtimeProvider.getState()
                            == PaddleOcrRuntimeProvider.State.CLOSED
                            ? TextRecognitionException.Error
                              .SESSION_CLOSED
                            : TextRecognitionException.Error
                              .RUNTIME_NOT_READY;

            throw new TextRecognitionException(
                    error,
                    "PP-OCRv5 runtime is not ready",
                    exception
            );
        }
    }

    private void validateDictionary(
            PaddleOcrDictionary dictionary
    ) throws TextRecognitionException {
        int expectedSize =
                configuration.getClassCount()
                        - 1
                        - configuration
                        .getAdditionalSpecialTokenCount();

        if (dictionary.size() != expectedSize) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error
                            .DICTIONARY_MISMATCH,
                    "Recognition dictionary has an incompatible size"
            );
        }
    }

    private void validateSessionNodes(
            OrtSession session
    ) throws TextRecognitionException {
        try {
            if (!session.getInputInfo().containsKey(
                    configuration.getInputName()
            )) {
                throw new TextRecognitionException(
                        TextRecognitionException.Error
                                .INPUT_SHAPE_INCOMPATIBLE,
                        "Configured recognizer input was not found"
                );
            }

            if (!session.getOutputInfo().containsKey(
                    configuration.getOutputName()
            )) {
                throw new TextRecognitionException(
                        TextRecognitionException.Error
                                .OUTPUT_NOT_FOUND,
                        "Configured recognizer output was not found"
                );
            }
        } catch (OrtException exception) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error
                            .INFERENCE_ERROR,
                    "Unable to inspect recognizer metadata",
                    exception
            );
        }
    }

    private float[][] readLogits(
            OrtSession.Result result
    ) throws TextRecognitionException {
        Optional<OnnxValue> output =
                result.get(
                        configuration.getOutputName()
                );

        if (!output.isPresent()) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error
                            .OUTPUT_NOT_FOUND,
                    "Recognizer output is missing"
            );
        }

        Object value;

        try {
            value = output.get().getValue();
        } catch (OrtException exception) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error
                            .INFERENCE_ERROR,
                    "Unable to read recognizer output",
                    exception
            );
        }

        if (!(value instanceof float[][][])) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error
                            .OUTPUT_SHAPE_INCOMPATIBLE,
                    "Recognizer output must have shape "
                            + "[batch, timeSteps, classes]"
            );
        }

        float[][][] batches =
                (float[][][]) value;

        if (batches.length != 1
                || batches[0] == null) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error
                            .OUTPUT_SHAPE_INCOMPATIBLE,
                    "Recognizer supports one region per inference"
            );
        }

        float[][] logits = batches[0];

        for (float[] timeStep : logits) {
            if (timeStep == null
                    || timeStep.length
                    != configuration.getClassCount()) {
                throw new TextRecognitionException(
                        TextRecognitionException.Error
                                .CLASS_COUNT_MISMATCH,
                        "Recognizer class count does not match "
                                + configuration.getClassCount()
                );
            }
        }

        return logits;
    }

    private void validateBitmap(
            Bitmap bitmap
    ) throws TextRecognitionException {
        if (bitmap == null
                || bitmap.isRecycled()
                || bitmap.getWidth() <= 0
                || bitmap.getHeight() <= 0) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error.INVALID_IMAGE,
                    "Recognizer requires a valid bitmap"
            );
        }
    }

    private long elapsedMilliseconds(
            long startedAtNanos
    ) {
        return Math.max(
                0L,
                (System.nanoTime() - startedAtNanos)
                        / 1_000_000L
        );
    }
}
