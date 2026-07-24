package com.rndymi.almacentracker.data.file.csv.backup;

import com.rndymi.almacentracker.core.csv.backup.WarehouseBackupCsvRow;
import com.rndymi.almacentracker.core.csv.backup.WarehouseBackupReadResult;
import com.rndymi.almacentracker.core.csv.CsvFormulaProtector;
import com.rndymi.almacentracker.core.csv.CsvRecordCodec;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WarehouseBackupCsvCodec {

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

        CsvRecordCodec.appendRecord(
                csv,
                CsvFormulaProtector.protectAll(HEADER)
        );

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

            List<CsvRecordCodec.Record> records =
                    CsvRecordCodec.parse(csv);

            if (records.isEmpty()) {
                return WarehouseBackupReadResult.invalidFormat(
                        new IllegalArgumentException(
                                "Backup header is missing"
                        )
                );
            }

            if (!HEADER.equals(
                    records.get(0).getColumns()
            )) {
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

                CsvRecordCodec.Record record =
                        records.get(index);

                if (record.isCompletelyEmpty()) {
                    continue;
                }

                if (record.getColumns().size()
                        != EXPECTED_COLUMN_COUNT) {
                    return WarehouseBackupReadResult.invalidFormat(
                            new IllegalArgumentException(
                                    "Invalid column count at row "
                                            + record.getRowNumber()
                            )
                    );
                }

                List<String> columns =
                        CsvFormulaProtector.unprotectAll(
                                record.getColumns()
                        );

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
                                record.getRowNumber(),
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

}
