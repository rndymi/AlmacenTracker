package com.rndymi.almacentracker.application.result;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Objects;

public abstract class WarehouseItemDetailResult {
    private WarehouseItemDetailResult() {

    }
    public static WarehouseItemDetailResult found(
            WarehouseItem warehouseItem
    ) {
        return new Found(warehouseItem);
    }
    public static WarehouseItemDetailResult notFound() {
        return new NotFound();
    }
    public static WarehouseItemDetailResult invalidId() {
        return new InvalidId();
    }
    public static WarehouseItemDetailResult error(Throwable cause) {
        return new Error(cause);
    }

    public static final class Found
            extends WarehouseItemDetailResult {
        private final WarehouseItem warehouseItem;

        private Found(WarehouseItem warehouseItem) {
            this.warehouseItem =
                    Objects.requireNonNull(warehouseItem);
        }

        public WarehouseItem getWarehouseItem() {
            return warehouseItem;
        }
    }
    public static final class NotFound
            extends WarehouseItemDetailResult {
        private NotFound() {
        }
    }
    public static final class InvalidId
            extends WarehouseItemDetailResult {
        private InvalidId() {
        }
    }
    public static final class Error
            extends WarehouseItemDetailResult {
        private final Throwable cause;
        private Error(Throwable cause) {
            this.cause = Objects.requireNonNull(cause);
        }
        public Throwable getCause() {
            return cause;
        }
    }
}
