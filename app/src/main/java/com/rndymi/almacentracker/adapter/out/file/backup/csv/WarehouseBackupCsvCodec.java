package com.rndymi.almacentracker.adapter.out.file.backup.csv;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class WarehouseBackupCsvCodec {

    private static final String LINE_SEPARATOR = "\r\n";

    private static final List<String> HEADER =
            Arrays.asList(
                    "format_version",
                    "category",
                    "code",
                    "site",
                    "position",
                    "observations",
                    "created_at",
                    "updated_at"
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

        for (WarehouseItem warehouseItem
                : warehouseItems) {
            appendRow(
                    csv,
                    mapper.toColumns(warehouseItem)
            );
        }

        return csv.toString().getBytes(
                StandardCharsets.UTF_8
        );
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
}