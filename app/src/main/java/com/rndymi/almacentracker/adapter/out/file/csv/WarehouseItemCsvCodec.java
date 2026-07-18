package com.rndymi.almacentracker.adapter.out.file.csv;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public final class WarehouseItemCsvCodec {

    private static final String HEADER =
            "category,code,site,position,observations";
    private static final String LINE_SEPARATOR = "\r\n";

    private final WarehouseItemCsvMapper mapper;

    public WarehouseItemCsvCodec(WarehouseItemCsvMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper);
    }

    public byte[] encode(List<WarehouseItem> warehouseItems) {
        Objects.requireNonNull(warehouseItems);

        StringBuilder csv = new StringBuilder();
        csv.append(HEADER).append(LINE_SEPARATOR);

        for (WarehouseItem warehouseItem : warehouseItems) {
            appendRow(csv, mapper.toColumns(warehouseItem));
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendRow(
            StringBuilder csv,
            List<String> columns
    ) {
        for (int index = 0; index < columns.size(); index++) {
            if (index > 0) {
                csv.append(',');
            }

            csv.append(escape(protectFormula(columns.get(index))));
        }

        csv.append(LINE_SEPARATOR);
    }

    private String protectFormula(String value) {
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

    private String escape(String value) {
        boolean requiresQuotes =
                value.indexOf(',') >= 0
                        || value.indexOf('"') >= 0
                        || value.indexOf('\r') >= 0
                        || value.indexOf('\n') >= 0;

        if (!requiresQuotes) {
            return value;
        }

        return '"' + value.replace("\"", "\"\"") + '"';
    }
}