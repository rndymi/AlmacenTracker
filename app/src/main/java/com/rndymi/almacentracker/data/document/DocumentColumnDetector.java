package com.rndymi.almacentracker.data.document;

import com.rndymi.almacentracker.core.document.RecognizedTextLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class DocumentColumnDetector {

    private static final int MINIMUM_LINES_PER_COLUMN = 2;
    private static final int MINIMUM_COLUMN_COUNT = 2;

    private static final float GLOBAL_LINE_WIDTH_FACTOR = 0.62f;

    private static final float MINIMUM_COLUMN_GAP_FACTOR = 0.055f;
    private static final float MINIMUM_WIDTH_BASED_GAP_FACTOR = 0.45f;

    private static final float MAXIMUM_COLUMN_CENTER_SPREAD_FACTOR = 0.14f;
    private static final float MAXIMUM_COLUMN_OVERLAP_FACTOR = 0.20f;

    public List<RecognizedTextLine> orderByColumns(
            List<RecognizedTextLine> sourceLines,
            int documentWidth
    ) {
        if (sourceLines == null
                || sourceLines.isEmpty()) {
            return Collections.emptyList();
        }

        List<RecognizedTextLine> verticallyOrdered =
                new ArrayList<>(sourceLines);

        sortVertically(verticallyOrdered);

        if (documentWidth <= 0
                || verticallyOrdered.size()
                < MINIMUM_LINES_PER_COLUMN
                * MINIMUM_COLUMN_COUNT) {
            return verticallyOrdered;
        }

        List<RecognizedTextLine> ordered =
                new ArrayList<>(
                        verticallyOrdered.size()
                );

        List<RecognizedTextLine> currentSegment =
                new ArrayList<>();

        for (RecognizedTextLine line
                : verticallyOrdered) {

            if (isGlobalLine(
                    line,
                    documentWidth
            )) {
                appendOrderedSegment(
                        ordered,
                        currentSegment,
                        documentWidth
                );

                currentSegment.clear();

                ordered.add(line);
            } else {
                currentSegment.add(line);
            }
        }

        appendOrderedSegment(
                ordered,
                currentSegment,
                documentWidth
        );

        return ordered;
    }

    private void appendOrderedSegment(
            List<RecognizedTextLine> destination,
            List<RecognizedTextLine> segment,
            int documentWidth
    ) {
        if (segment.isEmpty()) {
            return;
        }

        ColumnLayout layout =
                detectLayout(
                        segment,
                        documentWidth
                );

        if (layout == null) {
            List<RecognizedTextLine> fallback =
                    new ArrayList<>(segment);

            sortVertically(fallback);

            destination.addAll(fallback);

            return;
        }

        for (DocumentColumn column
                : layout.columns) {
            destination.addAll(
                    column.lines
            );
        }
    }

    private ColumnLayout detectLayout(
            List<RecognizedTextLine> segment,
            int documentWidth
    ) {
        List<RecognizedTextLine> candidates =
                new ArrayList<>();

        for (RecognizedTextLine line : segment) {
            if (isColumnCandidate(
                    line,
                    documentWidth
            )) {
                candidates.add(line);
            }
        }

        if (candidates.size()
                < MINIMUM_LINES_PER_COLUMN
                * MINIMUM_COLUMN_COUNT) {
            return null;
        }

        candidates.sort(
                Comparator.comparingDouble(
                        this::centerX
                )
        );

        float medianWidth =
                medianWidth(candidates);

        float minimumGap =
                Math.max(
                        documentWidth
                                * MINIMUM_COLUMN_GAP_FACTOR,
                        medianWidth
                                * MINIMUM_WIDTH_BASED_GAP_FACTOR
                );

        List<DocumentColumn> columns =
                buildColumns(
                        candidates,
                        minimumGap
                );

        if (columns.size()
                < MINIMUM_COLUMN_COUNT
                || !hasValidColumns(
                columns,
                documentWidth
        )) {
            return null;
        }

        for (RecognizedTextLine line : segment) {
            if (candidates.contains(line)) {
                continue;
            }

            DocumentColumn nearest =
                    findNearestColumn(
                            line,
                            columns
                    );

            if (nearest == null) {
                return null;
            }

            nearest.lines.add(line);
            nearest.recalculateBounds();
        }

        columns.sort(
                Comparator.comparingDouble(
                        column ->
                                column.centerX
                )
        );

        for (DocumentColumn column : columns) {
            sortVertically(column.lines);
        }

        return new ColumnLayout(columns);
    }

    private List<DocumentColumn> buildColumns(
            List<RecognizedTextLine> candidates,
            float minimumGap
    ) {
        List<DocumentColumn> columns =
                new ArrayList<>();

        DocumentColumn current =
                new DocumentColumn();

        current.add(candidates.get(0));

        for (int index = 1;
             index < candidates.size();
             index++) {

            RecognizedTextLine previous =
                    candidates.get(index - 1);

            RecognizedTextLine currentLine =
                    candidates.get(index);

            float centerGap =
                    centerX(currentLine)
                            - centerX(previous);

            if (centerGap >= minimumGap) {
                columns.add(current);

                current =
                        new DocumentColumn();
            }

            current.add(currentLine);
        }

        columns.add(current);

        return columns;
    }

    private boolean hasValidColumns(
            List<DocumentColumn> columns,
            int documentWidth
    ) {
        for (DocumentColumn column : columns) {
            if (column.lines.size()
                    < MINIMUM_LINES_PER_COLUMN) {
                return false;
            }

            if (column.centerSpread()
                    > documentWidth
                    * MAXIMUM_COLUMN_CENTER_SPREAD_FACTOR) {
                return false;
            }
        }

        columns.sort(
                Comparator.comparingDouble(
                        column ->
                                column.centerX
                )
        );

        for (int index = 0;
             index < columns.size() - 1;
             index++) {

            DocumentColumn left =
                    columns.get(index);

            DocumentColumn right =
                    columns.get(index + 1);

            int overlap =
                    Math.max(
                            0,
                            left.right - right.left
                    );

            int narrowerWidth =
                    Math.min(
                            left.width(),
                            right.width()
                    );

            if (narrowerWidth > 0
                    && overlap
                    > narrowerWidth
                    * MAXIMUM_COLUMN_OVERLAP_FACTOR) {
                return false;
            }
        }

        return true;
    }

    private DocumentColumn findNearestColumn(
            RecognizedTextLine line,
            List<DocumentColumn> columns
    ) {
        if (!line.hasBoundingBox()) {
            return null;
        }

        DocumentColumn nearest = null;

        float nearestDistance =
                Float.MAX_VALUE;

        for (DocumentColumn column : columns) {
            float distance =
                    Math.abs(
                            centerX(line)
                                    - column.centerX
                    );

            if (distance < nearestDistance) {
                nearest = column;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    private boolean isGlobalLine(
            RecognizedTextLine line,
            int documentWidth
    ) {
        if (!line.hasBoundingBox()) {
            return false;
        }

        int width =
                line.getRight()
                        - line.getLeft();

        return width >= documentWidth
                * GLOBAL_LINE_WIDTH_FACTOR;
    }

    private boolean isColumnCandidate(
            RecognizedTextLine line,
            int documentWidth
    ) {
        if (!line.hasBoundingBox()
                || line.getDisplayText()
                .trim()
                .isEmpty()) {
            return false;
        }

        int width =
                line.getRight()
                        - line.getLeft();

        return width > 0
                && width < documentWidth
                * GLOBAL_LINE_WIDTH_FACTOR;
    }

    private float medianWidth(
            List<RecognizedTextLine> lines
    ) {
        List<Integer> widths =
                new ArrayList<>();

        for (RecognizedTextLine line : lines) {
            widths.add(
                    line.getRight()
                            - line.getLeft()
            );
        }

        widths.sort(Integer::compareTo);

        int middle =
                widths.size() / 2;

        if (widths.size() % 2 == 0) {
            return (
                    widths.get(middle - 1)
                            + widths.get(middle)
            ) / 2.0f;
        }

        return widths.get(middle);
    }

    private float centerX(
            RecognizedTextLine line
    ) {
        return (
                line.getLeft()
                        + line.getRight()
        ) / 2.0f;
    }

    private void sortVertically(
            List<RecognizedTextLine> lines
    ) {
        lines.sort(
                Comparator
                        .comparingInt(
                                (
                                        RecognizedTextLine line
                                ) ->
                                        line.getTop() == null
                                                ? Integer.MAX_VALUE
                                                : line.getTop()
                        )
                        .thenComparingInt(
                                line ->
                                        line.getLeft() == null
                                                ? Integer.MAX_VALUE
                                                : line.getLeft()
                        )
                        .thenComparingInt(
                                RecognizedTextLine::getIndex
                        )
        );
    }

    private static final class ColumnLayout {

        private final List<DocumentColumn> columns;

        private ColumnLayout(
                List<DocumentColumn> columns
        ) {
            this.columns = columns;
        }
    }

    private static final class DocumentColumn {

        private final List<RecognizedTextLine> lines =
                new ArrayList<>();

        private int left =
                Integer.MAX_VALUE;

        private int right =
                Integer.MIN_VALUE;

        private float centerX;

        private float minimumCenter =
                Float.MAX_VALUE;

        private float maximumCenter =
                -Float.MAX_VALUE;

        private void add(
                RecognizedTextLine line
        ) {
            lines.add(line);
            recalculateBounds();
        }

        private void recalculateBounds() {
            left = Integer.MAX_VALUE;
            right = Integer.MIN_VALUE;

            minimumCenter =
                    Float.MAX_VALUE;

            maximumCenter =
                    -Float.MAX_VALUE;

            float centerSum = 0.0f;

            int boundedLineCount = 0;

            for (RecognizedTextLine line
                    : lines) {

                if (!line.hasBoundingBox()) {
                    continue;
                }

                left = Math.min(
                        left,
                        line.getLeft()
                );

                right = Math.max(
                        right,
                        line.getRight()
                );

                float lineCenter =
                        (
                                line.getLeft()
                                        + line.getRight()
                        ) / 2.0f;

                minimumCenter =
                        Math.min(
                                minimumCenter,
                                lineCenter
                        );

                maximumCenter =
                        Math.max(
                                maximumCenter,
                                lineCenter
                        );

                centerSum += lineCenter;
                boundedLineCount++;
            }

            centerX =
                    boundedLineCount == 0
                            ? 0.0f
                            : centerSum
                              / boundedLineCount;
        }

        private float centerSpread() {
            if (minimumCenter
                    == Float.MAX_VALUE) {
                return 0.0f;
            }

            return maximumCenter
                    - minimumCenter;
        }

        private int width() {
            if (left == Integer.MAX_VALUE
                    || right
                    == Integer.MIN_VALUE) {
                return 0;
            }

            return right - left;
        }
    }
}