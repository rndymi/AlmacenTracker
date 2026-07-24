package com.rndymi.almacentracker.data.file.csv.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public final class WarehouseItemCsvMapperTest {

    private final WarehouseItemCsvMapper mapper =
            new WarehouseItemCsvMapper();

    @Test
    public void mapsExchangeColumnsInContractOrder() {
        WarehouseItem item = item(
                "MR",
                "1050",
                "A1",
                "Nivel 2",
                "Revisado"
        );

        List<String> columns = mapper.toColumns(item);

        assertEquals(
                Arrays.asList(
                        "MR",
                        "1050",
                        "A1",
                        "Nivel 2",
                        "Revisado"
                ),
                columns
        );
    }

    @Test
    public void exchangeFormatOmitsInternalIdAndDates() {
        List<String> columns = mapper.toColumns(
                item("MR", "1050", "A1", null, null)
        );

        assertEquals(5, columns.size());
    }

    @Test
    public void nullOptionalsBecomeEmptyColumns() {
        List<String> columns = mapper.toColumns(
                item("MR", "1050", "A1", null, null)
        );

        assertEquals("", columns.get(3));
        assertEquals("", columns.get(4));
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
            String observations
    ) {
        return new WarehouseItem(
                99L,
                category,
                code,
                site,
                position,
                observations,
                1000L,
                2000L
        );
    }
}
