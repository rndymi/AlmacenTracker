package com.rndymi.almacentracker.data.document;

import com.rndymi.almacentracker.core.document.RecognizedTextLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class DocumentColumnDetector {

    private static final int MINIMUM_LINES_PER_COLUMN = 2;

    private static final float MINIMUM_COLUMN_GAP_FACTOR = 0.16f;

    public List<RecognizedTextLine> orderByColumns(
            List<RecognizedTextLine> sourceLines,
            int documentWidth
    ) {
        if (sourceLines == null
                || sourceLines.isEmpty()) {
            return Collections.emptyList();
        }

        List<RecognizedTextLine> lines =
                new ArrayList<>(sourceLines);

        if (documentWidth <= 0
                || lines.size() < 4) {
            sortVertically(lines);
            return lines;
        }

        ColumnDivision division =
                detectDivision(
                        lines,
                        documentWidth
                );

        if (division == null) {
            sortVertically(lines);
            return lines;
        }

        List<RecognizedTextLine> leftColumn =
                new ArrayList<>();

        List<RecognizedTextLine> rightColumn =
                new ArrayList<>();

        for (RecognizedTextLine line : lines) {
            if (!line.hasBoundingBox()) {
                leftColumn.add(line);
                continue;
            }

            float centerX =
                    (
                            line.getLeft()
                                    + line.getRight()
                    ) / 2.0f;

            if (centerX <= division.splitX) {
                leftColumn.add(line);
            } else {
                rightColumn.add(line);
            }
        }

        if (leftColumn.size()
                < MINIMUM_LINES_PER_COLUMN
                || rightColumn.size()
                < MINIMUM_LINES_PER_COLUMN) {
            sortVertically(lines);
            return lines;
        }

        sortVertically(leftColumn);
        sortVertically(rightColumn);

        List<RecognizedTextLine> ordered =
                new ArrayList<>(
                        leftColumn.size()
                                + rightColumn.size()
                );

        ordered.addAll(leftColumn);
        ordered.addAll(rightColumn);

        return ordered;
    }

    private ColumnDivision detectDivision(
            List<RecognizedTextLine> lines,
            int documentWidth
    ) {
        List<Float> centers =
                new ArrayList<>();

        for (RecognizedTextLine line : lines) {
            if (!line.hasBoundingBox()) {
                continue;
            }

            centers.add(
                    (
                            line.getLeft()
                                    + line.getRight()
                    ) / 2.0f
            );
        }

        if (centers.size() < 4) {
            return null;
        }

        centers.sort(Float::compareTo);

        float largestGap = 0.0f;
        float splitX = 0.0f;
        int splitIndex = -1;

        for (int index = 0;
             index < centers.size() - 1;
             index++) {

            float current =
                    centers.get(index);

            float next =
                    centers.get(index + 1);

            float gap = next - current;

            if (gap > largestGap) {
                largestGap = gap;
                splitX = current + gap / 2.0f;
                splitIndex = index;
            }
        }

        float minimumGap =
                documentWidth
                        * MINIMUM_COLUMN_GAP_FACTOR;

        if (largestGap < minimumGap) {
            return null;
        }

        int leftCount = splitIndex + 1;
        int rightCount =
                centers.size() - leftCount;

        if (leftCount < MINIMUM_LINES_PER_COLUMN
                || rightCount
                < MINIMUM_LINES_PER_COLUMN) {
            return null;
        }

        return new ColumnDivision(splitX);
    }

    private void sortVertically(
            List<RecognizedTextLine> lines
    ) {
        lines.sort(
                Comparator
                        .comparingInt(
                                (RecognizedTextLine line) ->
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
        );
    }

    private static final class ColumnDivision {

        private final float splitX;

        private ColumnDivision(float splitX) {
            this.splitX = splitX;
        }
    }
}
