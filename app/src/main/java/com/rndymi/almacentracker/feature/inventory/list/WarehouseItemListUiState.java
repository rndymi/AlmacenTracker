package com.rndymi.almacentracker.feature.inventory.list;

import com.rndymi.almacentracker.data.repository.PositionFilter;
import com.rndymi.almacentracker.data.repository.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.data.repository.WarehouseItemFilterOptions;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WarehouseItemListUiState {

    public enum Status {
        LOADING,
        CONTENT,
        EMPTY_DATABASE,
        NO_RESULTS,
        ERROR
    }

    private final Status status;
    private final List<WarehouseItem> items;
    private final WarehouseItemFilterCriteria criteria;
    private final WarehouseItemFilterOptions filterOptions;
    private final String errorMessage;

    private WarehouseItemListUiState(
            Status status,
            List<WarehouseItem> items,
            WarehouseItemFilterCriteria criteria,
            WarehouseItemFilterOptions filterOptions,
            String errorMessage
    ) {
        this.status = Objects.requireNonNull(status);

        this.items = items == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(
                new ArrayList<>(items)
        );

        this.criteria = criteria == null
                ? WarehouseItemFilterCriteria.empty()
                : criteria;

        this.filterOptions = filterOptions == null
                ? WarehouseItemFilterOptions.empty()
                : filterOptions;

        this.errorMessage = errorMessage;

        if (status == Status.CONTENT
                && this.items.isEmpty()) {
            throw new IllegalArgumentException(
                    "CONTENT requires at least one item"
            );
        }

        if (status == Status.NO_RESULTS
                && !this.criteria.hasQuery()
                && !this.criteria.hasActiveFilters()) {
            throw new IllegalArgumentException(
                    "NO_RESULTS requires search or filters"
            );
        }

        if (status == Status.ERROR
                && (errorMessage == null
                || errorMessage.trim().isEmpty())) {
            throw new IllegalArgumentException(
                    "ERROR requires a message"
            );
        }

        if (status != Status.ERROR
                && errorMessage != null) {
            throw new IllegalArgumentException(
                    status + " cannot contain an error message"
            );
        }
    }

    public static WarehouseItemListUiState loading(
            WarehouseItemFilterCriteria criteria,
            WarehouseItemFilterOptions options
    ) {
        return new WarehouseItemListUiState(
                Status.LOADING,
                Collections.emptyList(),
                criteria,
                options,
                null
        );
    }

    public static WarehouseItemListUiState content(
            List<WarehouseItem> items,
            WarehouseItemFilterCriteria criteria,
            WarehouseItemFilterOptions options
    ) {
        return new WarehouseItemListUiState(
                Status.CONTENT,
                items,
                criteria,
                options,
                null
        );
    }

    public static WarehouseItemListUiState emptyDatabase(
            WarehouseItemFilterCriteria criteria,
            WarehouseItemFilterOptions options
    ) {
        return new WarehouseItemListUiState(
                Status.EMPTY_DATABASE,
                Collections.emptyList(),
                criteria,
                options,
                null
        );
    }

    public static WarehouseItemListUiState noResults(
            WarehouseItemFilterCriteria criteria,
            WarehouseItemFilterOptions options
    ) {
        return new WarehouseItemListUiState(
                Status.NO_RESULTS,
                Collections.emptyList(),
                criteria,
                options,
                null
        );
    }

    public static WarehouseItemListUiState error(
            WarehouseItemFilterCriteria criteria,
            WarehouseItemFilterOptions options,
            String errorMessage
    ) {
        return new WarehouseItemListUiState(
                Status.ERROR,
                Collections.emptyList(),
                criteria,
                options,
                errorMessage
        );
    }

    public Status getStatus() {
        return status;
    }

    public List<WarehouseItem> getItems() {
        return items;
    }

    public WarehouseItemFilterCriteria getCriteria() {
        return criteria;
    }

    public WarehouseItemFilterOptions getFilterOptions() {
        return filterOptions;
    }

    public String getQuery() {
        return criteria.getQuery();
    }

    public String getSelectedCategory() {
        return criteria.getCategory();
    }

    public String getSelectedSite() {
        return criteria.getSite();
    }

    public PositionFilter getSelectedPositionFilter() {
        return criteria.getPositionFilter();
    }

    public int getActiveFilterCount() {
        return criteria.getActiveFilterCount();
    }

    public boolean hasSearchQuery() {
        return criteria.hasQuery();
    }

    public boolean hasActiveFilters() {
        return criteria.hasActiveFilters();
    }

    public NoResultsReason getNoResultsReason() {
        if (status != Status.NO_RESULTS) {
            throw new IllegalStateException(
                    "No-results reason is only available for NO_RESULTS"
            );
        }

        if (hasSearchQuery() && hasActiveFilters()) {
            return NoResultsReason.SEARCH_AND_FILTERS;
        }

        if (hasActiveFilters()) {
            return NoResultsReason.FILTERS;
        }

        return NoResultsReason.SEARCH;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
