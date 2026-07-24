package com.rndymi.almacentracker.core.csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CsvRecordCodec {

    public static final String LINE_SEPARATOR = "\r\n";

    private CsvRecordCodec() {
    }

    public static void appendRecord(
            StringBuilder csv,
            List<String> columns
    ) {
        Objects.requireNonNull(csv);
        Objects.requireNonNull(columns);

        for (int index = 0;
             index < columns.size();
             index++) {

            if (index > 0) {
                csv.append(',');
            }

            csv.append(escape(columns.get(index)));
        }

        csv.append(LINE_SEPARATOR);
    }

    public static List<Record> parse(String csv) {
        Objects.requireNonNull(csv);

        List<Record> records = new ArrayList<>();
        List<String> currentColumns = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();

        boolean insideQuotes = false;
        boolean quoteClosed = false;
        int currentLine = 1;
        int recordStartLine = 1;

        for (int index = 0;
             index < csv.length();
             index++) {

            char current = csv.charAt(index);

            if (insideQuotes) {
                if (current == '"') {
                    if (index + 1 < csv.length()
                            && csv.charAt(index + 1) == '"') {
                        currentField.append('"');
                        index++;
                    } else {
                        insideQuotes = false;
                        quoteClosed = true;
                    }

                    continue;
                }

                if (current == '\r') {
                    currentField.append(current);

                    if (index + 1 < csv.length()
                            && csv.charAt(index + 1) == '\n') {
                        currentField.append('\n');
                        index++;
                    }

                    currentLine++;
                    continue;
                }

                if (current == '\n') {
                    currentField.append(current);
                    currentLine++;
                    continue;
                }

                currentField.append(current);
                continue;
            }

            if (quoteClosed) {
                if (current == ',') {
                    finishField(currentColumns, currentField);
                    quoteClosed = false;
                    continue;
                }

                if (current == '\r' || current == '\n') {
                    finishField(currentColumns, currentField);
                    finishRecord(
                            records,
                            currentColumns,
                            recordStartLine
                    );
                    quoteClosed = false;

                    if (current == '\r'
                            && index + 1 < csv.length()
                            && csv.charAt(index + 1) == '\n') {
                        index++;
                    }

                    currentLine++;
                    recordStartLine = currentLine;
                    continue;
                }

                throw new IllegalArgumentException(
                        "Unexpected character after closing quote"
                );
            }

            if (current == '"') {
                if (currentField.length() != 0) {
                    throw new IllegalArgumentException(
                            "Unexpected quote inside unquoted field"
                    );
                }

                insideQuotes = true;
                continue;
            }

            if (current == ',') {
                finishField(currentColumns, currentField);
                continue;
            }

            if (current == '\r' || current == '\n') {
                finishField(currentColumns, currentField);
                finishRecord(
                        records,
                        currentColumns,
                        recordStartLine
                );

                if (current == '\r'
                        && index + 1 < csv.length()
                        && csv.charAt(index + 1) == '\n') {
                    index++;
                }

                currentLine++;
                recordStartLine = currentLine;
                continue;
            }

            currentField.append(current);
        }

        if (insideQuotes) {
            throw new IllegalArgumentException(
                    "CSV contains an unclosed quoted field"
            );
        }

        if (quoteClosed
                || currentField.length() > 0
                || !currentColumns.isEmpty()) {
            finishField(currentColumns, currentField);
            finishRecord(
                    records,
                    currentColumns,
                    recordStartLine
            );
        }

        return Collections.unmodifiableList(records);
    }

    private static String escape(String value) {
        String safeValue = value == null ? "" : value;

        boolean requiresQuotes =
                safeValue.indexOf(',') >= 0
                        || safeValue.indexOf('"') >= 0
                        || safeValue.indexOf('\r') >= 0
                        || safeValue.indexOf('\n') >= 0;

        if (!requiresQuotes) {
            return safeValue;
        }

        return '"'
                + safeValue.replace("\"", "\"\"")
                + '"';
    }

    private static void finishField(
            List<String> currentColumns,
            StringBuilder currentField
    ) {
        currentColumns.add(currentField.toString());
        currentField.setLength(0);
    }

    private static void finishRecord(
            List<Record> records,
            List<String> currentColumns,
            int rowNumber
    ) {
        records.add(
                new Record(
                        rowNumber,
                        currentColumns
                )
        );
        currentColumns.clear();
    }

    public static final class Record {

        private final int rowNumber;
        private final List<String> columns;

        private Record(
                int rowNumber,
                List<String> columns
        ) {
            this.rowNumber = rowNumber;
            this.columns = Collections.unmodifiableList(
                    new ArrayList<>(columns)
            );
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public List<String> getColumns() {
            return columns;
        }

        public boolean isCompletelyEmpty() {
            for (String column : columns) {
                if (column != null
                        && !column.trim().isEmpty()) {
                    return false;
                }
            }

            return true;
        }
    }
}
