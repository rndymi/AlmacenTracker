package com.rndymi.almacentracker.data.document.onnx.detection;

import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextPolygon;
import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextRegion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class PaddleTextDetectorPostProcessor {

    private final PaddleTextDetectorConfiguration configuration;
    private final TextProbabilityMapAnalyzer analyzer;

    public PaddleTextDetectorPostProcessor(
            PaddleTextDetectorConfiguration configuration
    ) {
        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration"
        );
        this.analyzer =
                new TextProbabilityMapAnalyzer(
                        configuration
                );
    }

    public List<DetectedTextRegion> process(
            float[][] probabilityMap,
            DetectorImageTransform transform
    ) throws TextDetectionException {
        Objects.requireNonNull(
                transform,
                "transform"
        );

        List<TextProbabilityMapAnalyzer.Candidate>
                candidates = analyzer.analyze(
                probabilityMap
        );

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        int outputHeight = probabilityMap.length;
        int outputWidth =
                probabilityMap[0].length;

        ArrayList<DetectedTextRegion> regions =
                new ArrayList<>(candidates.size());

        for (TextProbabilityMapAnalyzer.Candidate
                candidate : candidates) {
            DetectedTextRegion region =
                    restoreCandidate(
                            candidate,
                            transform,
                            outputWidth,
                            outputHeight
                    );

            if (region != null) {
                regions.add(region);
            }
        }

        removeDuplicates(regions);
        sortReadingOrder(regions);

        ArrayList<DetectedTextRegion> ordered =
                new ArrayList<>(regions.size());

        for (int index = 0;
             index < regions.size();
             index++) {
            DetectedTextRegion region =
                    regions.get(index);

            ordered.add(
                    new DetectedTextRegion(
                            region.getPolygon(),
                            region.getConfidence(),
                            index
                    )
            );
        }

        return Collections.unmodifiableList(ordered);
    }

    private DetectedTextRegion restoreCandidate(
            TextProbabilityMapAnalyzer.Candidate candidate,
            DetectorImageTransform transform,
            int outputWidth,
            int outputHeight
    ) {
        float inferenceLeft =
                transform.mapOutputXToInference(
                        candidate.getLeft(),
                        outputWidth
                );
        float inferenceTop =
                transform.mapOutputYToInference(
                        candidate.getTop(),
                        outputHeight
                );
        float inferenceRight =
                transform.mapOutputXToInference(
                        candidate.getRight(),
                        outputWidth
                );
        float inferenceBottom =
                transform.mapOutputYToInference(
                        candidate.getBottom(),
                        outputHeight
                );

        float centerX =
                (inferenceLeft + inferenceRight)
                        / 2.0f;
        float centerY =
                (inferenceTop + inferenceBottom)
                        / 2.0f;

        if (!transform.isInsideContent(
                centerX,
                centerY
        )) {
            return null;
        }

        float width =
                inferenceRight - inferenceLeft;
        float height =
                inferenceBottom - inferenceTop;

        float expandedWidth =
                width * configuration.getUnclipRatio();
        float expandedHeight =
                height * configuration.getUnclipRatio();

        float expandedLeft = centerX
                - expandedWidth / 2.0f;
        float expandedTop = centerY
                - expandedHeight / 2.0f;
        float expandedRight = centerX
                + expandedWidth / 2.0f;
        float expandedBottom = centerY
                + expandedHeight / 2.0f;

        DetectedTextPolygon polygon =
                transform.restoreRectangle(
                        expandedLeft,
                        expandedTop,
                        expandedRight,
                        expandedBottom
                );

        if (polygon.getWidth() < 1.0f
                || polygon.getHeight() < 1.0f) {
            return null;
        }

        return new DetectedTextRegion(
                polygon,
                candidate.getConfidence(),
                0
        );
    }

    private void removeDuplicates(
            List<DetectedTextRegion> regions
    ) {
        regions.sort(
                Comparator.comparing(
                        DetectedTextRegion::getConfidence
                ).reversed()
        );

        ArrayList<DetectedTextRegion> accepted =
                new ArrayList<>();

        for (DetectedTextRegion candidate : regions) {
            boolean duplicate = false;

            for (DetectedTextRegion existing
                    : accepted) {
                if (intersectionOverUnion(
                        candidate,
                        existing
                ) >= configuration
                        .getDuplicateIouThreshold()) {
                    duplicate = true;
                    break;
                }
            }

            if (!duplicate) {
                accepted.add(candidate);
            }
        }

        regions.clear();
        regions.addAll(accepted);
    }

    private void sortReadingOrder(
            List<DetectedTextRegion> regions
    ) {
        regions.sort((first, second) -> {
            float overlap = verticalOverlapRatio(
                    first,
                    second
            );

            if (overlap >= configuration
                    .getLineOverlapThreshold()) {
                int horizontal = Float.compare(
                        first.getLeft(),
                        second.getLeft()
                );

                if (horizontal != 0) {
                    return horizontal;
                }
            }

            int vertical = Float.compare(
                    first.getTop(),
                    second.getTop()
            );

            if (vertical != 0) {
                return vertical;
            }

            int horizontal = Float.compare(
                    first.getLeft(),
                    second.getLeft()
            );

            if (horizontal != 0) {
                return horizontal;
            }

            return Float.compare(
                    second.getConfidence(),
                    first.getConfidence()
            );
        });
    }

    private float verticalOverlapRatio(
            DetectedTextRegion first,
            DetectedTextRegion second
    ) {
        float overlapTop = Math.max(
                first.getTop(),
                second.getTop()
        );
        float overlapBottom = Math.min(
                first.getBottom(),
                second.getBottom()
        );

        float overlap = Math.max(
                0.0f,
                overlapBottom - overlapTop
        );

        float minimumHeight = Math.min(
                first.getHeight(),
                second.getHeight()
        );

        if (minimumHeight <= 0.0f) {
            return 0.0f;
        }

        return overlap / minimumHeight;
    }

    private float intersectionOverUnion(
            DetectedTextRegion first,
            DetectedTextRegion second
    ) {
        float left = Math.max(
                first.getLeft(),
                second.getLeft()
        );
        float top = Math.max(
                first.getTop(),
                second.getTop()
        );
        float right = Math.min(
                first.getRight(),
                second.getRight()
        );
        float bottom = Math.min(
                first.getBottom(),
                second.getBottom()
        );

        float intersectionWidth =
                Math.max(0.0f, right - left);
        float intersectionHeight =
                Math.max(0.0f, bottom - top);
        float intersection =
                intersectionWidth * intersectionHeight;

        if (intersection <= 0.0f) {
            return 0.0f;
        }

        float union = first.getPolygon().getArea()
                + second.getPolygon().getArea()
                - intersection;

        return union <= 0.0f
                ? 0.0f
                : intersection / union;
    }
}
