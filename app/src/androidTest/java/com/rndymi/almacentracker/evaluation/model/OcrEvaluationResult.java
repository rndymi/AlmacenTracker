package com.rndymi.almacentracker.evaluation.model;

import com.rndymi.almacentracker.evaluation.metrics.DetectionMetrics;
import com.rndymi.almacentracker.evaluation.metrics.EditDistanceResult;
import com.rndymi.almacentracker.evaluation.metrics.InterpretationMetrics;
import com.rndymi.almacentracker.evaluation.metrics.ReconstructionMetrics;
import com.rndymi.almacentracker.evaluation.performance.PerformanceMetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable data captured from one real OCR corpus execution. */
public final class OcrEvaluationResult {

    private final String caseId;
    private final String corpusVersion;
    private final int imageWidth;
    private final int imageHeight;
    private final List<String> recognizedLines;
    private final List<String> parsedReferences;
    private final String expectedTitle;
    private final String expectedBuyerOrStore;
    private final String expectedGlobalDestination;
    private final DetectionMetrics detection;
    private final EditDistanceResult recognition;
    private final ReconstructionMetrics reconstruction;
    private final InterpretationMetrics interpretation;
    private final PerformanceMetrics performance;
    private final List<String> limitations;

    public OcrEvaluationResult(
            String caseId,
            String corpusVersion,
            int imageWidth,
            int imageHeight,
            List<String> recognizedLines,
            List<String> parsedReferences,
            String expectedTitle,
            String expectedBuyerOrStore,
            String expectedGlobalDestination,
            DetectionMetrics detection,
            EditDistanceResult recognition,
            ReconstructionMetrics reconstruction,
            InterpretationMetrics interpretation,
            PerformanceMetrics performance,
            List<String> limitations
    ) {
        this.caseId = requireText(caseId, "caseId");
        this.corpusVersion = requireText(
                corpusVersion,
                "corpusVersion"
        );

        if (imageWidth <= 0 || imageHeight <= 0) {
            throw new IllegalArgumentException(
                    "Image dimensions must be positive"
            );
        }

        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.recognizedLines = immutableStrings(
                recognizedLines,
                "recognizedLines"
        );
        this.parsedReferences = immutableStrings(
                parsedReferences,
                "parsedReferences"
        );
        this.expectedTitle = expectedTitle;
        this.expectedBuyerOrStore = expectedBuyerOrStore;
        this.expectedGlobalDestination =
                expectedGlobalDestination;
        this.detection = Objects.requireNonNull(
                detection,
                "detection"
        );
        this.recognition = Objects.requireNonNull(
                recognition,
                "recognition"
        );
        this.reconstruction = Objects.requireNonNull(
                reconstruction,
                "reconstruction"
        );
        this.interpretation = Objects.requireNonNull(
                interpretation,
                "interpretation"
        );
        this.performance = Objects.requireNonNull(
                performance,
                "performance"
        );
        this.limitations = immutableStrings(
                limitations,
                "limitations"
        );
    }

    public String getCaseId() {
        return caseId;
    }

    public String getCorpusVersion() {
        return corpusVersion;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public List<String> getRecognizedLines() {
        return recognizedLines;
    }

    public List<String> getParsedReferences() {
        return parsedReferences;
    }

    public String getExpectedTitle() {
        return expectedTitle;
    }

    public String getExpectedBuyerOrStore() {
        return expectedBuyerOrStore;
    }

    public String getExpectedGlobalDestination() {
        return expectedGlobalDestination;
    }

    public DetectionMetrics getDetection() {
        return detection;
    }

    public EditDistanceResult getRecognition() {
        return recognition;
    }

    public ReconstructionMetrics getReconstruction() {
        return reconstruction;
    }

    public InterpretationMetrics getInterpretation() {
        return interpretation;
    }

    public PerformanceMetrics getPerformance() {
        return performance;
    }

    public List<String> getLimitations() {
        return limitations;
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        String result = Objects.requireNonNull(
                value,
                fieldName
        ).trim();

        if (result.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }

        return result;
    }

    private static List<String> immutableStrings(
            List<String> values,
            String fieldName
    ) {
        Objects.requireNonNull(values, fieldName);
        List<String> result = new ArrayList<>(values.size());

        for (String value : values) {
            result.add(
                    Objects.requireNonNull(
                            value,
                            fieldName + " cannot contain null"
                    )
            );
        }

        return Collections.unmodifiableList(result);
    }
}
