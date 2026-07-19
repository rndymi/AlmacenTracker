package com.rndymi.almacentracker.adapter.out.file.backup.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

public final class WarehouseBackupCsvCodecTest {

    private WarehouseBackupCsvCodec codec;

    @Before
    public void setUp() {
        codec = new WarehouseBackupCsvCodec(
                new WarehouseBackupCsvMapper()
        );
    }

    @Test
    public void encodeEmptyListWritesValidHeader() {
        String csv = encode(
                Collections.emptyList()
        );

        assertEquals(
                "format_version,category,code,site,"
                        + "position,observations,"
                        + "created_at,updated_at\r\n",
                csv
        );
    }

    @Test
    public void encodeWritesVersionAndDates() {
        WarehouseItem item = item(
                "MR",
                "1050",
                "A1",
                "Nivel 2",
                "Correcto",
                1_721_304_000_000L,
                1_721_308_000_000L
        );

        String csv = encode(
                Collections.singletonList(item)
        );

        assertTrue(
                csv.contains(
                        "1,MR,1050,A1,Nivel 2,Correcto,"
                                + "1721304000000,"
                                + "1721308000000\r\n"
                )
        );
    }

    @Test
    public void encodeRepresentsNullOptionalsAsEmpty() {
        WarehouseItem item = item(
                "MR",
                "1050",
                "A1",
                null,
                null,
                1000L,
                1000L
        );

        String csv = encode(
                Collections.singletonList(item)
        );

        assertTrue(
                csv.contains(
                        "1,MR,1050,A1,,,1000,1000\r\n"
                )
        );
    }

    @Test
    public void encodeEscapesCommaQuotesAndNewLine() {
        WarehouseItem item = item(
                "MR",
                "1050",
                "A1",
                null,
                "Caja \"A\", revisar\nsegunda línea",
                1000L,
                2000L
        );

        String csv = encode(
                Collections.singletonList(item)
        );

        assertTrue(
                csv.contains(
                        "\"Caja \"\"A\"\", revisar\n"
                                + "segunda línea\""
                )
        );
    }

    @Test
    public void encodeProtectsFormulaPrefix() {
        WarehouseItem item = item(
                "MR",
                "=SUM(A1:A2)",
                "A1",
                null,
                "@command",
                1000L,
                2000L
        );

        String csv = encode(
                Collections.singletonList(item)
        );

        assertTrue(csv.contains("'=SUM(A1:A2)"));
        assertTrue(csv.contains("'@command"));
    }

    @Test
    public void encodeProtectsOriginalApostrophe() {
        WarehouseItem item = item(
                "MR",
                "'=SUM(A1:A2)",
                "A1",
                null,
                null,
                1000L,
                2000L
        );

        String csv = encode(
                Collections.singletonList(item)
        );

        assertTrue(csv.contains("''=SUM(A1:A2)"));
    }

    @Test
    public void encodePreservesUnicodeAsUtf8() {
        WarehouseItem item = item(
                "MR",
                "Ñ-1050",
                "ÁREA-1",
                null,
                "Mercancía frágil",
                1000L,
                2000L
        );

        String csv = encode(
                Collections.singletonList(item)
        );

        assertTrue(csv.contains("Ñ-1050"));
        assertTrue(csv.contains("ÁREA-1"));
        assertTrue(csv.contains("Mercancía frágil"));
    }

    @Test
    public void encodeRejectsNonPositiveCreatedAt() {
        WarehouseItem item = item(
                "MR",
                "1050",
                "A1",
                null,
                null,
                0L,
                1000L
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> codec.encode(
                        Collections.singletonList(item)
                )
        );
    }

    @Test
    public void encodeRejectsNonPositiveUpdatedAt() {
        WarehouseItem item = item(
                "MR",
                "1050",
                "A1",
                null,
                null,
                1000L,
                0L
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> codec.encode(
                        Collections.singletonList(item)
                )
        );
    }

    @Test
    public void encodeRejectsUpdatedAtBeforeCreatedAt() {
        WarehouseItem item = item(
                "MR",
                "1050",
                "A1",
                null,
                null,
                2000L,
                1000L
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> codec.encode(
                        Collections.singletonList(item)
                )
        );
    }

    @Test
    public void encodeDoesNotIncludeInternalId() {
        WarehouseItem item =
                new WarehouseItem(
                        987654321L,
                        "MR",
                        "1050",
                        "A1",
                        null,
                        null,
                        1000L,
                        2000L
                );

        String csv = encode(
                Collections.singletonList(item)
        );

        assertFalse(csv.contains("987654321"));
    }

    @Test
    public void encodeUsesCrLfTerminators() {
        WarehouseItem item = item(
                "MR",
                "1050",
                "A1",
                null,
                null,
                1000L,
                2000L
        );

        String csv = encode(
                Collections.singletonList(item)
        );

        assertTrue(csv.endsWith("\r\n"));
        assertFalse(
                csv.replace("\r\n", "")
                        .contains("\n")
        );
    }

    private String encode(
            java.util.List<WarehouseItem> items
    ) {
        return new String(
                codec.encode(items),
                StandardCharsets.UTF_8
        );
    }

    private WarehouseItem item(
            String category,
            String code,
            String site,
            String position,
            String observations,
            long createdAt,
            long updatedAt
    ) {
        return new WarehouseItem(
                7L,
                category,
                code,
                site,
                position,
                observations,
                createdAt,
                updatedAt
        );
    }
}