package com.rndymi.almacentracker.data.document;

import com.rndymi.almacentracker.core.document.RecognizedTextElement;
import com.rndymi.almacentracker.core.document.RecognizedTextLine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class DocumentLineReconstructor {

    private static final float MINIMUM_VERTICAL_OVERLAP = 0.35f;
    private static final float MAXIMUM_CENTER_DISTANCE_FACTOR = 0.65f;

    public List<RecognizedTextLine> reconstruct(
            List<RecognizedTextElement> sourceElements
    ) {
        List<RecognizedTextElement> elements =
                cleanAndSort(sourceElements);

        List<SpatialRow> rows =
                new ArrayList<>();

        for (RecognizedTextElement element : elements) {
            SpatialRow bestRow =
                    findBestRow(
                            rows,
                            element
                    );

            if (bestRow == null) {
                bestRow = new SpatialRow();
                rows.add(bestRow);
            }

            bestRow.add(element);
        }

        rows.sort(
                Comparator.comparingInt(
                        SpatialRow::getTop
                )
        );

        List<RecognizedTextLine> result =
                new ArrayList<>();

        for (int index = 0;
             index < rows.size();
             index++) {

            SpatialRow row = rows.get(index);

            row.sortHorizontally();

            result.add(
                    new RecognizedTextLine(
                            index,
                            row.rawText(),
                            row.reconstructedText(),
                            row.getLeft(),
                            row.getTop(),
                            row.getRight(),
                            row.getBottom(),
                            row.copyElements()
                    )
            );
        }

        return result;
    }

    private List<RecognizedTextElement> cleanAndSort(
            List<RecognizedTextElement> sourceElements
    ) {
        List<RecognizedTextElement> result =
                new ArrayList<>();

        if (sourceElements == null) {
            return result;
        }

        for (RecognizedTextElement element
                : sourceElements) {
            if (element != null
                    && element.hasText()
                    && element.getHeight() > 0) {
                result.add(element);
            }
        }

        result.sort(
                Comparator
                        .comparingInt(
                                RecognizedTextElement::getTop
                        )
                        .thenComparingInt(
                                RecognizedTextElement::getLeft
                        )
        );

        return result;
    }

    private SpatialRow findBestRow(
            List<SpatialRow> rows,
            RecognizedTextElement element
    ) {
        SpatialRow bestRow = null;
        float bestScore = Float.NEGATIVE_INFINITY;

        for (SpatialRow row : rows) {
            float overlap =
                    verticalOverlapRatio(
                            row,
                            element
                    );

            float centerDistance =
                    Math.abs(
                            row.getCenterY()
                                    - element.getCenterY()
                    );

            float referenceHeight =
                    Math.max(
                            1f,
                            Math.min(
                                    row.getAverageHeight(),
                                    element.getHeight()
                            )
                    );

            boolean compatible =
                    overlap
                            >= MINIMUM_VERTICAL_OVERLAP
                            || centerDistance
                            <= referenceHeight
                            * MAXIMUM_CENTER_DISTANCE_FACTOR;

            if (!compatible) {
                continue;
            }

            float score =
                    overlap
                            - centerDistance
                            / Math.max(
                            1f,
                            referenceHeight
                    );

            if (score > bestScore) {
                bestScore = score;
                bestRow = row;
            }
        }

        return bestRow;
    }

    private float verticalOverlapRatio(
            SpatialRow row,
            RecognizedTextElement element
    ) {
        int overlapTop =
                Math.max(
                        row.getTop(),
                        element.getTop()
                );

        int overlapBottom =
                Math.min(
                        row.getBottom(),
                        element.getBottom()
                );

        int overlap =
                Math.max(
                        0,
                        overlapBottom - overlapTop
                );

        int minimumHeight =
                Math.max(
                        1,
                        Math.min(
                                row.getHeight(),
                                element.getHeight()
                        )
                );

        return overlap / (float) minimumHeight;
    }

    private static final class SpatialRow {

        private final List<RecognizedTextElement> elements =
                new ArrayList<>();

        private int left = Integer.MAX_VALUE;
        private int top = Integer.MAX_VALUE;
        private int right = Integer.MIN_VALUE;
        private int bottom = Integer.MIN_VALUE;

        private void add(
                RecognizedTextElement element
        ) {
            elements.add(element);

            left = Math.min(left, element.getLeft());
            top = Math.min(top, element.getTop());
            right = Math.max(right, element.getRight());
            bottom = Math.max(
                    bottom,
                    element.getBottom()
            );
        }

        private void sortHorizontally() {
            elements.sort(
                    Comparator.comparingInt(
                            RecognizedTextElement::getLeft
                    )
            );
        }

        private String rawText() {
            StringBuilder result =
                    new StringBuilder();

            for (RecognizedTextElement element
                    : elements) {
                if (result.length() > 0) {
                    result.append(' ');
                }

                result.append(
                        element.getRawText()
                );
            }

            return normalizeSpaces(
                    result.toString()
            );
        }

        private String reconstructedText() {
            if (elements.isEmpty()) {
                return "";
            }

            float averageCharacterWidth =
                    calculateAverageCharacterWidth();

            StringBuilder result =
                    new StringBuilder();

            RecognizedTextElement previous = null;

            for (RecognizedTextElement current
                    : elements) {
                if (previous != null) {
                    int horizontalGap =
                            current.getLeft()
                                    - previous.getRight();

                    /*
                     * Se introduce un único espacio para evitar
                     * producir columnas visuales artificiales.
                     */
                    if (horizontalGap
                            > averageCharacterWidth * 0.20f) {
                        result.append(' ');
                    }
                }

                result.append(
                        current.getRawText()
                );

                previous = current;
            }

            return normalizeSpaces(
                    result.toString()
            );
        }

        private float calculateAverageCharacterWidth() {
            float totalWidth = 0f;
            int characterCount = 0;

            for (RecognizedTextElement element
                    : elements) {
                String text =
                        element.getRawText();

                if (text.isEmpty()) {
                    continue;
                }

                totalWidth += element.getWidth();
                characterCount += text.length();
            }

            if (characterCount <= 0) {
                return 1f;
            }

            return Math.max(
                    1f,
                    totalWidth / characterCount
            );
        }

        private List<RecognizedTextElement>
        copyElements() {
            return new ArrayList<>(elements);
        }

        private int getLeft() {
            return left;
        }

        private int getTop() {
            return top;
        }

        private int getRight() {
            return right;
        }

        private int getBottom() {
            return bottom;
        }

        private int getHeight() {
            return Math.max(
                    1,
                    bottom - top
            );
        }

        private float getCenterY() {
            return top + getHeight() / 2.0f;
        }

        private float getAverageHeight() {
            if (elements.isEmpty()) {
                return 1f;
            }

            float total = 0f;

            for (RecognizedTextElement element
                    : elements) {
                total += element.getHeight();
            }

            return Math.max(
                    1f,
                    total / elements.size()
            );
        }

        private static String normalizeSpaces(
                String value
        ) {
            return value
                    .trim()
                    .replaceAll(
                            "[\\p{Z}\\s]+",
                            " "
                    );
        }
    }
}
