package com.rndymi.almacentracker.application.result;

import java.util.Objects;

public abstract class WarehouseItemFilterOptionsResult {

    private WarehouseItemFilterOptionsResult() {
    }

    public static WarehouseItemFilterOptionsResult success(
            WarehouseItemFilterOptions options
    ) {
        return new Success(options);
    }

    public static WarehouseItemFilterOptionsResult error(
            Throwable cause
    ) {
        return new Error(cause);
    }

    public static final class Success
            extends WarehouseItemFilterOptionsResult {

        private final WarehouseItemFilterOptions options;

        private Success(
                WarehouseItemFilterOptions options
        ) {
            this.options = Objects.requireNonNull(options);
        }

        public WarehouseItemFilterOptions getOptions() {
            return options;
        }
    }

    public static final class Error
            extends WarehouseItemFilterOptionsResult {

        private final Throwable cause;

        private Error(Throwable cause) {
            this.cause = Objects.requireNonNull(cause);
        }

        public Throwable getCause() {
            return cause;
        }
    }
}
