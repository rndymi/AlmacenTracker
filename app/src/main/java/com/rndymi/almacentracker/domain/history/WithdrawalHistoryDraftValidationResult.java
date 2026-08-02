package com.rndymi.almacentracker.domain.history;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class WithdrawalHistoryDraftValidationResult {

    private final String titleError;
    private final String dateError;
    private final Map<Long, EntryErrors> entryErrors;

    public WithdrawalHistoryDraftValidationResult(
            String titleError,
            String dateError,
            Map<Long, EntryErrors> entryErrors
    ) {
        this.titleError = titleError;
        this.dateError = dateError;
        this.entryErrors =
                Collections.unmodifiableMap(
                        new HashMap<>(
                                entryErrors == null
                                        ? Collections.emptyMap()
                                        : entryErrors
                        )
                );
    }

    public String getTitleError() {
        return titleError;
    }

    public String getDateError() {
        return dateError;
    }

    public Map<Long, EntryErrors> getEntryErrors() {
        return entryErrors;
    }

    public boolean isValid() {
        return titleError == null
                && dateError == null
                && entryErrors.isEmpty();
    }

    public static final class EntryErrors {

        private final String quantityError;
        private final String unitError;

        public EntryErrors(
                String quantityError,
                String unitError
        ) {
            this.quantityError = quantityError;
            this.unitError = unitError;
        }

        public String getQuantityError() {
            return quantityError;
        }

        public String getUnitError() {
            return unitError;
        }

        public boolean hasErrors() {
            return quantityError != null
                    || unitError != null;
        }
    }
}
