package com.rndymi.almacentracker.application.result;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Collections;
import java.util.List;

public abstract class WarehouseItemsResult {

    private WarehouseItemsResult() {
    }

    public static WarehouseItemsResult success(List<WarehouseItem> items) {
        return new Success(items);
    }

    public static WarehouseItemsResult error(Throwable cause) {
        return new Error(cause);
    }

    public static final class Success extends WarehouseItemsResult {

        private final List<WarehouseItem> items;

        private Success(List<WarehouseItem> items) {
            this.items = items == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(items);
        }

        public List<WarehouseItem> getItems() {
            return items;
        }
    }

    public static final class Error extends WarehouseItemsResult {

        private final Throwable cause;

        private Error(Throwable cause) {
            this.cause = cause;
        }

        public Throwable getCause() {
            return cause;
        }
    }
}