package com.rndymi.almacentracker.adapter.out.file.csv;

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

        String csv = new String(
                csvBytes,
                StandardCharsets.UTF_8
        );

        return decode(csv);
    }

    WarehouseItemCsvReadResult decode(
            String csv
    ) throws WarehouseItemCsvFormatException {
        Objects.requireNonNull(csv);

        String normalizedCsv = removeUtf8Bom(csv);

        if (normalizedCsv.isEmpty()) {
            return new WarehouseItemCsvReadResult(
                    Collections.emptyList(),
                    0,
                    0
            );
        }

        List<List<String>> records =
                parseRecords(normalizedCsv);

        if (records.isEmpty()) {
            return new WarehouseItemCsvReadResult(
                    Collections.emptyList(),
                    0,
                    0
            );
        }

        validateHeader(records.get(0));

        List<WarehouseItemCsvRow> rows =
                new ArrayList<>();

        int totalRows = 0;
        int invalidRows = 0;

        for (int index = 1;
             index < records.size();
             index++) {

            List<String> record = records.get(index);

            if (isCompletelyEmpty(record)) {
                continue;
            }

            totalRows++;

            if (record.size() != EXPECTED_COLUMN_COUNT) {
                invalidRows++;
                continue;
            }

            rows.add(
                    new WarehouseItemCsvRow(
                            unprotectFormula(record.get(0)),
                            unprotectFormula(record.get(1)),
                            unprotectFormula(record.get(2)),
                            unprotectFormula(record.get(3)),
                            unprotectFormula(record.get(4))
                    )
            );
        }

        return new WarehouseItemCsvReadResult(
                rows,
                totalRows,
                invalidRows
        );
    }

    private List<List<String>> parseRecords(
            String csv
    ) throws WarehouseItemCsvFormatException {
        List<List<String>> records =
                new ArrayList<>();

        List<String> currentRecord =
                new ArrayList<>();

        StringBuilder currentField =
                new StringBuilder();

        boolean insideQuotes = false;
        boolean quoteClosed = false;

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
                } else {
                    currentField.append(current);
                }

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

                if (current == '\r' || current == '\n') {
                    finishField(
                            currentRecord,
                            currentField
                    );
                    finishRecord(records, currentRecord);

                    quoteClosed = false;

                    if (current == '\r'
                            && index + 1 < csv.length()
                            && csv.charAt(index + 1) == '\n') {
                        index++;
                    }

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

            if (current == '\r' || current == '\n') {
                finishField(
                        currentRecord,
                        currentField
                );
                finishRecord(records, currentRecord);

                if (current == '\r'
                        && index + 1 < csv.length()
                        && csv.charAt(index + 1) == '\n') {
                    index++;
                }

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
            finishRecord(records, currentRecord);
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
        currentRecord.add(currentField.toString());
        currentField.setLength(0);
    }

    private void finishRecord(
            List<List<String>> records,
            List<String> currentRecord
    ) {
        records.add(
                new ArrayList<>(currentRecord)
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

    private String unprotectFormula(
            String value
    ) {
        if (value == null || value.length() < 2) {
            return value == null ? "" : value;
        }

        if (value.charAt(0) != '\'') {
            return value;
        }

        char protectedCharacter = value.charAt(1);

        if (protectedCharacter == '\''
                || protectedCharacter == '='
                || protectedCharacter == '+'
                || protectedCharacter == '-'
                || protectedCharacter == '@') {
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

    private String protectFormula(
            String value
    ) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        char firstCharacter = value.charAt(0);

        if (firstCharacter == '\''
                || firstCharacter == '='
                || firstCharacter == '+'
                || firstCharacter == '-'
                || firstCharacter == '@') {
            return "'" + value;
        }

        return value;
    }

    private String escape(
            String value
    ) {
        boolean requiresQuotes =
                value.indexOf(',') >= 0
                        || value.indexOf('"') >= 0
                        || value.indexOf('\r') >= 0
                        || value.indexOf('\n') >= 0;

        if (!requiresQuotes) {
            return value;
        }

        return '"'
                + value.replace("\"", "\"\"")
                + '"';
    }
}