package com.rndymi.almacentracker.adapter.in.ui.state;

import com.rndymi.almacentracker.domain.model.WarehouseItem;
public final class WarehouseItemDetailUiState {
    public enum Status {
        LOADING,
        CONTENT,
        NOT_FOUND,
        INVALID_ID,
        ERROR
    }
    private final Status status;
    private final WarehouseItem warehouseItem;
    private final String errorMessage;

    public WarehouseItemDetailUiState(
            Status status,
            WarehouseItem warehouseItem,
            String errorMessage
    ) {
        this.status = status;
        this.warehouseItem = warehouseItem;
        this.errorMessage = errorMessage;
    }

    public static WarehouseItemDetailUiState loading() {
        return new WarehouseItemDetailUiState(
                Status.LOADING,
                null,
                null
        );
    }

    public static WarehouseItemDetailUiState content(
            WarehouseItem warehouseItem
    ) {
        return new WarehouseItemDetailUiState(
                Status.CONTENT,
                warehouseItem,
                null
        );
    }

    public static WarehouseItemDetailUiState notFound() {
        return new WarehouseItemDetailUiState(
                Status.NOT_FOUND,
                null,
                null
        );
    }

    public static WarehouseItemDetailUiState invalidId() {
        return new WarehouseItemDetailUiState(
                Status.INVALID_ID,
                null,
                null
        );
    }

    public static WarehouseItemDetailUiState error(
            String errorMessage
    ) {
        return new WarehouseItemDetailUiState(
                Status.ERROR,
                null,
                errorMessage
        );
    }

    public Status getStatus() {
        return status;
    }

    public WarehouseItem getWarehouseItem() {
        return warehouseItem;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
