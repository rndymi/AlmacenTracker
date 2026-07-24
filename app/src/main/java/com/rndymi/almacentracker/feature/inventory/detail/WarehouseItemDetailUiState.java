package com.rndymi.almacentracker.feature.inventory.detail;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Objects;

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
    private final boolean deleting;
    private final String deleteErrorMessage;

    private WarehouseItemDetailUiState(
            Status status,
            WarehouseItem warehouseItem,
            String errorMessage,
            boolean deleting,
            String deleteErrorMessage
    ) {
        this.status = status;
        this.warehouseItem = warehouseItem;
        this.errorMessage = errorMessage;
        this.deleting = deleting;
        this.deleteErrorMessage = deleteErrorMessage;
    }

    public static WarehouseItemDetailUiState loading() {
        return new WarehouseItemDetailUiState(
                Status.LOADING,
                null,
                null,
                false,
                null
        );
    }

    public static WarehouseItemDetailUiState content(
            WarehouseItem warehouseItem
    ) {
        Objects.requireNonNull(warehouseItem);

        return new WarehouseItemDetailUiState(
                Status.CONTENT,
                warehouseItem,
                null,
                false,
                null
        );
    }

    public static WarehouseItemDetailUiState deleting(
            WarehouseItem warehouseItem
    ) {
        Objects.requireNonNull(warehouseItem);

        return new WarehouseItemDetailUiState(
                Status.CONTENT,
                warehouseItem,
                null,
                true,
                null
        );
    }

    public static WarehouseItemDetailUiState deleteError(
            WarehouseItem warehouseItem,
            String deleteErrorMessage
    ) {
        Objects.requireNonNull(warehouseItem);
        requireMessage(
                deleteErrorMessage,
                "Delete error"
        );

        return new WarehouseItemDetailUiState(
                Status.CONTENT,
                warehouseItem,
                null,
                false,
                deleteErrorMessage
        );
    }

    public static WarehouseItemDetailUiState notFound() {
        return new WarehouseItemDetailUiState(
                Status.NOT_FOUND,
                null,
                null,
                false,
                null
        );
    }

    public static WarehouseItemDetailUiState invalidId() {
        return new WarehouseItemDetailUiState(
                Status.INVALID_ID,
                null,
                null,
                false,
                null
        );
    }

    public static WarehouseItemDetailUiState error(
            String errorMessage
    ) {
        requireMessage(errorMessage, "Error");

        return new WarehouseItemDetailUiState(
                Status.ERROR,
                null,
                errorMessage,
                false,
                null
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

    public boolean isDeleting() {
        return deleting;
    }

    public String getDeleteErrorMessage() {
        return deleteErrorMessage;
    }

    private static void requireMessage(
            String message,
            String stateName
    ) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    stateName + " requires a message"
            );
        }
    }
}
