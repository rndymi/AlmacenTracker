package com.rndymi.almacentracker.evaluation.report;

import android.content.Context;

import com.rndymi.almacentracker.evaluation.metrics.DetectionMetrics;
import com.rndymi.almacentracker.evaluation.metrics.EditDistanceResult;
import com.rndymi.almacentracker.evaluation.metrics.InterpretationMetrics;
import com.rndymi.almacentracker.evaluation.metrics.ReconstructionMetrics;
import com.rndymi.almacentracker.evaluation.model.OcrEvaluationResult;
import com.rndymi.almacentracker.evaluation.performance.MemorySnapshot;
import com.rndymi.almacentracker.evaluation.performance.PerformanceMetrics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class OcrEvaluationReportWriter {

    private final File outputDirectory;

    /**
     * Creates a writer in app-specific external storage. Instrumented Gradle
     * runs should prefer the File constructor with additionalTestOutputDir.
     */
    public OcrEvaluationReportWriter(
            Context context
    ) {
        this(externalOutputDirectory(context));
    }

    public OcrEvaluationReportWriter(
            File outputDirectory
    ) {
        this.outputDirectory = Objects.requireNonNull(
                outputDirectory,
                "outputDirectory"
        );

        if (outputDirectory.exists()
                && !outputDirectory.isDirectory()) {
            throw new IllegalStateException(
                    "OCR evaluation output path is not a directory: "
                            + outputDirectory.getAbsolutePath()
            );
        }

        if (!outputDirectory.exists()
                && !outputDirectory.mkdirs()) {
            throw new IllegalStateException(
                    "Cannot create OCR evaluation output directory: "
                            + outputDirectory.getAbsolutePath()
            );
        }
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public File writeJson(
            OcrEvaluationResult result
    ) throws IOException, JSONException {
        Objects.requireNonNull(result, "result");

        JSONObject root = new JSONObject();
        root.put("caseId", result.getCaseId());
        root.put("corpusVersion", result.getCorpusVersion());
        root.put("status", "SUCCESS");
        root.put("technicalErrors", new JSONArray());

        JSONObject image = new JSONObject();
        image.put("width", result.getImageWidth());
        image.put("height", result.getImageHeight());
        root.put("image", image);
        root.put("regionCount", result.getDetection()
                .getDetectedRegionCount());
        root.put("lineCount", result.getRecognizedLines().size());
        root.put("recognizedLines", array(result.getRecognizedLines()));
        root.put("parsedReferences", array(result.getParsedReferences()));

        JSONObject expectedDocument = new JSONObject();
        expectedDocument.put(
                "title",
                nullable(result.getExpectedTitle())
        );
        expectedDocument.put(
                "buyerOrStore",
                nullable(result.getExpectedBuyerOrStore())
        );
        expectedDocument.put(
                "globalDestination",
                nullable(result.getExpectedGlobalDestination())
        );
        root.put("expectedDocument", expectedDocument);

        root.put("detection", detectionJson(result.getDetection()));
        root.put("recognition", recognitionJson(result.getRecognition()));
        root.put(
                "reconstruction",
                reconstructionJson(result.getReconstruction())
        );
        root.put(
                "interpretation",
                interpretationJson(result.getInterpretation())
        );
        root.put(
                "performance",
                performanceJson(result.getPerformance())
        );
        root.put("limitations", array(result.getLimitations()));

        File output = new File(
                outputDirectory,
                result.getCaseId() + "-evaluation-result.json"
        );
        writeText(output, root.toString(2));
        return output;
    }

    public File writeMarkdown(
            OcrEvaluationResult result
    ) throws IOException {
        Objects.requireNonNull(result, "result");
        DetectionMetrics detection = result.getDetection();
        EditDistanceResult recognition = result.getRecognition();
        ReconstructionMetrics reconstruction =
                result.getReconstruction();
        InterpretationMetrics interpretation =
                result.getInterpretation();
        PerformanceMetrics performance = result.getPerformance();

        StringBuilder report = new StringBuilder();
        report.append("# OCR evaluation — ")
                .append(result.getCaseId())
                .append("\n\n")
                .append("- Status: SUCCESS\n")
                .append("- Corpus: ")
                .append(result.getCorpusVersion())
                .append("\n- Image: ")
                .append(result.getImageWidth())
                .append(" × ")
                .append(result.getImageHeight())
                .append("\n- Regions: ")
                .append(detection.getDetectedRegionCount())
                .append("\n- Lines: ")
                .append(result.getRecognizedLines().size())
                .append("\n\n");

        report.append("## Detection\n\n")
                .append("| Metric | Value |\n|---|---:|\n")
                .append("| Expected regions | ")
                .append(detection.getExpectedRegionCount()).append(" |\n")
                .append("| Detected regions | ")
                .append(detection.getDetectedRegionCount()).append(" |\n")
                .append("| Matched regions | ")
                .append(detection.getMatchedRegionCount()).append(" |\n")
                .append("| Precision | ")
                .append(percent(detection.getPrecision().getValueOrZero()))
                .append(" |\n| Recall | ")
                .append(percent(detection.getRecall().getValueOrZero()))
                .append(" |\n| F1 | ")
                .append(percent(detection.getF1())).append(" |\n\n");

        report.append("## Recognition\n\n")
                .append("| Metric | Value |\n|---|---:|\n")
                .append("| Expected text | `")
                .append(escapeMarkdown(recognition.getExpectedText()))
                .append("` |\n| Recognized text | `")
                .append(escapeMarkdown(recognition.getActualText()))
                .append("` |\n| Substitutions | ")
                .append(recognition.getSubstitutions()).append(" |\n")
                .append("| Insertions | ")
                .append(recognition.getInsertions()).append(" |\n")
                .append("| Deletions | ")
                .append(recognition.getDeletions()).append(" |\n")
                .append("| CER | ")
                .append(percent(recognition.getCharacterErrorRate()))
                .append(" |\n\n");

        report.append("## Reconstruction\n\n")
                .append("| Metric | Value |\n|---|---:|\n")
                .append("| Expected lines | ")
                .append(reconstruction.getExpectedLineCount()).append(" |\n")
                .append("| Reconstructed lines | ")
                .append(reconstruction.getReconstructedLineCount())
                .append(" |\n| Merged lines | ")
                .append(reconstruction.getMergedLineCount()).append(" |\n")
                .append("| Split lines | ")
                .append(reconstruction.getSplitLineCount()).append(" |\n")
                .append("| Column order | ")
                .append(percent(reconstruction.getColumnOrderAccuracy()
                        .getValueOrZero()))
                .append(" |\n\n");

        report.append("## Interpretation\n\n")
                .append("| Metric | Value |\n|---|---:|\n")
                .append("| Exact references | ")
                .append(interpretation.getExactMatches()).append(" |\n")
                .append("| Recovered by Room | ")
                .append(interpretation.getRoomRecoveredReferences())
                .append(" |\n| False positives | ")
                .append(interpretation.getFalsePositiveReferences())
                .append(" |\n| Missed references | ")
                .append(interpretation.getMissedReferences())
                .append(" |\n| Parsed references | ")
                .append(escapeMarkdown(String.join(", ",
                        result.getParsedReferences())))
                .append(" |\n\n");

        report.append("## Performance\n\n")
                .append("| Metric | Value |\n|---|---:|\n")
                .append("| Cold start | ")
                .append(performance.isColdStart()).append(" |\n")
                .append("| Initialization | ")
                .append(performance.getModelInitializationMs())
                .append(" ms |\n| Total | ")
                .append(performance.getTotalMs()).append(" ms |\n")
                .append("| Detection preprocess | ")
                .append(performance.getDetectionPreprocessMs())
                .append(" ms |\n| Detection inference | ")
                .append(performance.getDetectionInferenceMs())
                .append(" ms |\n| Detection postprocess | ")
                .append(performance.getDetectionPostprocessMs())
                .append(" ms |\n| Recognition preprocess | ")
                .append(performance.getRecognitionPreprocessMs())
                .append(" ms |\n| Recognition inference | ")
                .append(performance.getRecognitionInferenceMs())
                .append(" ms |\n| Recognition decode | ")
                .append(performance.getRecognitionDecodeMs())
                .append(" ms |\n| Java memory before | ")
                .append(performance.getMemoryBefore().getJavaUsedBytes())
                .append(" bytes |\n| Native memory before | ")
                .append(performance.getMemoryBefore().getNativeHeapBytes())
                .append(" bytes |\n| Approximate peak | ")
                .append(totalMemory(performance.getMemoryPeak()))
                .append(" bytes |\n| Java memory after | ")
                .append(performance.getMemoryAfter().getJavaUsedBytes())
                .append(" bytes |\n| Native memory after | ")
                .append(performance.getMemoryAfter().getNativeHeapBytes())
                .append(" bytes |\n\n");

        report.append("## Expected document metadata\n\n")
                .append("- Title: ").append(display(result.getExpectedTitle()))
                .append("\n- Buyer/store: ")
                .append(display(result.getExpectedBuyerOrStore()))
                .append("\n- Destination: ")
                .append(display(result.getExpectedGlobalDestination()))
                .append("\n\n## Measurement limitations\n\n");

        for (String limitation : result.getLimitations()) {
            report.append("- ").append(limitation).append("\n");
        }

        File output = new File(
                outputDirectory,
                result.getCaseId() + "-evaluation-report.md"
        );
        writeText(output, report.toString());
        return output;
    }

    private JSONObject detectionJson(DetectionMetrics value)
            throws JSONException {
        JSONObject result = new JSONObject();
        result.put("expectedRegionCount", value.getExpectedRegionCount());
        result.put("detectedRegionCount", value.getDetectedRegionCount());
        result.put("matchedRegionCount", value.getMatchedRegionCount());
        result.put("missedRegionCount", value.getMissedRegionCount());
        result.put("extraRegionCount", value.getExtraRegionCount());
        result.put("mergedRegionCount", value.getMergedRegionCount());
        result.put("splitRegionCount", value.getSplitRegionCount());
        result.put("precision", value.getPrecision().getValueOrZero());
        result.put("precisionDefined", value.getPrecision().isDefined());
        result.put("recall", value.getRecall().getValueOrZero());
        result.put("recallDefined", value.getRecall().isDefined());
        result.put("f1", value.getF1());
        return result;
    }

    private JSONObject recognitionJson(EditDistanceResult value)
            throws JSONException {
        JSONObject result = new JSONObject();
        result.put("expectedText", value.getExpectedText());
        result.put("recognizedText", value.getActualText());
        result.put("correctCharacters", value.getCorrectCharacters());
        result.put("substitutions", value.getSubstitutions());
        result.put("insertions", value.getInsertions());
        result.put("deletions", value.getDeletions());
        result.put("cer", value.getCharacterErrorRate());
        result.put("exactMatch", value.isExactMatch());
        return result;
    }

    private JSONObject reconstructionJson(ReconstructionMetrics value)
            throws JSONException {
        JSONObject result = new JSONObject();
        result.put("expectedLineCount", value.getExpectedLineCount());
        result.put("reconstructedLineCount",
                value.getReconstructedLineCount());
        result.put("exactLineMatches", value.getExactLineMatches());
        result.put("mergedLineCount", value.getMergedLineCount());
        result.put("splitLineCount", value.getSplitLineCount());
        result.put("correctOrderCount", value.getCorrectOrderCount());
        result.put("columnOrderAccuracy",
                value.getColumnOrderAccuracy().getValueOrZero());
        return result;
    }

    private JSONObject interpretationJson(InterpretationMetrics value)
            throws JSONException {
        JSONObject result = new JSONObject();
        result.put("expectedReferenceCount",
                value.getExpectedReferenceCount());
        result.put("proposedReferenceCount",
                value.getProposedReferenceCount());
        result.put("exactMatches", value.getExactMatches());
        result.put("roomRecoveredReferences",
                value.getRoomRecoveredReferences());
        result.put("uniqueSuggestions", value.getUniqueSuggestions());
        result.put("ambiguities", value.getAmbiguities());
        result.put("noMatches", value.getNoMatches());
        result.put("falsePositiveReferences",
                value.getFalsePositiveReferences());
        result.put("missedReferences", value.getMissedReferences());
        result.put("quantityAccuracy",
                value.getQuantityAccuracy().getValueOrZero());
        result.put("unitAccuracy",
                value.getUnitAccuracy().getValueOrZero());
        result.put("destinationAccuracy",
                value.getDestinationAccuracy().getValueOrZero());
        result.put("titleAccuracy",
                value.getTitleAccuracy().getValueOrZero());
        return result;
    }

    private JSONObject performanceJson(PerformanceMetrics value)
            throws JSONException {
        JSONObject result = new JSONObject();
        result.put("coldStart", value.isColdStart());
        result.put("modelInitializationMs", value.getModelInitializationMs());
        result.put("imageProcessingMs", value.getImageProcessingMs());
        result.put("detectionPreprocessMs", value.getDetectionPreprocessMs());
        result.put("detectionInferenceMs", value.getDetectionInferenceMs());
        result.put("detectionPostprocessMs", value.getDetectionPostprocessMs());
        result.put("recognitionPreprocessMs",
                value.getRecognitionPreprocessMs());
        result.put("recognitionInferenceMs",
                value.getRecognitionInferenceMs());
        result.put("recognitionDecodeMs", value.getRecognitionDecodeMs());
        result.put("reconstructionMs", value.getReconstructionMs());
        result.put("interpretationMs", value.getInterpretationMs());
        result.put("totalMs", value.getTotalMs());
        result.put("memoryBefore", memoryJson(value.getMemoryBefore()));
        result.put("memoryPeak", memoryJson(value.getMemoryPeak()));
        result.put("memoryAfter", memoryJson(value.getMemoryAfter()));
        return result;
    }

    private JSONObject memoryJson(MemorySnapshot value)
            throws JSONException {
        JSONObject result = new JSONObject();
        result.put("javaUsedBytes", value.getJavaUsedBytes());
        result.put("nativeHeapBytes", value.getNativeHeapBytes());
        result.put("totalBytes", totalMemory(value));
        return result;
    }

    private JSONArray array(List<String> values) {
        JSONArray result = new JSONArray();
        for (String value : values) {
            result.put(value);
        }
        return result;
    }

    private Object nullable(String value) {
        return value == null ? JSONObject.NULL : value;
    }

    private long totalMemory(MemorySnapshot value) {
        return value.getJavaUsedBytes() + value.getNativeHeapBytes();
    }

    private String percent(double value) {
        return String.format(Locale.ROOT, "%.2f%%", value * 100.0d);
    }

    private String escapeMarkdown(String value) {
        return value.replace("|", "\\|")
                .replace("`", "\\`")
                .replace("\n", "<br>");
    }

    private String display(String value) {
        return value == null ? "not defined" : escapeMarkdown(value);
    }

    private void writeText(File output, String value)
            throws IOException {
        try (FileOutputStream stream = new FileOutputStream(output)) {
            stream.write(value.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static File externalOutputDirectory(Context context) {
        Objects.requireNonNull(context, "context");
        File external = context.getExternalFilesDir(null);

        if (external == null) {
            throw new IllegalStateException(
                    "External files directory is unavailable for OCR reports"
            );
        }

        return new File(external, "ocr-evaluation");
    }
}
