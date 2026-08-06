package com.rndymi.almacentracker.evaluation.performance;

public final class PerformanceMetrics {

    private final boolean coldStart;
    private final long modelInitializationMs;
    private final long imageProcessingMs;
    private final long detectionPreprocessMs;
    private final long detectionInferenceMs;
    private final long detectionPostprocessMs;
    private final long recognitionPreprocessMs;
    private final long recognitionInferenceMs;
    private final long recognitionDecodeMs;
    private final long reconstructionMs;
    private final long interpretationMs;
    private final long totalMs;
    private final MemorySnapshot memoryBefore;
    private final MemorySnapshot memoryPeak;
    private final MemorySnapshot memoryAfter;

    public PerformanceMetrics(
            boolean coldStart,
            long modelInitializationMs,
            long imageProcessingMs,
            long detectionPreprocessMs,
            long detectionInferenceMs,
            long detectionPostprocessMs,
            long recognitionPreprocessMs,
            long recognitionInferenceMs,
            long recognitionDecodeMs,
            long reconstructionMs,
            long interpretationMs,
            long totalMs,
            MemorySnapshot memoryBefore,
            MemorySnapshot memoryPeak,
            MemorySnapshot memoryAfter
    ) {
        this.coldStart = coldStart;
        this.modelInitializationMs =
                requireDuration(
                        modelInitializationMs,
                        "modelInitializationMs"
                );
        this.imageProcessingMs =
                requireDuration(
                        imageProcessingMs,
                        "imageProcessingMs"
                );
        this.detectionPreprocessMs =
                requireDuration(
                        detectionPreprocessMs,
                        "detectionPreprocessMs"
                );
        this.detectionInferenceMs =
                requireDuration(
                        detectionInferenceMs,
                        "detectionInferenceMs"
                );
        this.detectionPostprocessMs =
                requireDuration(
                        detectionPostprocessMs,
                        "detectionPostprocessMs"
                );
        this.recognitionPreprocessMs =
                requireDuration(
                        recognitionPreprocessMs,
                        "recognitionPreprocessMs"
                );
        this.recognitionInferenceMs =
                requireDuration(
                        recognitionInferenceMs,
                        "recognitionInferenceMs"
                );
        this.recognitionDecodeMs =
                requireDuration(
                        recognitionDecodeMs,
                        "recognitionDecodeMs"
                );
        this.reconstructionMs =
                requireDuration(
                        reconstructionMs,
                        "reconstructionMs"
                );
        this.interpretationMs =
                requireDuration(
                        interpretationMs,
                        "interpretationMs"
                );
        this.totalMs =
                requireDuration(
                        totalMs,
                        "totalMs"
                );

        if (memoryBefore == null
                || memoryPeak == null
                || memoryAfter == null) {
            throw new NullPointerException(
                    "Memory snapshots are required"
            );
        }

        this.memoryBefore = memoryBefore;
        this.memoryPeak = memoryPeak;
        this.memoryAfter = memoryAfter;
    }

    public boolean isColdStart() {
        return coldStart;
    }

    public long getModelInitializationMs() {
        return modelInitializationMs;
    }

    public long getImageProcessingMs() {
        return imageProcessingMs;
    }

    public long getDetectionPreprocessMs() {
        return detectionPreprocessMs;
    }

    public long getDetectionInferenceMs() {
        return detectionInferenceMs;
    }

    public long getDetectionPostprocessMs() {
        return detectionPostprocessMs;
    }

    public long getRecognitionPreprocessMs() {
        return recognitionPreprocessMs;
    }

    public long getRecognitionInferenceMs() {
        return recognitionInferenceMs;
    }

    public long getRecognitionDecodeMs() {
        return recognitionDecodeMs;
    }

    public long getReconstructionMs() {
        return reconstructionMs;
    }

    public long getInterpretationMs() {
        return interpretationMs;
    }

    public long getTotalMs() {
        return totalMs;
    }

    public MemorySnapshot getMemoryBefore() {
        return memoryBefore;
    }

    public MemorySnapshot getMemoryPeak() {
        return memoryPeak;
    }

    public MemorySnapshot getMemoryAfter() {
        return memoryAfter;
    }

    private static long requireDuration(
            long value,
            String fieldName
    ) {
        if (value < 0L) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be negative"
            );
        }

        return value;
    }
}
