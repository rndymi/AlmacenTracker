package com.rndymi.almacentracker.adapter.in.ui.formatter;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class WarehouseItemDateFormatterTest {

    @Test
    public void format_returnsReadableDate() {
        WarehouseItemDateFormatter formatter =
                new WarehouseItemDateFormatter();

        String result = formatter.format(0L);

        assertTrue(
                result.matches(
                        "\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}"
                )
        );
    }
}