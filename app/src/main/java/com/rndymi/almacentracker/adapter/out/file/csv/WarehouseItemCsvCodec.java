package com.rndymi.almacentracker.adapter.out.file.csv;

import com.rndymi.almacentracker.application.result.ImportWarehouseItemIssue;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvReadResult;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvRow;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WarehouseItemCsvCodec {

    private static final String HEADER =
            "category,code,site,position,observations";

    private static final List<String> HEADER_COLUMNS =
            Collections.unmodifiableList(
                    Arrays.asList(
                            "category",
                            "code",
                            "site",
                            "position",
                            "observations"
                    )
            );

    private static final String LINE_SEPARATOR = "\r\n";
    private static final int EXPECTED_COLUMN_COUNT = 5;

    private final WarehouseItemCsvMapper mapper;

    public WarehouseItemCsvCodec(
            WarehouseItemCsvMapper mapper
    ) {
        this.mapper = Objects.requireNonNull(mapper);
    }

    public byte[] encode(
            List<WarehouseItem> warehouseItems
    ) {
        Objects.requireNonNull(warehouseItems);

        StringBuilder csv = new StringBuilder();
        csv.append(HEADER).append(LINE_SEPARATOR);

        for (WarehouseItem warehouseItem : warehouseItems) {
            appendRow(
                    csv,
                    mapper.toColumns(warehouseItem)
            );
        }

        return csv.toString().getBytes(
                StandardCharsets.UTF_8
        );
    }

    public WarehouseItemCsvReadResult decode(
            byte[] csvBytes
    ) throws WarehouseItemCsvFormatException {
        Objects.requireNonNull(csvBytes);

        return decode(
                new String(
                        csvBytes,
                        StandardCharsets.UTF_8
                )
        );
    }

    WarehouseItemCsvReadResult decode(
            String csv
    ) throws WarehouseItemCsvFormatException {
        Objects.requireNonNull(csv);

        String normalizedCsv = removeUtf8Bom(csv);

        if (normalizedCsv.isEmpty()) {
            return emptyReadResult();
        }

        List<ParsedCsvRecord> records =
                parseRecords(normalizedCsv);

        if (records.isEmpty()) {
            return emptyReadResult();
        }

        validateHeader(
                records.get(0).columns
        );

        List<WarehouseItemCsvRow> rows =
                new ArrayList<>();

        List<ImportWarehouseItemIssue> issues =
                new ArrayList<>();

        int totalRows = 0;

        for (int index = 1;
             index < records.size();
             index++) {

            ParsedCsvRecord record =
                    records.get(index);

            if (isCompletelyEmpty(record.columns)) {
                continue;
            }

            totalRows++;

            if (record.columns.size()
                    != EXPECTED_COLUMN_COUNT) {

                issues.add(
                        ImportWarehouseItemIssue
                                .invalidColumnCount(
                                        record.rowNumber,
                                        record.columns.size(),
                                        EXPECTED_COLUMN_COUNT
                                )
                );

                continue;
            }

            rows.add(
                    new WarehouseItemCsvRow(
                            record.rowNumber,
                            unprotectFormula(
                                    record.columns.get(0)
                            ),
                            unprotectFormula(
                                    record.columns.get(1)
                            ),
                            unprotectFormula(
                                    record.columns.get(2)
                            ),
                            unprotectFormula(
                                    record.columns.get(3)
                            ),
                            unprotectFormula(
                                    record.columns.get(4)
                            )
                    )
            );
        }

        return new WarehouseItemCsvReadResult(
                rows,
                totalRows,
                issues
        );
    }

    private WarehouseItemCsvReadResult emptyReadResult() {
        return new WarehouseItemCsvReadResult(
                Collections.emptyList(),
                0,
                Collections.emptyList()
        );
    }

    private List<ParsedCsvRecord> parseRecords(
            String csv
    ) throws WarehouseItemCsvFormatException {
        List<ParsedCsvRecord> records =
                new ArrayList<>();

        List<String> currentRecord =
                new ArrayList<>();

        StringBuilder currentField =
                new StringBuilder();

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
                    finishField(
                            currentRecord,
                            currentField
                    );

                    quoteClosed = false;
                    continue;
                }

                if (current == '\r'
                        || current == '\n') {
                    finishField(
                            currentRecord,
                            currentField
                    );

                    finishRecord(
                            records,
                            currentRecord,
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

                throw new WarehouseItemCsvFormatException(
                        "Unexpected character after closing quote"
                );
            }

            if (current == '"') {
                if (currentField.length() != 0) {
                    throw new WarehouseItemCsvFormatException(
                            "Unexpected quote inside unquoted field"
                    );
                }

                insideQuotes = true;
                continue;
            }

            if (current == ',') {
                finishField(
                        currentRecord,
                        currentField
                );
                continue;
            }

            if (current == '\r'
                    || current == '\n') {
                finishField(
                        currentRecord,
                        currentField
                );

                finishRecord(
                        records,
                        currentRecord,
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
            throw new WarehouseItemCsvFormatException(
                    "CSV contains an unclosed quoted field"
            );
        }

        if (quoteClosed
                || currentField.length() > 0
                || !currentRecord.isEmpty()) {
            finishField(
                    currentRecord,
                    currentField
            );

            finishRecord(
                    records,
                    currentRecord,
                    recordStartLine
            );
        }

        return records;
    }

    private void validateHeader(
            List<String> header
    ) throws WarehouseItemCsvFormatException {
        if (!HEADER_COLUMNS.equals(header)) {
            throw new WarehouseItemCsvFormatException(
                    "Unexpected CSV header"
            );
        }
    }

    private void finishField(
            List<String> currentRecord,
            StringBuilder currentField
    ) {
        currentRecord.add(
                currentField.toString()
        );

        currentField.setLength(0);
    }

    private void finishRecord(
            List<ParsedCsvRecord> records,
            List<String> currentRecord,
            int rowNumber
    ) {
        records.add(
                new ParsedCsvRecord(
                        rowNumber,
                        currentRecord
                )
        );

        currentRecord.clear();
    }

    private boolean isCompletelyEmpty(
            List<String> record
    ) {
        for (String value : record) {
            if (value != null
                    && !value.trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private String removeUtf8Bom(
            String value
    ) {
        if (!value.isEmpty()
                && value.charAt(0) == '\uFEFF') {
            return value.substring(1);
        }

        return value;
    }

    private void appendRow(
            StringBuilder csv,
            List<String> columns
    ) {
        for (int index = 0;
             index < columns.size();
             index++) {

            if (index > 0) {
                csv.append(',');
            }

            csv.append(
                    escape(
                            protectFormula(
                                    columns.get(index)
                            )
                    )
            );
        }

        csv.append(LINE_SEPARATOR);
    }

    private String escape(
            String value
    ) {
        String safeValue =
                value == null ? "" : value;

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

    private String protectFormula(
            String value
    ) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        if (value.charAt(0) == '\'') {
            return "'" + value;
        }

        char firstCharacter = value.charAt(0);

        if (firstCharacter == '='
                || firstCharacter == '+'
                || firstCharacter == '-'
                || firstCharacter == '@') {
            return "'" + value;
        }

        return value;
    }

    private String unprotectFormula(
            String value
    ) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        if (value.startsWith("''")) {
            return value.substring(1);
        }

        if (value.length() >= 2
                && value.charAt(0) == '\''
                && isFormulaPrefix(value.charAt(1))) {
            return value.substring(1);
        }

        return value;
    }

    private boolean isFormulaPrefix(
            char value
    ) {
        return value == '='
                || value == '+'
                || value == '-'
                || value == '@';
    }

    private static final class ParsedCsvRecord {

        private final int rowNumber;
        private final List<String> columns;

        private ParsedCsvRecord(
                int rowNumber,
                List<String> columns
        ) {
            this.rowNumber = rowNumber;
            this.columns = new ArrayList<>(columns);
        }
    }
}