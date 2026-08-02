package com.rndymi.almacentracker.data.document.onnx.detection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class TextProbabilityMapAnalyzer {

    static final class Candidate {

        private final int left;
        private final int top;
        private final int right;
        private final int bottom;
        private final float confidence;

        Candidate(
                int left,
                int top,
                int right,
                int bottom,
                float confidence
        ) {
            if (left < 0
                    || top < 0
                    || right <= left
                    || bottom <= top) {
                throw new IllegalArgumentException(
                        "Candidate bounds are invalid"
                );
            }

            if (!Float.isFinite(confidence)
                    || confidence < 0.0f
                    || confidence > 1.0f) {
                throw new IllegalArgumentException(
                        "Candidate confidence is invalid"
                );
            }

            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.confidence = confidence;
        }

        int getLeft() {
            return left;
        }

        int getTop() {
            return top;
        }

        int getRight() {
            return right;
        }

        int getBottom() {
            return bottom;
        }

        float getConfidence() {
            return confidence;
        }

        int getWidth() {
            return right - left;
        }

        int getHeight() {
            return bottom - top;
        }
    }

    private static final int[] NEIGHBOR_X = {
            -1, 0, 1,
            -1,    1,
            -1, 0, 1
    };

    private static final int[] NEIGHBOR_Y = {
            -1, -1, -1,
            0,      0,
            1,  1,  1
    };

    private final PaddleTextDetectorConfiguration configuration;

    TextProbabilityMapAnalyzer(
            PaddleTextDetectorConfiguration configuration
    ) {
        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration"
        );
    }

    List<Candidate> analyze(
            float[][] probabilityMap
    ) throws TextDetectionException {
        validateProbabilityMap(probabilityMap);

        int height = probabilityMap.length;
        int width = probabilityMap[0].length;

        boolean[][] visited =
                new boolean[height][width];

        ArrayList<Candidate> candidates =
                new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (visited[y][x]
                        || probabilityMap[y][x]
                        < configuration.getPixelThreshold()) {
                    continue;
                }

                Candidate candidate = exploreComponent(
                        probabilityMap,
                        visited,
                        x,
                        y
                );

                if (candidate != null) {
                    candidates.add(candidate);
                }
            }
        }

        return candidates;
    }

    private Candidate exploreComponent(
            float[][] probabilityMap,
            boolean[][] visited,
            int startX,
            int startY
    ) {
        int height = probabilityMap.length;
        int width = probabilityMap[0].length;

        ArrayDeque<Integer> queue =
                new ArrayDeque<>();

        queue.add(startY * width + startX);
        visited[startY][startX] = true;

        int left = startX;
        int right = startX;
        int top = startY;
        int bottom = startY;

        int pixelCount = 0;
        float probabilitySum = 0.0f;

        while (!queue.isEmpty()) {
            int encoded = queue.removeFirst();
            int y = encoded / width;
            int x = encoded % width;

            float probability = probabilityMap[y][x];

            pixelCount++;
            probabilitySum += probability;

            left = Math.min(left, x);
            right = Math.max(right, x);
            top = Math.min(top, y);
            bottom = Math.max(bottom, y);

            for (int index = 0;
                 index < NEIGHBOR_X.length;
                 index++) {
                int neighborX = x + NEIGHBOR_X[index];
                int neighborY = y + NEIGHBOR_Y[index];

                if (neighborX < 0
                        || neighborX >= width
                        || neighborY < 0
                        || neighborY >= height
                        || visited[neighborY][neighborX]) {
                    continue;
                }

                if (probabilityMap[neighborY][neighborX]
                        < configuration
                        .getPixelThreshold()) {
                    continue;
                }

                visited[neighborY][neighborX] = true;
                queue.add(
                        neighborY * width + neighborX
                );
            }
        }

        int exclusiveRight = right + 1;
        int exclusiveBottom = bottom + 1;

        int componentWidth =
                exclusiveRight - left;
        int componentHeight =
                exclusiveBottom - top;

        float confidence =
                probabilitySum / pixelCount;

        if (componentWidth
                < configuration.getMinimumRegionSize()
                || componentHeight
                < configuration.getMinimumRegionSize()
                || confidence
                < configuration.getBoxThreshold()) {
            return null;
        }

        return new Candidate(
                left,
                top,
                exclusiveRight,
                exclusiveBottom,
                confidence
        );
    }

    private void validateProbabilityMap(
            float[][] probabilityMap
    ) throws TextDetectionException {
        if (probabilityMap == null
                || probabilityMap.length == 0
                || probabilityMap[0] == null
                || probabilityMap[0].length == 0) {
            throw new TextDetectionException(
                    TextDetectionException.Error
                            .OUTPUT_SHAPE_INCOMPATIBLE,
                    "Detector probability map is empty"
            );
        }

        int expectedWidth =
                probabilityMap[0].length;

        for (float[] row : probabilityMap) {
            if (row == null
                    || row.length != expectedWidth) {
                throw new TextDetectionException(
                        TextDetectionException.Error
                                .OUTPUT_SHAPE_INCOMPATIBLE,
                        "Detector probability map is not rectangular"
                );
            }

            for (float value : row) {
                if (!Float.isFinite(value)
                        || value < 0.0f
                        || value > 1.0f) {
                    throw new TextDetectionException(
                            TextDetectionException.Error
                                    .OUTPUT_VALUE_INVALID,
                            "Detector output contains invalid probabilities"
                    );
                }
            }
        }
    }
}
