package com.rndymi.almacentracker.evaluation.report;

import android.content.Context;

import com.rndymi.almacentracker.evaluation.metrics.DetectionMetrics;
import com.rndymi.almacentracker.evaluation.metrics.EditDistanceResult;
import com.rndymi.almacentracker.evaluation.metrics.InterpretationMetrics;
import com.rndymi.almacentracker.evaluation.metrics.ReconstructionMetrics;
import com.rndymi.almacentracker.evaluation.performance.PerformanceMetrics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

public final class OcrEvaluationReportWriter {

    private final File outputDirectory;

    public OcrEvaluationReportWriter(
            Context context
    ) {
        Objects.requireNonNull(context, "context");

        outputDirectory = new File(
                context.getFilesDir(),
                "ocr-evaluation"
        );

        if (!outputDirectory.exists()
                && !outputDirectory.mkdirs()) {
            throw new IllegalStateException(
                    "Cannot create OCR evaluation output directory"
            );
        }
    }

    public File writeJson(
            String caseId,
            DetectionMetrics detection,
            EditDistanceResult recognition,
            ReconstructionMetrics reconstruction,
            InterpretationMetrics interpretation,
            PerformanceMetrics performance
    ) throws IOException, JSONException {
        JSONObject root = new JSONObject();

        root.put("caseId", caseId);
        root.put(
                "detection",
                detectionJson(detection)
        );
        root.put(
                "recognition",
                recognitionJson(recognition)
        );
        root.put(
                "reconstruction",
                reconstructionJson(reconstruction)
        );
        root.put(
                "interpretation",
                interpretationJson(interpretation)
        );
        root.put(
                "performance",
                performanceJson(performance)
        );

        File output = new File(
                outputDirectory,
                caseId + "-result.json"
        );

        writeText(
                output,
                root.toString(2)
        );

        return output;
    }

