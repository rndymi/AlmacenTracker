package com.rndymi.almacentracker.app.di;

import com.rndymi.almacentracker.data.document.DocumentLineReconstructor;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeProvider;
import com.rndymi.almacentracker.data.document.onnx.detection.PaddleTextDetector;
import com.rndymi.almacentracker.data.document.onnx.recognition.PaddleTextRecognizer;

import java.util.Objects;

/** Production-equivalent OCR components used by the instrumented benchmark. */
public final class OcrEvaluationComponents {

    private final PaddleTextDetector detector;
    private final PaddleTextRecognizer recognizer;
    private final PaddleOcrRuntimeProvider runtimeProvider;
    private final DocumentLineReconstructor lineReconstructor;

    OcrEvaluationComponents(
            PaddleTextDetector detector,
            PaddleTextRecognizer recognizer,
            PaddleOcrRuntimeProvider runtimeProvider,
            DocumentLineReconstructor lineReconstructor
    ) {
        this.detector =
                Objects.requireNonNull(detector, "detector");
        this.recognizer =
                Objects.requireNonNull(recognizer, "recognizer");
        this.runtimeProvider =
                Objects.requireNonNull(
                        runtimeProvider,
                        "runtimeProvider"
                );
        this.lineReconstructor =
                Objects.requireNonNull(
                        lineReconstructor,
                        "lineReconstructor"
                );
    }

    public PaddleTextDetector getDetector() {
        return detector;
    }

    public PaddleTextRecognizer getRecognizer() {
        return recognizer;
    }

    public PaddleOcrRuntimeProvider getRuntimeProvider() {
        return runtimeProvider;
    }

    public DocumentLineReconstructor getLineReconstructor() {
        return lineReconstructor;
    }
}
