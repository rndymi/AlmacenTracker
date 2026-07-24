package com.rndymi.almacentracker.core.csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CsvFormulaProtector {

    private CsvFormulaProtector() {
    }

    public static String protect(String value) {
        String safeValue = value == null ? "" : value;

        if (safeValue.isEmpty()) {
            return safeValue;
        }

        char firstCharacter = safeValue.charAt(0);

        if (firstCharacter == '\''
                || isFormulaPrefix(firstCharacter)) {
            return "'" + safeValue;
        }

        return safeValue;
    }

    public static String unprotect(String value) {
        if (value == null || value.length() < 2) {
            return value == null ? "" : value;
        }

        if (value.charAt(0) != '\'') {
            return value;
        }

        char secondCharacter = value.charAt(1);

        if (secondCharacter == '\''
                || isFormulaPrefix(secondCharacter)) {
            return value.substring(1);
        }

        return value;
    }

    public static List<String> protectAll(
            List<String> values
    ) {
        return transform(values, true);
    }

    public static List<String> unprotectAll(
            List<String> values
    ) {
        return transform(values, false);
    }

    private static List<String> transform(
            List<String> values,
            boolean protecting
    ) {
        Objects.requireNonNull(values);

        List<String> transformed =
                new ArrayList<>(values.size());

        for (String value : values) {
            transformed.add(
                    protecting
                            ? protect(value)
                            : unprotect(value)
            );
        }

        return Collections.unmodifiableList(transformed);
    }

    private static boolean isFormulaPrefix(char value) {
        return value == '='
                || value == '+'
                || value == '-'
                || value == '@';
    }
}
