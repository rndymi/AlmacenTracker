package com.rndymi.almacentracker.data.file.csv.exchange;

import com.rndymi.almacentracker.application.result.ImportWarehouseItemIssue;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvReadResult;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvRow;
import com.rndymi.almacentracker.core.csv.CsvFormulaProtector;
import com.rndymi.almacentracker.core.csv.CsvRecordCodec;
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
        csv.append(HEADER)
                .append(CsvRecordCodec.LINE_SEPARATOR);

        for (WarehouseItem warehouseItem : warehouseItems) {
            CsvRecordCodec.appendRecord(
                    csv,
                    CsvFormulaProtector.protectAll(
                            mapper.toColumns(warehouseItem)
                    )
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

        final List<CsvRecordCodec.Record> records;

        try {
            records = CsvRecordCodec.parse(normalizedCsv);
        } catch (IllegalArgumentException exception) {
            throw new WarehouseItemCsvFormatException(
                    exception.getMessage()
            );
        }

        if (records.isEmpty()) {
            return emptyReadResult();
        }

        validateHeader(
                records.get(0).getColumns()
        );

        List<WarehouseItemCsvRow> rows =
                new ArrayList<>();

        List<ImportWarehouseItemIssue> issues =
                new ArrayList<>();

        int totalRows = 0;

        for (int index = 1;
             index < records.size();
             index++) {

            CsvRecordCodec.Record record =
                    records.get(index);

            if (record.isCompletelyEmpty()) {
                continue;
            }

            totalRows++;

            if (record.getColumns().size()
                    != EXPECTED_COLUMN_COUNT) {

                issues.add(
                        ImportWarehouseItemIssue
                                .invalidColumnCount(
                                        record.getRowNumber(),
                                        record.getColumns().size(),
                                        EXPECTED_COLUMN_COUNT
                                )
                );

                continue;
            }

            rows.add(
                    new WarehouseItemCsvRow(
                            record.getRowNumber(),
                            CsvFormulaProtector.unprotect(
                                    record.getColumns().get(0)
                            ),
                            CsvFormulaProtector.unprotect(
                                    record.getColumns().get(1)
                            ),
                            CsvFormulaProtector.unprotect(
                                    record.getColumns().get(2)
                            ),
                            CsvFormulaProtector.unprotect(
                                    record.getColumns().get(3)
                            ),
                            CsvFormulaProtector.unprotect(
                                    record.getColumns().get(4)
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

    private void validateHeader(
            List<String> header
    ) throws WarehouseItemCsvFormatException {
        if (!HEADER_COLUMNS.equals(header)) {
            throw new WarehouseItemCsvFormatException(
                    "Unexpected CSV header"
            );
        }
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

}