    public File writeMarkdown(
            String caseId,
            DetectionMetrics detection,
            EditDistanceResult recognition,
            ReconstructionMetrics reconstruction,
            InterpretationMetrics interpretation,
            PerformanceMetrics performance
    ) throws IOException {
        StringBuilder report =
                new StringBuilder();

        report.append("# OCR evaluation — ")
                .append(caseId)
                .append("\n\n");

        report.append("## Detection\n\n")
                .append("| Metric | Value |\n")
                .append("|---|---:|\n")
                .append("| Expected regions | ")
                .append(
                        detection
                                .getExpectedRegionCount()
                )
                .append(" |\n")
                .append("| Detected regions | ")
                .append(
                        detection
                                .getDetectedRegionCount()
                )
                .append(" |\n")
                .append("| Matched regions | ")
                .append(
                        detection
                                .getMatchedRegionCount()
                )
                .append(" |\n")
                .append("| Precision | ")
                .append(percent(
                        detection
                                .getPrecision()
                                .getValueOrZero()
                ))
                .append(" |\n")
                .append("| Recall | ")
                .append(percent(
                        detection
                                .getRecall()
                                .getValueOrZero()
                ))
                .append(" |\n")
                .append("| F1 | ")
                .append(percent(
                        detection.getF1()
                ))
                .append(" |\n\n");

        report.append("## Recognition\n\n")
                .append("| Metric | Value |\n")
                .append("|---|---:|\n")
                .append("| Expected text | `")
                .append(
                        escapeMarkdown(
                                recognition
                                        .getExpectedText()
                        )
                )
                .append("` |\n")
                .append("| Recognized text | `")
                .append(
                        escapeMarkdown(
                                recognition
                                        .getActualText()
                        )
                )
                .append("` |\n")
                .append("| Substitutions | ")
                .append(
                        recognition
                                .getSubstitutions()
                )
                .append(" |\n")
                .append("| Insertions | ")
                .append(
                        recognition
                                .getInsertions()
                )
                .append(" |\n")
                .append("| Deletions | ")
                .append(
                        recognition
                                .getDeletions()
                )
                .append(" |\n")
                .append("| CER | ")
                .append(percent(
                        recognition
                                .getCharacterErrorRate()
                ))
                .append(" |\n\n");

        report.append("## Reconstruction\n\n")
                .append("| Metric | Value |\n")
                .append("|---|---:|\n")
                .append("| Expected lines | ")
                .append(
                        reconstruction
                                .getExpectedLineCount()
                )
                .append(" |\n")
                .append("| Reconstructed lines | ")
                .append(
                        reconstruction
                                .getReconstructedLineCount()
                )
                .append(" |\n")
                .append("| Merged lines | ")
                .append(
                        reconstruction
                                .getMergedLineCount()
                )
                .append(" |\n")
                .append("| Split lines | ")
                .append(
                        reconstruction
                                .getSplitLineCount()
                )
                .append(" |\n")
                .append("| Column order | ")
                .append(percent(
                        reconstruction
                                .getColumnOrderAccuracy()
                                .getValueOrZero()
                ))
                .append(" |\n\n");

        report.append("## Interpretation\n\n")
                .append("| Metric | Value |\n")
                .append("|---|---:|\n")
                .append("| Exact references | ")
                .append(
                        interpretation
                                .getExactMatches()
                )
                .append(" |\n")
                .append("| Recovered by Room | ")
                .append(
                        interpretation
                                .getRoomRecoveredReferences()
                )
                .append(" |\n")
                .append("| Ambiguities | ")
                .append(
                        interpretation
                                .getAmbiguities()
                )
                .append(" |\n")
                .append("| False positives | ")
                .append(
                        interpretation
                                .getFalsePositiveReferences()
                )
                .append(" |\n")
                .append("| Missed references | ")
                .append(
                        interpretation
                                .getMissedReferences()
                )
                .append(" |\n\n");

        report.append("## Performance\n\n")
                .append("| Metric | Value |\n")
                .append("|---|---:|\n")
                .append("| Cold start | ")
                .append(
                        performance.isColdStart()
                )
                .append(" |\n")
                .append("| Initialization | ")
                .append(
                        performance
                                .getModelInitializationMs()
                )
                .append(" ms |\n")
                .append("| Total | ")
                .append(
                        performance.getTotalMs()
                )
                .append(" ms |\n")
                .append("| Java memory before | ")
                .append(
                        performance
                                .getMemoryBefore()
                                .getJavaUsedBytes()
                )
                .append(" bytes |\n")
                .append("| Native memory before | ")
                .append(
                        performance
                                .getMemoryBefore()
                                .getNativeHeapBytes()
                )
                .append(" bytes |\n")
                .append("| Java memory peak | ")
                .append(
                        performance
                                .getMemoryPeak()
                                .getJavaUsedBytes()
                )
                .append(" bytes |\n")
                .append("| Native memory peak | ")
                .append(
                        performance
                                .getMemoryPeak()
                                .getNativeHeapBytes()
                )
                .append(" bytes |\n");

        File output = new File(
                outputDirectory,
                caseId + "-report.md"
        );

        writeText(output, report.toString());

        return output;
    }

    private JSONObject detectionJson(
            DetectionMetrics value
    ) throws JSONException {
        JSONObject result = new JSONObject();

        result.put(
                "expectedRegionCount",
                value.getExpectedRegionCount()
        );
        result.put(
                "detectedRegionCount",
                value.getDetectedRegionCount()
        );
        result.put(
                "matchedRegionCount",
                value.getMatchedRegionCount()
        );
        result.put(
                "missedRegionCount",
                value.getMissedRegionCount()
        );
        result.put(
                "extraRegionCount",
                value.getExtraRegionCount()
        );
        result.put(
                "mergedRegionCount",
                value.getMergedRegionCount()
        );
        result.put(
                "splitRegionCount",
                value.getSplitRegionCount()
        );
        result.put(
                "precision",
                value.getPrecision()
                        .getValueOrZero()
        );
        result.put(
                "precisionDefined",
                value.getPrecision().isDefined()
        );
        result.put(
                "recall",
                value.getRecall()
                        .getValueOrZero()
        );
        result.put("f1", value.getF1());

        return result;
    }

