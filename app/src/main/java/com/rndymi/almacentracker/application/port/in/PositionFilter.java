package com.rndymi.almacentracker.application.port.in;

import java.util.Objects;

public class PositionFilter {
    public enum Type {
        ALL,
        WITHOUT_POSITION,
        EXACT_VALUE
    }

    private static final PositionFilter ALL_FILTER =
            new PositionFilter(Type.ALL, null);

    private static final PositionFilter WITHOUT_POSITION_FILTER =
            new PositionFilter(Type.WITHOUT_POSITION, null);

    private final Type type;
    private final String value;

    private PositionFilter(Type type, String value) {
        this.type = Objects.requireNonNull(type);
        this.value = value;
    }

    public static PositionFilter all() {
        return ALL_FILTER;
    }

    public static PositionFilter withoutPosition() {
        return WITHOUT_POSITION_FILTER;
    }

    public static PositionFilter exact(String value) {
        String normalizedValue = value == null
                ? ""
                : value.trim();

        if (normalizedValue.isEmpty()) {
            throw new IllegalArgumentException(
                    "Exact position value cannot be empty"
            );
        }

        return new PositionFilter(
                Type.EXACT_VALUE,
                normalizedValue
        );
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean isActive() {
        return type != Type.ALL;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof PositionFilter)) {
            return false;
        }

        PositionFilter other = (PositionFilter) object;

        return type == other.type
                && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
