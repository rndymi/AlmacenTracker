package com.rndymi.almacentracker.adapter.in.ui.state;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Collections;
import java.util.List;

public final class WarehouseItemListUiState {
    public enum Status {
        LOADING,
        CONTENT,
        EMPTY,
        ERROR
    }

    private final Status status;
    private final List<WarehouseItem> items;
    private final String errorMessage;

    private WarehouseItemListUiState(
            Status status,
            List<WarehouseItem> items,
            String errorMessage
    ) {
        this.status = status;
        this.items = items == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(items);
        this.errorMessage = errorMessage;
    }

    public static WarehouseItemListUiState loading() {
        return new WarehouseItemListUiState(
                Status.LOADING,
                Collections.emptyList(),
                null
        );
    }

    public static WarehouseItemListUiState content(
            List<WarehouseItem> items
    ) {
        return new WarehouseItemListUiState(
                Status.CONTENT,
                items,
                null
        );
    }

    public static WarehouseItemListUiState empty() {
        return new WarehouseItemListUiState(
                Status.EMPTY,
                Collections.emptyList(),
                null
        );
    }

    public static WarehouseItemListUiState error(
            String errorMessage
    ) {
        return new WarehouseItemListUiState(
                Status.ERROR,
                Collections.emptyList(),
                errorMessage
        );
    }

    public Status getStatus() {
        return status;
    }

    public List<WarehouseItem> getItems() {
        return items;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}