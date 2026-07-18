package com.rndymi.almacentracker.adapter.out.file.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
}