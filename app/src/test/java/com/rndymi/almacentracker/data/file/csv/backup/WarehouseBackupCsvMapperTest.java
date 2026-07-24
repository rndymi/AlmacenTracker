package com.rndymi.almacentracker.data.file.csv.backup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public final class WarehouseBackupCsvMapperTest {

    private final WarehouseBackupCsvMapper mapper =
            new WarehouseBackupCsvMapper();

    @Test
    public void mapsBackupColumnsInContractOrder() {
        WarehouseItem item = item(
                "MR",
                "1050",
                "A1",
                "Nivel 2",
                "Revisado",
                1000L,
                2000L
        );

        List<String> columns = mapper.toColumns(item);

        assertEquals(
                Arrays.asList(
                        WarehouseBackupCsvMapper.FORMAT_VERSION,
                        "MR",
                        "1050",
                        "A1",
                        "Nivel 2",
                        "Revisado",
                        "1000",
                        "2000"
                ),
                columns
        );
    }

    @Test
    public void backupFormatIncludesVersionAndDates() {
        List<String> columns = mapper.toColumns(
                item(
                        "MR",
                        "1050",
                        "A1",
                        null,
                        null,
                        1000L,
                        2000L
                )
        );

        assertEquals(8, columns.size());
        assertEquals("1", columns.get(0));
        assertEquals("1000", columns.get(6));
        assertEquals("2000", columns.get(7));
    }

    @Test
    public void nullOptionalsBecomeEmptyColumns() {
        List<String> columns = mapper.toColumns(
                item(
                        "MR",
                        "1050",
                        "A1",
                        null,
                        null,
                        1000L,
                        2000L
                )
        );

        assertEquals("", columns.get(4));
        assertEquals("", columns.get(5));
    }

    @Test
    public void rejectsBlankRequiredValue() {
        WarehouseItem item = item(
                "   ",
                "1050",
                "A1",
                null,
                null,
                1000L,
                2000L
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toColumns(item)
        );
    }

    @Test
    public void rejectsInvalidDateRange() {
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
                () -> mapper.toColumns(item)
        );
    }

    @Test
    public void rejectsNullDomainItem() {
        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toColumns(null)
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
                99L,
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
