package com.rndymi.almacentracker.domain.rule;

import java.util.Objects;

public final class WarehouseItemIdentity {

    private static final WarehouseItemNormalizer NORMALIZER =
            new WarehouseItemNormalizer();

    private final String category;
    private final String code;

    public WarehouseItemIdentity(
            String category,
            String code
    ) {
        this.category =
                NORMALIZER.normalizeCategory(category);
        this.code = NORMALIZER.normalizeCode(code);
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof WarehouseItemIdentity)) {
            return false;
        }

        WarehouseItemIdentity identity =
                (WarehouseItemIdentity) other;

        return category.equals(identity.category)
                && code.equals(identity.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, code);
    }
}
