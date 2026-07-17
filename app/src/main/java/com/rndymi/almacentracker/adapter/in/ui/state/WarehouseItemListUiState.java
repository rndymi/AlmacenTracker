package com.rndymi.almacentracker.adapter.in.ui.state;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Collections;
import java.util.List;

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
    private final String query;
    private final String errorMessage;

    private WarehouseItemListUiState(
            Status status,
            List<WarehouseItem> items,
            String query,
            String errorMessage
    ) {
        this.status = status;
        this.items = items == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(items);
        this.query = query == null ? "" : query;
        this.errorMessage = errorMessage;
    }

    public static WarehouseItemListUiState loading(
            String query
    ) {
        return new WarehouseItemListUiState(
                Status.LOADING,
                Collections.emptyList(),
                query,
                null
        );
    }

    public static WarehouseItemListUiState content(
            List<WarehouseItem> items,
            String query
    ) {
        return new WarehouseItemListUiState(
                Status.CONTENT,
                items,
                query,
                null
        );
    }

    public static WarehouseItemListUiState emptyDatabase() {
        return new WarehouseItemListUiState(
                Status.EMPTY_DATABASE,
                Collections.emptyList(),
                "",
                null
        );
    }

    public static WarehouseItemListUiState noResults(
            String query
    ) {
        return new WarehouseItemListUiState(
                Status.NO_RESULTS,
                Collections.emptyList(),
                query,
                null
        );
    }

    public static WarehouseItemListUiState error(
            String query,
            String errorMessage
    ) {
        return new WarehouseItemListUiState(
                Status.ERROR,
                Collections.emptyList(),
                query,
                errorMessage
        );
    }

    public Status getStatus() {
        return status;
    }

    public List<WarehouseItem> getItems() {
        return items;
    }

    public String getQuery() {
        return query;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}