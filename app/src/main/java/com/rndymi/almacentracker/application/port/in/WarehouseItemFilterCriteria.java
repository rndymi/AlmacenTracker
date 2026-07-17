package com.rndymi.almacentracker.application.port.in;

import java.util.Objects;

public final class WarehouseItemFilterCriteria {

    private final String query;
    private final String category;
    private final String site;
    private final PositionFilter positionFilter;

    private WarehouseItemFilterCriteria(
            String query,
            String category,
            String site,
            PositionFilter positionFilter
    ) {
        this.query = normalizeRequiredValue(query);
        this.category = normalizeOptionalValue(category);
        this.site = normalizeOptionalValue(site);
        this.positionFilter = Objects.requireNonNull(
                positionFilter
        );
    }

    public static WarehouseItemFilterCriteria empty() {
        return new WarehouseItemFilterCriteria(
                "",
                null,
                null,
                PositionFilter.all()
        );
    }

    public static WarehouseItemFilterCriteria of(
            String query,
            String category,
            String site,
            PositionFilter positionFilter
    ) {
        return new WarehouseItemFilterCriteria(
                query,
                category,
                site,
                positionFilter == null
                        ? PositionFilter.all()
                        : positionFilter
        );
    }

    public WarehouseItemFilterCriteria withQuery(
            String query
    ) {
        return of(
                query,
                category,
                site,
                positionFilter
        );
    }

    public WarehouseItemFilterCriteria withCategory(
            String category
    ) {
        return of(
                query,
                category,
                site,
                positionFilter
        );
    }

    public WarehouseItemFilterCriteria withSite(
            String site
    ) {
        return of(
                query,
                category,
                site,
                positionFilter
        );
    }

    public WarehouseItemFilterCriteria withPositionFilter(
            PositionFilter positionFilter
    ) {
        return of(
                query,
                category,
                site,
                positionFilter
        );
    }

    public WarehouseItemFilterCriteria clearFilters() {
        return of(
                query,
                null,
                null,
                PositionFilter.all()
        );
    }

    public String getQuery() {
        return query;
    }

    public String getCategory() {
        return category;
    }

    public String getSite() {
        return site;
    }

    public PositionFilter getPositionFilter() {
        return positionFilter;
    }

    public boolean hasQuery() {
        return !query.isEmpty();
    }

    public boolean hasActiveFilters() {
        return getActiveFilterCount() > 0;
    }

    public int getActiveFilterCount() {
        int count = 0;

        if (category != null) {
            count++;
        }

        if (site != null) {
            count++;
        }

        if (positionFilter.isActive()) {
            count++;
        }

        return count;
    }

    private static String normalizeRequiredValue(
            String value
    ) {
        return value == null
                ? ""
                : value.trim();
    }

    private static String normalizeOptionalValue(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalizedValue = value.trim();

        return normalizedValue.isEmpty()
                ? null
                : normalizedValue;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof WarehouseItemFilterCriteria)) {
            return false;
        }

        WarehouseItemFilterCriteria other =
                (WarehouseItemFilterCriteria) object;

        return query.equals(other.query)
                && Objects.equals(category, other.category)
                && Objects.equals(site, other.site)
                && positionFilter.equals(
                other.positionFilter
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                query,
                category,
                site,
                positionFilter
        );
    }
}
