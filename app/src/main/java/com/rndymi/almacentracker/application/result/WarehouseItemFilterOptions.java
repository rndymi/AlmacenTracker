package com.rndymi.almacentracker.application.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WarehouseItemFilterOptions {

    private final List<String> categories;
    private final List<String> sites;
    private final List<String> positions;
    private final boolean hasItemsWithoutPosition;

    public WarehouseItemFilterOptions(
            List<String> categories,
            List<String> sites,
            List<String> positions,
            boolean hasItemsWithoutPosition
    ) {
        this.categories = immutableCopy(categories);
        this.sites = immutableCopy(sites);
        this.positions = immutableCopy(positions);
        this.hasItemsWithoutPosition =
                hasItemsWithoutPosition;
    }

    public static WarehouseItemFilterOptions empty() {
        return new WarehouseItemFilterOptions(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                false
        );
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<String> getSites() {
        return sites;
    }

    public List<String> getPositions() {
        return positions;
    }

    public boolean hasItemsWithoutPosition() {
        return hasItemsWithoutPosition;
    }

    private static List<String> immutableCopy(
            List<String> values
    ) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(
                new ArrayList<>(values)
        );
    }
}
