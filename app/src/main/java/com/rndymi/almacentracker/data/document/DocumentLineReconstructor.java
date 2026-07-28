package com.rndymi.almacentracker.data.document;

import com.rndymi.almacentracker.core.document.RecognizedTextElement;
import com.rndymi.almacentracker.core.document.RecognizedTextLine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DocumentLineReconstructor {

    private static final float MINIMUM_VERTICAL_OVERLAP =
            0.35f;

    private static final float
            MAXIMUM_CENTER_DISTANCE_FACTOR =
            0.65f;

    private static final float
            MINIMUM_ROW_SPLIT_GAP_FACTOR =
            0.10f;

    private static final float
            CHARACTER_GAP_SPLIT_FACTOR =
            4.5f;

    private final DocumentColumnDetector columnDetector;

    public DocumentLineReconstructor() {
        this(new DocumentColumnDetector());
    }

    public DocumentLineReconstructor(
            DocumentColumnDetector columnDetector
    ) {
        if (columnDetector == null) {
            throw new IllegalArgumentException(
                    "columnDetector cannot be null"
            );
        }

        this.columnDetector = columnDetector;
    }

    public List<RecognizedTextLine> reconstruct(
            List<RecognizedTextElement> sourceElements
    ) {
        return reconstruct(
                sourceElements,
                calculateDocumentWidth(
                        sourceElements
                )
        );
    }

    public List<RecognizedTextLine> reconstruct(
            List<RecognizedTextElement> sourceElements,
            int documentWidth
    ) {
        List<RecognizedTextElement> elements =
                cleanAndSort(sourceElements);

        List<SpatialRow> initialRows =
                groupVertically(elements);

        List<SpatialRow> separatedRows =
                splitRowsByLargeHorizontalGaps(
                        initialRows,
                        documentWidth
                );

        List<RecognizedTextLine> reconstructed =
                mapLines(separatedRows);

        List<RecognizedTextLine> ordered =
                columnDetector.orderByColumns(
                        reconstructed,
                        documentWidth
                );

        return reindex(ordered);
    }

    private List<SpatialRow> groupVertically(
            List<RecognizedTextElement> elements
    ) {
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

        return rows;
    }

    private List<SpatialRow>
    splitRowsByLargeHorizontalGaps(
            List<SpatialRow> rows,
            int documentWidth
    ) {
        List<SpatialRow> result =
                new ArrayList<>();

        for (SpatialRow row : rows) {
            row.sortHorizontally();

            result.addAll(
                    row.splitByLargeGaps(
                            documentWidth
                    )
            );
        }

        return result;
    }

    private List<RecognizedTextLine> mapLines(
            List<SpatialRow> rows
    ) {
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

    private List<RecognizedTextLine> reindex(
            List<RecognizedTextLine> lines
    ) {
        List<RecognizedTextLine> result =
                new ArrayList<>();

        for (int index = 0;
             index < lines.size();
             index++) {

            RecognizedTextLine line =
                    lines.get(index);

            result.add(
                    new RecognizedTextLine(
                            index,
                            line.getRawText(),
                            line.getReconstructedText(),
                            line.getLeft(),
                            line.getTop(),
                            line.getRight(),
                            line.getBottom(),
                            line.getElements()
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
        float bestScore =
                Float.NEGATIVE_INFINITY;

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
                            1.0f,
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
                            / referenceHeight;

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

        return overlap
                / (float) minimumHeight;
    }

    private int calculateDocumentWidth(
            List<RecognizedTextElement> elements
    ) {
        if (elements == null
                || elements.isEmpty()) {
            return 0;
        }

        int maximumRight = 0;

        for (RecognizedTextElement element
                : elements) {

            if (element != null) {
                maximumRight =
                        Math.max(
                                maximumRight,
                                element.getRight()
                        );
            }
        }

        return maximumRight;
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

            left =
                    Math.min(
                            left,
                            element.getLeft()
                    );

            top =
                    Math.min(
                            top,
                            element.getTop()
                    );

            right =
                    Math.max(
                            right,
                            element.getRight()
                    );

            bottom =
                    Math.max(
                            bottom,
                            element.getBottom()
                    );
        }

        private List<SpatialRow> splitByLargeGaps(
                int documentWidth
        ) {
            List<SpatialRow> result =
                    new ArrayList<>();

            if (elements.isEmpty()) {
                return result;
            }

            if (elements.size() == 1) {
                result.add(copy());
                return result;
            }

            float averageCharacterWidth =
                    calculateAverageCharacterWidth();

            float widthThreshold =
                    documentWidth > 0
                            ? documentWidth
                              * MINIMUM_ROW_SPLIT_GAP_FACTOR
                            : 0.0f;

            float characterThreshold =
                    averageCharacterWidth
                            * CHARACTER_GAP_SPLIT_FACTOR;

            float splitThreshold =
                    Math.max(
                            widthThreshold,
                            characterThreshold
                    );

            SpatialRow current =
                    new SpatialRow();

            RecognizedTextElement previous = null;

            for (RecognizedTextElement element
                    : elements) {

                if (previous != null) {
                    int horizontalGap =
                            element.getLeft()
                                    - previous.getRight();

                    if (horizontalGap
                            >= splitThreshold) {

                        if (!current.elements.isEmpty()) {
                            result.add(current);
                        }

                        current =
                                new SpatialRow();
                    }
                }

                current.add(element);
                previous = element;
            }

            if (!current.elements.isEmpty()) {
                result.add(current);
            }

            return result;
        }

        private SpatialRow copy() {
            SpatialRow copy =
                    new SpatialRow();

            for (RecognizedTextElement element
                    : elements) {
                copy.add(element);
            }

            return copy;
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

                    if (horizontalGap
                            > averageCharacterWidth
                            * 0.20f) {
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
            float totalWidth = 0.0f;
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
                return 1.0f;
            }

            return Math.max(
                    1.0f,
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
            return top
                    + getHeight() / 2.0f;
        }

        private float getAverageHeight() {
            if (elements.isEmpty()) {
                return 1.0f;
            }

            float total = 0.0f;

            for (RecognizedTextElement element
                    : elements) {
                total += element.getHeight();
            }

            return Math.max(
                    1.0f,
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