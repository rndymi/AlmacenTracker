package com.rndymi.almacentracker.adapter.out.file.backup.csv;

import com.rndymi.almacentracker.application.result.WarehouseBackupCsvRow;
import com.rndymi.almacentracker.application.result.WarehouseBackupReadResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WarehouseBackupCsvCodec {

    private static final String LINE_SEPARATOR = "\r\n";
    private static final int EXPECTED_COLUMN_COUNT = 8;

    private static final List<String> HEADER =
            Collections.unmodifiableList(
                    Arrays.asList(
                            "format_version",
                            "category",
                            "code",
                            "site",
                            "position",
                            "observations",
                            "created_at",
                            "updated_at"
                    )
            );

    private final WarehouseBackupCsvMapper mapper;

    public WarehouseBackupCsvCodec(
            WarehouseBackupCsvMapper mapper
    ) {
        this.mapper = Objects.requireNonNull(mapper);
    }

    public byte[] encode(
            List<WarehouseItem> warehouseItems
    ) {
        Objects.requireNonNull(
                warehouseItems,
                "Warehouse items cannot be null"
        );

        StringBuilder csv = new StringBuilder();

        appendRow(csv, HEADER);

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

    public WarehouseBackupReadResult decode(byte[] content) {
        if (content == null) {
            return WarehouseBackupReadResult.invalidFormat(
                    new IllegalArgumentException(
                            "Backup content cannot be null"
                    )
            );
        }

        try {
            String csv = new String(
                    content,
                    StandardCharsets.UTF_8
            );

            List<ParsedRecord> records = parseRecords(csv);

            if (records.isEmpty()) {
                return WarehouseBackupReadResult.invalidFormat(
                        new IllegalArgumentException(
                                "Backup header is missing"
                        )
                );
            }

            if (!HEADER.equals(records.get(0).columns)) {
                return WarehouseBackupReadResult.invalidFormat(
                        new IllegalArgumentException(
                                "Unexpected backup CSV header"
                        )
                );
            }

            List<WarehouseBackupCsvRow> rows =
                    new ArrayList<>();

            String detectedVersion = null;

            for (int index = 1;
                 index < records.size();
                 index++) {

                ParsedRecord record = records.get(index);

                if (record.isCompletelyEmpty()) {
                    continue;
                }

                if (record.columns.size()
                        != EXPECTED_COLUMN_COUNT) {
                    return WarehouseBackupReadResult.invalidFormat(
                            new IllegalArgumentException(
                                    "Invalid column count at row "
                                            + record.rowNumber
                            )
                    );
                }

                List<String> columns =
                        unprotectColumns(record.columns);

                String formatVersion = columns.get(0);

                if (!WarehouseBackupCsvMapper
                        .FORMAT_VERSION
                        .equals(formatVersion)) {
                    return WarehouseBackupReadResult
                            .incompatibleVersion();
                }

                if (detectedVersion == null) {
                    detectedVersion = formatVersion;
                } else if (!detectedVersion.equals(
                        formatVersion
                )) {
                    return WarehouseBackupReadResult
                            .incompatibleVersion();
                }

                rows.add(
                        new WarehouseBackupCsvRow(
                                record.rowNumber,
                                formatVersion,
                                columns.get(1),
                                columns.get(2),
                                columns.get(3),
                                columns.get(4),
                                columns.get(5),
                                columns.get(6),
                                columns.get(7)
                        )
                );
            }

            return WarehouseBackupReadResult.success(rows);
        } catch (RuntimeException exception) {
            return WarehouseBackupReadResult.invalidFormat(
                    exception
            );
        }
    }

    private List<ParsedRecord> parseRecords(String csv) {
        List<ParsedRecord> records = new ArrayList<>();
        List<String> currentColumns = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();

        boolean insideQuotes = false;
        boolean quoteClosed = false;
        int currentRowNumber = 1;
        int recordStartRow = 1;

        for (int index = 0; index < csv.length(); index++) {
            char character = csv.charAt(index);

            if (insideQuotes) {
                if (character == '"') {
                    if (index + 1 < csv.length()
                            && csv.charAt(index + 1) == '"') {
                        currentField.append('"');
                        index++;
                    } else {
                        insideQuotes = false;
                        quoteClosed = true;
                    }
                } else {
                    currentField.append(character);

                    if (character == '\n') {
                        currentRowNumber++;
                    }
                }

                continue;
            }

            if (quoteClosed) {
                if (character == ',') {
                    currentColumns.add(
                            currentField.toString()
                    );
                    currentField.setLength(0);
                    quoteClosed = false;
                    continue;
                }

                if (character == '\r'
                        || character == '\n') {
                    currentColumns.add(
                            currentField.toString()
                    );

                    records.add(
                            new ParsedRecord(
                                    recordStartRow,
                                    currentColumns
                            )
                    );

                    currentColumns = new ArrayList<>();
                    currentField.setLength(0);
                    quoteClosed = false;

                    if (character == '\r'
                            && index + 1 < csv.length()
                            && csv.charAt(index + 1) == '\n') {
                        index++;
                    }

                    currentRowNumber++;
                    recordStartRow = currentRowNumber;
                    continue;
                }

                throw new IllegalArgumentException(
                        "Unexpected character after closing quote"
                );
            }

            if (character == '"') {
                if (currentField.length() > 0) {
                    throw new IllegalArgumentException(
                            "Unexpected quote inside unquoted field"
                    );
                }

                insideQuotes = true;
                continue;
            }

            if (character == ',') {
                currentColumns.add(
                        currentField.toString()
                );
                currentField.setLength(0);
                continue;
            }

            if (character == '\r'
                    || character == '\n') {
                currentColumns.add(
                        currentField.toString()
                );

                records.add(
                        new ParsedRecord(
                                recordStartRow,
                                currentColumns
                        )
                );

                currentColumns = new ArrayList<>();
                currentField.setLength(0);

                if (character == '\r'
                        && index + 1 < csv.length()
                        && csv.charAt(index + 1) == '\n') {
                    index++;
                }

                currentRowNumber++;
                recordStartRow = currentRowNumber;
                continue;
            }

            currentField.append(character);
        }

        if (insideQuotes) {
            throw new IllegalArgumentException(
                    "Unclosed quoted field"
            );
        }

        if (quoteClosed
                || currentField.length() > 0
                || !currentColumns.isEmpty()) {
            currentColumns.add(currentField.toString());

            records.add(
                    new ParsedRecord(
                            recordStartRow,
                            currentColumns
                    )
            );
        }

        return records;
    }

    private List<String> unprotectColumns(
            List<String> columns
    ) {
        List<String> values =
                new ArrayList<>(columns.size());

        for (String column : columns) {
            values.add(unprotectFormula(column));
        }

        return values;
    }

    private String unprotectFormula(String value) {
        if (value == null || value.length() < 2) {
            return value == null ? "" : value;
        }

        if (value.charAt(0) != '\'') {
            return value;
        }

        char secondCharacter = value.charAt(1);

        if (secondCharacter == '\''
                || secondCharacter == '='
                || secondCharacter == '+'
                || secondCharacter == '-'
                || secondCharacter == '@') {
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

    private String protectFormula(String value) {
        String safeValue = value == null ? "" : value;

        if (safeValue.isEmpty()) {
            return safeValue;
        }

        char firstCharacter = safeValue.charAt(0);

        if (firstCharacter == '\''
                || firstCharacter == '='
                || firstCharacter == '+'
                || firstCharacter == '-'
                || firstCharacter == '@') {
            return "'" + safeValue;
        }

        return safeValue;
    }

    private String escape(String value) {
        boolean requiresQuotes =
                value.indexOf(',') >= 0
                        || value.indexOf('"') >= 0
                        || value.indexOf('\r') >= 0
                        || value.indexOf('\n') >= 0;

        if (!requiresQuotes) {
            return value;
        }

        return "\""
                + value.replace("\"", "\"\"")
                + "\"";
    }

    private static final class ParsedRecord {

        private final int rowNumber;
        private final List<String> columns;

        private ParsedRecord(
                int rowNumber,
                List<String> columns
        ) {
            this.rowNumber = rowNumber;
            this.columns = Collections.unmodifiableList(
                    new ArrayList<>(columns)
            );
        }

        private boolean isCompletelyEmpty() {
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