package com.rndymi.almacentracker.adapter.out.file.backup.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.application.result.WarehouseBackupCsvRow;
import com.rndymi.almacentracker.application.result.WarehouseBackupReadResult;
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

    @Test
    public void decode_validBackup_returnsRows() {
        WarehouseBackupReadResult result = decode(
                backupHeader()
                        + "1,MR,1050,A1,Nivel 2,Correcto,1000,2000\r\n"
                        + "1,MD,2050,B1,,,3000,4000\r\n"
        );

        assertEquals(
                WarehouseBackupReadResult.Status.SUCCESS,
                result.getStatus()
        );
        assertEquals(2, result.getRows().size());

        WarehouseBackupCsvRow firstRow =
                result.getRows().get(0);

        assertEquals(2, firstRow.getRowNumber());
        assertEquals("1", firstRow.getFormatVersion());
        assertEquals("MR", firstRow.getCategory());
        assertEquals("1050", firstRow.getCode());
        assertEquals("A1", firstRow.getSite());
        assertEquals("Nivel 2", firstRow.getPosition());
        assertEquals("Correcto", firstRow.getObservations());
        assertEquals("1000", firstRow.getCreatedAt());
        assertEquals("2000", firstRow.getUpdatedAt());
    }

    @Test
    public void decode_headerOnly_returnsEmptyBackup() {
        WarehouseBackupReadResult result = decode(
                backupHeader()
        );

        assertEquals(
                WarehouseBackupReadResult.Status.SUCCESS,
                result.getStatus()
        );
        assertTrue(result.getRows().isEmpty());
    }

    @Test
    public void decode_exchangeCsv_rejectsFormat() {
        WarehouseBackupReadResult result = decode(
                "category,code,site,position,observations\r\n"
                        + "MR,1050,A1,Nivel 2,Correcto\r\n"
        );

        assertEquals(
                WarehouseBackupReadResult.Status.INVALID_FORMAT,
                result.getStatus()
        );
        assertTrue(result.getRows().isEmpty());
        assertNotNull(result.getCause());
    }

    @Test
    public void decode_versionTwo_rejectsVersion() {
        WarehouseBackupReadResult result = decode(
                backupHeader()
                        + "2,MR,1050,A1,,,1000,2000\r\n"
        );

        assertEquals(
                WarehouseBackupReadResult.Status.INCOMPATIBLE_VERSION,
                result.getStatus()
        );
        assertTrue(result.getRows().isEmpty());
    }

    @Test
    public void decode_mixedVersions_rejectsVersion() {
        WarehouseBackupReadResult result = decode(
                backupHeader()
                        + "1,MR,1050,A1,,,1000,2000\r\n"
                        + "2,MD,2050,B1,,,3000,4000\r\n"
        );

        assertEquals(
                WarehouseBackupReadResult.Status.INCOMPATIBLE_VERSION,
                result.getStatus()
        );
        assertTrue(result.getRows().isEmpty());
    }

    @Test
    public void decode_quotedComma_preservesValue() {
        WarehouseBackupReadResult result = decode(
                backupHeader()
                        + "1,MR,1050,A1,,\"Caja A, revisar\",1000,2000\r\n"
        );

        assertEquals(
                WarehouseBackupReadResult.Status.SUCCESS,
                result.getStatus()
        );
        assertEquals(1, result.getRows().size());
        assertEquals(
                "Caja A, revisar",
                result.getRows().get(0).getObservations()
        );
    }

    @Test
    public void decode_multilineObservation_preservesSingleRecord() {
        WarehouseBackupReadResult result = decode(
                backupHeader()
                        + "1,MR,1050,A1,,\"Primera línea\n"
                        + "Segunda línea\",1000,2000\r\n"
        );

        assertEquals(
                WarehouseBackupReadResult.Status.SUCCESS,
                result.getStatus()
        );
        assertEquals(1, result.getRows().size());
        assertEquals(
                "Primera línea\nSegunda línea",
                result.getRows().get(0).getObservations()
        );
        assertEquals(2, result.getRows().get(0).getRowNumber());
    }

    @Test
    public void decode_unclosedQuote_rejectsFormat() {
        WarehouseBackupReadResult result = decode(
                backupHeader()
                        + "1,MR,1050,A1,,\"Sin cerrar,1000,2000"
        );

        assertEquals(
                WarehouseBackupReadResult.Status.INVALID_FORMAT,
                result.getStatus()
        );
        assertTrue(result.getRows().isEmpty());
        assertNotNull(result.getCause());
    }

    @Test
    public void decode_formulaProtectedValue_restoresOriginalValue() {
        WarehouseBackupReadResult result = decode(
                backupHeader()
                        + "1,MR,'=SUM(A1:A2),A1,,'@command,1000,2000\r\n"
        );

        assertEquals(
                WarehouseBackupReadResult.Status.SUCCESS,
                result.getStatus()
        );
        assertEquals(1, result.getRows().size());
        assertEquals(
                "=SUM(A1:A2)",
                result.getRows().get(0).getCode()
        );
        assertEquals(
                "@command",
                result.getRows().get(0).getObservations()
        );
    }

    private WarehouseBackupReadResult decode(String csv) {
        return codec.decode(
                csv.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String backupHeader() {
        return "format_version,category,code,site,"
                + "position,observations,created_at,updated_at\r\n";
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
