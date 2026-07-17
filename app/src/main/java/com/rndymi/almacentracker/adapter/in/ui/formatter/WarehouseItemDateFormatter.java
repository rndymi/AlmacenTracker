package com.rndymi.almacentracker.adapter.in.ui.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class WarehouseItemDateFormatter {

    private static final String DATE_PATTERN =
            "dd/MM/yyyy HH:mm";

    public String format(long timestamp) {
        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        DATE_PATTERN,
                        Locale.getDefault()
                );

        return formatter.format(new Date(timestamp));
    }
}