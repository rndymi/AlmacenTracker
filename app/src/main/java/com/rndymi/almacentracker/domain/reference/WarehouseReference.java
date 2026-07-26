package com.rndymi.almacentracker.domain.reference;

import java.util.Objects;

public final class WarehouseReference {

    private static final String IDENTITY_SEPARATOR =
            "\u0000";

    private final String category;
    private final String code;

    public WarehouseReference(
            String category,
            String code
    ) {
        this.category =
                Objects.requireNonNull(
                        category,
                        "category"
                );

        this.code =
                Objects.requireNonNull(
                        code,
                        "code"
                );
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String identityKey() {
        return category
                + IDENTITY_SEPARATOR
                + code;
    }

    public String displayValue() {
        return category + " " + code;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof WarehouseReference)) {
            return false;
        }

        WarehouseReference that =
                (WarehouseReference) other;

        return category.equals(that.category)
                && code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                category,
                code
        );
    }
}