    private JSONObject recognitionJson(
            EditDistanceResult value
    ) throws JSONException {
        JSONObject result = new JSONObject();

        result.put(
                "expectedText",
                value.getExpectedText()
        );
        result.put(
                "recognizedText",
                value.getActualText()
        );
        result.put(
                "correctCharacters",
                value.getCorrectCharacters()
        );
        result.put(
                "substitutions",
                value.getSubstitutions()
        );
        result.put(
                "insertions",
                value.getInsertions()
        );
        result.put(
                "deletions",
                value.getDeletions()
        );
        result.put(
                "cer",
                value.getCharacterErrorRate()
        );
        result.put(
                "exactMatch",
                value.isExactMatch()
        );

        return result;
    }

    private JSONObject reconstructionJson(
            ReconstructionMetrics value
    ) throws JSONException {
        JSONObject result = new JSONObject();

        result.put(
                "expectedLineCount",
                value.getExpectedLineCount()
        );
        result.put(
                "reconstructedLineCount",
                value.getReconstructedLineCount()
        );
        result.put(
                "exactLineMatches",
                value.getExactLineMatches()
        );
        result.put(
                "mergedLineCount",
                value.getMergedLineCount()
        );
        result.put(
                "splitLineCount",
                value.getSplitLineCount()
        );
        result.put(
                "correctOrderCount",
                value.getCorrectOrderCount()
        );
        result.put(
                "columnOrderAccuracy",
                value.getColumnOrderAccuracy()
                        .getValueOrZero()
        );

        return result;
    }

    private JSONObject interpretationJson(
            InterpretationMetrics value
    ) throws JSONException {
        JSONObject result = new JSONObject();

        result.put(
                "expectedReferenceCount",
                value.getExpectedReferenceCount()
        );
        result.put(
                "proposedReferenceCount",
                value.getProposedReferenceCount()
        );
        result.put(
                "exactMatches",
                value.getExactMatches()
        );
        result.put(
                "roomRecoveredReferences",
                value.getRoomRecoveredReferences()
        );
        result.put(
                "uniqueSuggestions",
                value.getUniqueSuggestions()
        );
        result.put(
                "ambiguities",
                value.getAmbiguities()
        );
        result.put(
                "noMatches",
                value.getNoMatches()
        );
        result.put(
                "falsePositiveReferences",
                value.getFalsePositiveReferences()
        );
        result.put(
                "missedReferences",
                value.getMissedReferences()
        );

        return result;
    }

    private JSONObject performanceJson(
            PerformanceMetrics value
    ) throws JSONException {
        JSONObject result = new JSONObject();

        result.put(
                "coldStart",
                value.isColdStart()
        );
        result.put(
                "modelInitializationMs",
                value.getModelInitializationMs()
        );
        result.put(
                "imageProcessingMs",
                value.getImageProcessingMs()
        );
        result.put(
                "detectionPreprocessMs",
                value.getDetectionPreprocessMs()
        );
        result.put(
                "detectionInferenceMs",
                value.getDetectionInferenceMs()
        );
        result.put(
                "detectionPostprocessMs",
                value.getDetectionPostprocessMs()
        );
        result.put(
                "recognitionPreprocessMs",
                value.getRecognitionPreprocessMs()
        );
        result.put(
                "recognitionInferenceMs",
                value.getRecognitionInferenceMs()
        );
        result.put(
                "recognitionDecodeMs",
                value.getRecognitionDecodeMs()
        );
        result.put(
                "reconstructionMs",
                value.getReconstructionMs()
        );
        result.put(
                "interpretationMs",
                value.getInterpretationMs()
        );
        result.put(
                "totalMs",
                value.getTotalMs()
        );

        return result;
    }

    private String percent(
            double value
    ) {
        return String.format(
                Locale.ROOT,
                "%.2f%%",
                value * 100.0d
        );
    }

    private String escapeMarkdown(
            String value
    ) {
        return value
                .replace("|", "\\|")
                .replace("`", "\\`");
    }

    private void writeText(
            File output,
            String value
    ) throws IOException {
        try (FileOutputStream stream =
                     new FileOutputStream(output)) {
            stream.write(
                    value.getBytes(
                            StandardCharsets.UTF_8
                    )
            );
        }
    }
}
