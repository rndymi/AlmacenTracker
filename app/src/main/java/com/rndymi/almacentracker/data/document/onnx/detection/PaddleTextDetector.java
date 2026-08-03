package com.rndymi.almacentracker.data.document.onnx.detection;

import android.graphics.Bitmap;

import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextRegion;
import com.rndymi.almacentracker.data.document.onnx.model.TextDetectionResult;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeProvider;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrSessionBundle;
import com.rndymi.almacentracker.data.document.onnx.pipeline.TextRegionDetector;

import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class PaddleTextDetector
        implements TextRegionDetector {

    private final Object inferenceLock =
            new Object();

    private final PaddleOcrRuntimeProvider runtimeProvider;
    private final PaddleTextDetectorConfiguration configuration;
    private final PaddleTextDetectorPreprocessor preprocessor;
    private final PaddleTextDetectorPostProcessor postProcessor;

    public PaddleTextDetector(
            PaddleOcrRuntimeProvider runtimeProvider,
            PaddleTextDetectorConfiguration configuration,
            PaddleTextDetectorPreprocessor preprocessor,
            PaddleTextDetectorPostProcessor postProcessor
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
        this.postProcessor = Objects.requireNonNull(
                postProcessor,
                "postProcessor"
        );
    }

    @Override
    public TextDetectionResult detect(
            Bitmap bitmap
    ) throws TextDetectionException {
        if (bitmap == null
                || bitmap.isRecycled()
                || bitmap.getWidth() <= 0
                || bitmap.getHeight() <= 0) {
            throw new TextDetectionException(
                    TextDetectionException.Error.INVALID_IMAGE,
                    "Detector requires a valid bitmap"
            );
        }

        synchronized (inferenceLock) {
            return detectSerially(bitmap);
        }
    }

    private TextDetectionResult detectSerially(
            Bitmap bitmap
    ) throws TextDetectionException {
        PaddleOcrSessionBundle sessionBundle =
                requireReadySessions();

        OrtSession detectorSession;

        try {
            detectorSession =
                    sessionBundle.getDetectorSession();
        } catch (IllegalStateException exception) {
            throw new TextDetectionException(
                    TextDetectionException.Error
                            .SESSION_CLOSED,
                    "PP-OCRv5 detector session is closed",
                    exception
            );
        }

        validateSessionNodes(detectorSession);

        long preprocessStartedAt =
                System.nanoTime();

        try (DetectorInput input =
                     preprocessor.prepare(bitmap)) {
            long preprocessDurationMs =
                    elapsedMilliseconds(
                            preprocessStartedAt
                    );

            long inferenceStartedAt =
                    System.nanoTime();

            Map<String, ai.onnxruntime.OnnxTensor>
                    inputs = Collections.singletonMap(
                    configuration.getInputName(),
                    input.getTensor()
            );

            try (OrtSession.Result sessionResult =
                         detectorSession.run(inputs)) {
                long inferenceDurationMs =
                        elapsedMilliseconds(
                                inferenceStartedAt
                        );

                float[][] probabilityMap =
                        readProbabilityMap(
                                sessionResult
                        );

                long postprocessStartedAt =
                        System.nanoTime();

                List<DetectedTextRegion> regions =
                        postProcessor.process(
                                probabilityMap,
                                input.getTransform()
                        );

                long postprocessDurationMs =
                        elapsedMilliseconds(
                                postprocessStartedAt
                        );

                return new TextDetectionResult(
                        regions,
                        bitmap.getWidth(),
                        bitmap.getHeight(),
                        input.getTransform()
                                .getPaddedWidth(),
                        input.getTransform()
                                .getPaddedHeight(),
                        preprocessDurationMs,
                        inferenceDurationMs,
                        postprocessDurationMs
                );
            } catch (OrtException exception) {
                throw new TextDetectionException(
                        TextDetectionException.Error
                                .INFERENCE_ERROR,
                        "PP-OCRv5 detector inference failed",
                        exception
                );
            }
        } catch (TextDetectionException exception) {
            throw exception;
        } catch (OutOfMemoryError error) {
            throw new TextDetectionException(
                    TextDetectionException.Error.MEMORY_ERROR,
                    "Insufficient memory during text detection",
                    error
            );
        } catch (RuntimeException exception) {
            throw new TextDetectionException(
                    TextDetectionException.Error
                            .POSTPROCESSING_ERROR,
                    "Unexpected PP-OCRv5 detection failure",
                    exception
            );
        }
    }

    private PaddleOcrSessionBundle
    requireReadySessions()
            throws TextDetectionException {
        try {
            return runtimeProvider.requireReadySessions();
        } catch (IllegalStateException exception) {
            TextDetectionException.Error error =
                    runtimeProvider.getState()
                            == PaddleOcrRuntimeProvider.State.CLOSED
                            ? TextDetectionException.Error
                              .SESSION_CLOSED
                            : TextDetectionException.Error
                              .RUNTIME_NOT_READY;

            throw new TextDetectionException(
                    error,
                    "PP-OCRv5 runtime is not ready",
                    exception
            );
        }
    }

    private void validateSessionNodes(
            OrtSession session
    ) throws TextDetectionException {
        boolean hasConfiguredInput;
        boolean hasConfiguredOutput;

        try {
            hasConfiguredInput =
                    session.getInputInfo().containsKey(
                            configuration.getInputName()
                    );
            hasConfiguredOutput =
                    session.getOutputInfo().containsKey(
                            configuration.getOutputName()
                    );
        } catch (OrtException exception) {
            throw new TextDetectionException(
                    TextDetectionException.Error
                            .INFERENCE_ERROR,
                    "Unable to inspect detector session metadata",
                    exception
            );
        }

        if (!hasConfiguredInput) {
            throw new TextDetectionException(
                    TextDetectionException.Error
                            .INPUT_SHAPE_INCOMPATIBLE,
                    "Configured detector input was not found"
            );
        }

        if (!hasConfiguredOutput) {
            throw new TextDetectionException(
                    TextDetectionException.Error
                            .OUTPUT_NOT_FOUND,
                    "Configured detector output was not found"
            );
        }
    }

    private float[][] readProbabilityMap(
            OrtSession.Result result
    ) throws TextDetectionException {
        Optional<OnnxValue> output = result.get(
                configuration.getOutputName()
        );

        if (!output.isPresent()) {
            throw new TextDetectionException(
                    TextDetectionException.Error
                            .OUTPUT_NOT_FOUND,
                    "Detector output is missing"
            );
        }

        Object value;

        try {
            value = output.get().getValue();
        } catch (OrtException exception) {
            throw new TextDetectionException(
                    TextDetectionException.Error
                            .OUTPUT_TYPE_INCOMPATIBLE,
                    "Unable to read detector output",
                    exception
            );
        }

        if (!(value instanceof float[][][][])) {
            throw new TextDetectionException(
                    TextDetectionException.Error
                            .OUTPUT_TYPE_INCOMPATIBLE,
                    "Detector output must be FLOAT with rank four"
            );
        }

        float[][][][] tensor =
                (float[][][][]) value;

        if (tensor.length != 1
                || tensor[0] == null
                || tensor[0].length != 1
                || tensor[0][0] == null
                || tensor[0][0].length == 0
                || tensor[0][0][0] == null
                || tensor[0][0][0].length == 0) {
            throw new TextDetectionException(
                    TextDetectionException.Error
                            .OUTPUT_SHAPE_INCOMPATIBLE,
                    "Detector output shape must be [1, 1, H, W]"
            );
        }

        int width = tensor[0][0][0].length;

        for (float[] row : tensor[0][0]) {
            if (row == null || row.length != width) {
                throw new TextDetectionException(
                        TextDetectionException.Error
                                .OUTPUT_SHAPE_INCOMPATIBLE,
                        "Detector output rows are inconsistent"
                );
            }
        }

        return tensor[0][0];
    }

    private long elapsedMilliseconds(
            long startedAt
    ) {
        return Math.max(
                0L,
                (System.nanoTime() - startedAt)
                        / 1_000_000L
        );
    }
}
