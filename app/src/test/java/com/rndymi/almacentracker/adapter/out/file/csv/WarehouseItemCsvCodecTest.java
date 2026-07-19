package com.rndymi.almacentracker.adapter.out.file.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.application.result.WarehouseItemCsvReadResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

public final class WarehouseItemCsvCodecTest {

    private final WarehouseItemCsvCodec codec =
            new WarehouseItemCsvCodec(
                    new WarehouseItemCsvMapper()
            );

    @Test
    public void encodeWritesOfficialHeaderAndOrderedColumns() {
        WarehouseItem item = new WarehouseItem(
                7L,
                "MR",
                "1050",
                "A1",
                "Nivel 2",
                null,
                10L,
                20L
        );

        String csv = new String(
                codec.encode(Collections.singletonList(item)),
                StandardCharsets.UTF_8
        );

        assertEquals(
                "category,code,site,position,observations\r\n"
                        + "MR,1050,A1,Nivel 2,\r\n",
                csv
        );
        assertFalse(csv.contains("createdAt"));
        assertFalse(csv.contains("updatedAt"));
        assertFalse(csv.contains(",7,"));
    }

    @Test
    public void encodeEscapesCommaQuotesAndLineBreaks() {
        WarehouseItem item = new WarehouseItem(
                1L, "CA", "2000", "C1", null,
                "Frágil, caja \"A\"\nRevisar",
                1L, 1L
        );

        String csv = new String(
                codec.encode(Collections.singletonList(item)),
                StandardCharsets.UTF_8
        );

        assertTrue(
                csv.contains(
                        "\"Frágil, caja \"\"A\"\"\nRevisar\""
                )
        );
    }

    @Test
    public void encodeProtectsSpreadsheetFormulaPrefixes() {
        WarehouseItem item = new WarehouseItem(
                1L, "MR", "=SUM(A1:A2)", "A1", null, null,
                1L, 1L
        );

        String csv = new String(
                codec.encode(Collections.singletonList(item)),
                StandardCharsets.UTF_8
        );

        assertTrue(csv.contains("MR,'=SUM(A1:A2),A1"));
    }

    @Test
    public void decodeReadsOfficialHeaderAndSimpleRow()
            throws WarehouseItemCsvFormatException {
        String csv =
                "category,code,site,position,observations\r\n"
                        + "MR,1050,A1,Nivel 2,\r\n";

        WarehouseItemCsvReadResult result =
                codec.decode(
                        csv.getBytes(StandardCharsets.UTF_8)
                );

        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getInvalidRowCount());
        assertEquals(1, result.getRows().size());
        assertEquals(
                "MR",
                result.getRows().get(0).getCategory()
        );
        assertEquals(
                "Nivel 2",
                result.getRows().get(0).getPosition()
        );
    }

    @Test
    public void decodeReadsQuotedCommaQuotesAndMultilineField()
            throws WarehouseItemCsvFormatException {
        String csv =
                "category,code,site,position,observations\r\n"
                        + "CA,2000,C1,,"
                        + "\"Frágil, caja \"\"A\"\"\nRevisar\"\r\n";

        WarehouseItemCsvReadResult result =
                codec.decode(
                        csv.getBytes(StandardCharsets.UTF_8)
                );

        assertEquals(1, result.getRows().size());
        assertEquals(
                "Frágil, caja \"A\"\nRevisar",
                result.getRows().get(0).getObservations()
        );
    }

    @Test
    public void decodeReversesFormulaProtection()
            throws WarehouseItemCsvFormatException {
        String csv =
                "category,code,site,position,observations\r\n"
                        + "MR,'=SUM(A1:A2),A1,,''Texto\r\n";

        WarehouseItemCsvReadResult result =
                codec.decode(
                        csv.getBytes(StandardCharsets.UTF_8)
                );

        assertEquals(
                "=SUM(A1:A2)",
                result.getRows().get(0).getCode()
        );

        assertEquals(
                "'Texto",
                result.getRows().get(0).getObservations()
        );
    }

    @Test
    public void decodeCountsRowsWithWrongColumnCountAsInvalid()
            throws WarehouseItemCsvFormatException {
        String csv =
                "category,code,site,position,observations\r\n"
                        + "MR,1050,A1\r\n";

        WarehouseItemCsvReadResult result =
                codec.decode(
                        csv.getBytes(StandardCharsets.UTF_8)
                );

        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getInvalidRowCount());
        assertTrue(result.getRows().isEmpty());
    }

    @Test(expected = WarehouseItemCsvFormatException.class)
    public void decodeRejectsUnexpectedHeader()
            throws WarehouseItemCsvFormatException {
        codec.decode(
                "code,category,site,position,observations\r\n"
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test(expected = WarehouseItemCsvFormatException.class)
    public void decodeRejectsUnclosedQuotedField()
            throws WarehouseItemCsvFormatException {
        codec.decode(
                (
                        "category,code,site,position,observations\r\n"
                                + "MR,1050,A1,,\"Unclosed"
                ).getBytes(StandardCharsets.UTF_8)
        );
    }
}