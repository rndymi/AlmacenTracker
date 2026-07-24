package com.rndymi.almacentracker.domain.rule;

import java.util.Locale;

public final class WarehouseItemNormalizer {

    public String normalizeCategory(String value) {
        return normalizeRequired(value);
    }

    public String normalizeCode(String value) {
        return normalizeRequired(value);
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
