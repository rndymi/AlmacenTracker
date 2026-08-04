package com.rndymi.almacentracker.data.document;

import com.rndymi.almacentracker.core.document.RecognizedTextLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class DocumentColumnDetector {

    private static final int MINIMUM_LINES_PER_COLUMN = 2;
    private static final int MINIMUM_COLUMN_COUNT = 2;

    /*
     * Las columnas se detectan principalmente mediante sus bordes izquierdos.
     * En listas manuscritas, el ancho y el centro de cada texto pueden variar
     * demasiado para utilizarlos como ancla principal.
     */
    private static final float MINIMUM_LEFT_GAP_DOCUMENT_FACTOR = 0.075f;
    private static final float MINIMUM_LEFT_GAP_WIDTH_FACTOR = 0.70f;

    /*
     * Una línea excesivamente ancha no debe participar en la detección de
     * columnas porque puede ser un título, una anotación o una región OCR
     * que fusionó contenido de columnas adyacentes.
     */
    private static final float MAXIMUM_COLUMN_CANDIDATE_WIDTH_FACTOR = 0.32f;

    /*
     * Variación máxima admitida entre los bordes izquierdos de las líneas
     * que forman una misma columna.
     */
    private static final float MAXIMUM_LEFT_SPREAD_FACTOR = 0.10f;

    /*
     * Distancia máxima para asignar una línea adicional a una columna ya
     * detectada. Las líneas dudosas se conservan aparte y no contaminan el
     * orden de las columnas.
     */
    private static final float MAXIMUM_ASSIGNMENT_DISTANCE_FACTOR = 0.13f;

    public List<RecognizedTextLine> orderByColumns(
            List<RecognizedTextLine> sourceLines,
            int documentWidth
    ) {
        if (sourceLines == null
                || sourceLines.isEmpty()) {
            return Collections.emptyList();
        }

        List<RecognizedTextLine> verticalOrder =
                new ArrayList<>(sourceLines);

        sortVertically(verticalOrder);

        if (documentWidth <= 0
                || verticalOrder.size()
                < MINIMUM_LINES_PER_COLUMN
                * MINIMUM_COLUMN_COUNT) {
            return verticalOrder;
        }

        ColumnLayout layout =
                detectColumnLayout(
                        verticalOrder,
                        documentWidth
                );

        if (layout == null) {
            return verticalOrder;
        }

        return buildReadingOrder(
                verticalOrder,
                layout,
                documentWidth
        );
    }

    private ColumnLayout detectColumnLayout(
            List<RecognizedTextLine> lines,
            int documentWidth
    ) {
        List<RecognizedTextLine> candidates =
                new ArrayList<>();

        for (RecognizedTextLine line : lines) {
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
                Comparator
                        .comparingInt(
                                RecognizedTextLine::getLeft
                        )
                        .thenComparingInt(
                                RecognizedTextLine::getTop
                        )
        );

        float medianWidth =
                calculateMedianWidth(candidates);

        float minimumLeftGap =
                Math.max(
                        documentWidth
                                * MINIMUM_LEFT_GAP_DOCUMENT_FACTOR,
                        medianWidth
                                * MINIMUM_LEFT_GAP_WIDTH_FACTOR
                );

        List<DocumentColumn> columns =
                createColumnGroups(
                        candidates,
                        minimumLeftGap
                );

        removeUnsupportedColumns(columns);

        if (columns.size()
                < MINIMUM_COLUMN_COUNT) {
            return null;
        }

        columns.sort(
                Comparator.comparingDouble(
                        DocumentColumn::getAnchorLeft
                )
        );

        if (!hasStableSeparation(
                columns,
                documentWidth
        )) {
            return null;
        }

        return new ColumnLayout(columns);
    }

    private List<DocumentColumn> createColumnGroups(
            List<RecognizedTextLine> candidates,
            float minimumLeftGap
    ) {
        List<DocumentColumn> columns =
                new ArrayList<>();

        for (RecognizedTextLine line : candidates) {
            DocumentColumn nearest =
                    findNearestColumnByLeft(
                            line,
                            columns,
                            minimumLeftGap
                    );

            if (nearest == null) {
                nearest = new DocumentColumn();
                columns.add(nearest);
            }

            nearest.add(line);
        }

        return columns;
    }

    private DocumentColumn findNearestColumnByLeft(
            RecognizedTextLine line,
            List<DocumentColumn> columns,
            float maximumDistance
    ) {
        DocumentColumn nearest = null;
        float nearestDistance =
                Float.MAX_VALUE;

        for (DocumentColumn column : columns) {
            float distance =
                    Math.abs(
                            line.getLeft()
                                    - column.getAnchorLeft()
                    );

            if (distance <= maximumDistance
                    && distance < nearestDistance) {
                nearest = column;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    private void removeUnsupportedColumns(
            List<DocumentColumn> columns
    ) {
        columns.removeIf(
                column ->
                        column.getLines().size()
                                < MINIMUM_LINES_PER_COLUMN
        );
    }

    private boolean hasStableSeparation(
            List<DocumentColumn> columns,
            int documentWidth
    ) {
        for (DocumentColumn column : columns) {
            if (column.getLeftSpread()
                    > documentWidth
                    * MAXIMUM_LEFT_SPREAD_FACTOR) {
                return false;
            }
        }

        for (int index = 0;
             index < columns.size() - 1;
             index++) {

            DocumentColumn current =
                    columns.get(index);

            DocumentColumn next =
                    columns.get(index + 1);

            if (next.getAnchorLeft()
                    <= current.getAnchorLeft()) {
                return false;
            }
        }

        return true;
    }

    private List<RecognizedTextLine> buildReadingOrder(
            List<RecognizedTextLine> originalLines,
            ColumnLayout layout,
            int documentWidth
    ) {
        List<DocumentColumn> columns =
                layout.copyColumns();

        List<RecognizedTextLine> unassigned =
                new ArrayList<>();

        for (RecognizedTextLine line : originalLines) {
            if (layout.contains(line)) {
                continue;
            }

            DocumentColumn target =
                    findAssignmentColumn(
                            line,
                            columns,
                            documentWidth
                    );

            if (target == null) {
                unassigned.add(line);
            } else {
                target.add(line);
            }
        }

        for (DocumentColumn column : columns) {
            sortVertically(column.getLines());
        }

        columns.sort(
                Comparator.comparingDouble(
                        DocumentColumn::getAnchorLeft
                )
        );

        /*
         * Las líneas no asignables no se utilizan para determinar columnas.
         * Se insertan según su posición vertical, sin modificar el orden
         * interno de las columnas detectadas.
         */
        List<RecognizedTextLine> orderedColumns =
                new ArrayList<>();

        for (DocumentColumn column : columns) {
            orderedColumns.addAll(
                    column.getLines()
            );
        }

        if (unassigned.isEmpty()) {
            return orderedColumns;
        }

        sortVertically(unassigned);

        return mergeUnassignedLines(
                orderedColumns,
                unassigned
        );
    }

    private DocumentColumn findAssignmentColumn(
            RecognizedTextLine line,
            List<DocumentColumn> columns,
            int documentWidth
    ) {
        if (!isColumnCandidate(
                line,
                documentWidth
        )) {
            return null;
        }

        DocumentColumn nearest = null;
        float nearestDistance =
                Float.MAX_VALUE;

        float maximumDistance =
                documentWidth
                        * MAXIMUM_ASSIGNMENT_DISTANCE_FACTOR;

        for (DocumentColumn column : columns) {
            float distance =
                    Math.abs(
                            line.getLeft()
                                    - column.getAnchorLeft()
                    );

            if (distance <= maximumDistance
                    && distance < nearestDistance) {
                nearest = column;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    private List<RecognizedTextLine> mergeUnassignedLines(
            List<RecognizedTextLine> orderedColumns,
            List<RecognizedTextLine> unassigned
    ) {
        List<RecognizedTextLine> result =
                new ArrayList<>(orderedColumns);

        for (RecognizedTextLine line : unassigned) {
            int insertionIndex =
                    findSafeInsertionIndex(
                            result,
                            line
                    );

            result.add(
                    insertionIndex,
                    line
            );
        }

        return result;
    }

    private int findSafeInsertionIndex(
            List<RecognizedTextLine> orderedLines,
            RecognizedTextLine line
    ) {
        if (line.getTop() == null) {
            return orderedLines.size();
        }

        /*
         * Solo las líneas situadas claramente antes de todo el bloque de
         * columnas se presentan al principio. Una anotación ancha que se
         * solape con la primera fila no desplaza referencias de otras
         * columnas por sí sola.
         */
        int minimumTop =
                Integer.MAX_VALUE;

        for (RecognizedTextLine current
                : orderedLines) {
            if (current.getTop() != null) {
                minimumTop =
                        Math.min(
                                minimumTop,
                                current.getTop()
                        );
            }
        }

        if (minimumTop != Integer.MAX_VALUE
                && line.getBottom() != null
                && line.getBottom()
                < minimumTop) {
            return 0;
        }

        return orderedLines.size();
    }

    private boolean isColumnCandidate(
            RecognizedTextLine line,
            int documentWidth
    ) {
        if (line == null
                || !line.hasBoundingBox()
                || line.getDisplayText()
                .trim()
                .isEmpty()) {
            return false;
        }

        int width =
                line.getRight()
                        - line.getLeft();

        return width > 0
                && width
                <= documentWidth
                * MAXIMUM_COLUMN_CANDIDATE_WIDTH_FACTOR;
    }

    private float calculateMedianWidth(
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
        private final List<RecognizedTextLine> detectedLines;

        private ColumnLayout(
                List<DocumentColumn> columns
        ) {
            this.columns =
                    new ArrayList<>(columns);

            this.detectedLines =
                    new ArrayList<>();

            for (DocumentColumn column : columns) {
                detectedLines.addAll(
                        column.getLines()
                );
            }
        }

        private boolean contains(
                RecognizedTextLine line
        ) {
            return detectedLines.contains(line);
        }

        private List<DocumentColumn> copyColumns() {
            List<DocumentColumn> result =
                    new ArrayList<>();

            for (DocumentColumn source : columns) {
                DocumentColumn copy =
                        new DocumentColumn();

                for (RecognizedTextLine line
                        : source.getLines()) {
                    copy.add(line);
                }

                result.add(copy);
            }

            return result;
        }
    }

    private static final class DocumentColumn {

        private final List<RecognizedTextLine> lines =
                new ArrayList<>();

        private float anchorLeft;
        private int minimumLeft =
                Integer.MAX_VALUE;
        private int maximumLeft =
                Integer.MIN_VALUE;

        private void add(
                RecognizedTextLine line
        ) {
            lines.add(line);
            recalculate();
        }

        private void recalculate() {
            float totalLeft = 0.0f;
            int boundedCount = 0;

            minimumLeft =
                    Integer.MAX_VALUE;
            maximumLeft =
                    Integer.MIN_VALUE;

            for (RecognizedTextLine line : lines) {
                if (!line.hasBoundingBox()) {
                    continue;
                }

                int left = line.getLeft();

                totalLeft += left;
                boundedCount++;

                minimumLeft =
                        Math.min(
                                minimumLeft,
                                left
                        );

                maximumLeft =
                        Math.max(
                                maximumLeft,
                                left
                        );
            }

            anchorLeft =
                    boundedCount == 0
                            ? 0.0f
                            : totalLeft
                              / boundedCount;
        }

        private List<RecognizedTextLine> getLines() {
            return lines;
        }

        private float getAnchorLeft() {
            return anchorLeft;
        }

        private float getLeftSpread() {
            if (minimumLeft
                    == Integer.MAX_VALUE) {
                return 0.0f;
            }

            return maximumLeft
                    - minimumLeft;
        }
    }
}
