package com.rndymi.almacentracker.core.scanner;

import java.util.Objects;

public final class ScannedCode {

    private final String value;
    private final ScannedCodeFormat format;

    public ScannedCode(
            String value,
            ScannedCodeFormat format
    ) {
        this.value = Objects.requireNonNull(value);
        this.format = Objects.requireNonNull(format);
    }

    public String getValue() {
        return value;
    }

    public ScannedCodeFormat getFormat() {
        return format;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ScannedCode)) {
            return false;
        }

        ScannedCode that = (ScannedCode) other;

        return value.equals(that.value)
                && format == that.format;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, format);
    }
}