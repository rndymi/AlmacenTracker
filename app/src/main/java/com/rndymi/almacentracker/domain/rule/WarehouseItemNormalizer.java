package com.rndymi.almacentracker.domain.rule;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WarehouseItemNormalizer {

    private static final Pattern CODE_WITH_TEXT_SUFFIX =
            Pattern.compile(
                    "^([0-9]{4,5})\\s*(\\p{L}.*)$"
            );

    private static final Pattern WHITESPACE =
            Pattern.compile("\\s+");

    public String normalizeCategory(String value) {
        return normalizeRequired(value);
    }

    public String normalizeCode(String value) {
        String normalized =
                normalizeRequired(value);

        Matcher matcher =
                CODE_WITH_TEXT_SUFFIX.matcher(
                        normalized
                );

        if (!matcher.matches()) {
            return normalized;
        }

        String suffix =
                WHITESPACE.matcher(
                        matcher.group(2).trim()
                ).replaceAll(" ");

        return matcher.group(1)
                + " "
                + suffix;
    }

    public String normalizeSite(String value) {
        return normalizeRequired(value);
    }

    public String normalizeOptional(String value) {
        String normalized = trim(value);

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private String normalizeRequired(String value) {
        return trim(value).toUpperCase(Locale.ROOT);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
