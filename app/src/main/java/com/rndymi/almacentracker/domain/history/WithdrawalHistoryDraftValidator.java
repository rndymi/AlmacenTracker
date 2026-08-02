package com.rndymi.almacentracker.domain.history;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WithdrawalHistoryDraftValidator {

    public static final int MAX_TITLE_LENGTH = 120;
    public static final int MAX_UNIT_LENGTH = 30;
    public static final int MAX_QUANTITY_DIGITS = 9;

    public WithdrawalHistoryDraftValidationResult validate(
            String title,
            long registeredAt,
            List<EditableEntry> entries,
            long now
    ) {
        String titleError = validateTitle(title);
        String dateError =
                validateRegisteredAt(
                        registeredAt,
                        now
                );

        Map<Long,
                WithdrawalHistoryDraftValidationResult
                        .EntryErrors> entryErrors =
                new HashMap<>();

        if (entries != null) {
            for (EditableEntry entry : entries) {
                if (entry == null) {
                    continue;
                }

                String quantityError =
                        validateQuantity(
                                entry.getQuantityText()
                        );

                String unitError =
                        validateUnit(
                                entry.getQuantityText(),
                                entry.getUnitText()
                        );

                if (quantityError != null
                        || unitError != null) {
                    entryErrors.put(
                            entry.getStableId(),
                            new WithdrawalHistoryDraftValidationResult
                                    .EntryErrors(
                                    quantityError,
                                    unitError
                            )
                    );
                }
            }
        }

        return new WithdrawalHistoryDraftValidationResult(
                titleError,
                dateError,
                entryErrors
        );
    }

    public String normalizeTitle(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    public Integer parseQuantity(
            String value
    ) {
        if (value == null
                || value.trim().isEmpty()) {
            return null;
        }

        try {
            int quantity =
                    Integer.parseInt(
                            value.trim()
                    );

            return quantity > 0
                    ? quantity
                    : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String validateTitle(
            String value
    ) {
        String normalized =
                normalizeTitle(value);

        if (normalized != null
                && normalized.length()
                > MAX_TITLE_LENGTH) {
            return "El título no puede superar "
                    + MAX_TITLE_LENGTH
                    + " caracteres.";
        }

        return null;
    }

    private String validateRegisteredAt(
            long registeredAt,
            long now
    ) {
        if (registeredAt <= 0L) {
            return "La fecha no es válida.";
        }

        long allowedClockDifference =
                5L * 60L * 1000L;

        if (registeredAt
                > now + allowedClockDifference) {
            return "La fecha no puede estar en el futuro.";
        }

        return null;
    }

    private String validateQuantity(
            String value
    ) {
        if (value == null
                || value.trim().isEmpty()) {
            return null;
        }

        String normalized = value.trim();

        if (!normalized.matches("[0-9]+")) {
            return "Introduce una cantidad entera positiva.";
        }

        if (normalized.length()
                > MAX_QUANTITY_DIGITS) {
            return "La cantidad es demasiado grande.";
        }

        try {
            int quantity =
                    Integer.parseInt(normalized);

            if (quantity <= 0) {
                return "La cantidad debe ser mayor que cero.";
            }
        } catch (NumberFormatException exception) {
            return "La cantidad es demasiado grande.";
        }

        return null;
    }

    private String validateUnit(
            String quantity,
            String unit
    ) {
        String normalizedUnit =
                unit == null
                        ? ""
                        : unit.trim();

        String normalizedQuantity =
                quantity == null
                        ? ""
                        : quantity.trim();

        if (normalizedUnit.isEmpty()) {
            return null;
        }

        if (normalizedUnit.length()
                > MAX_UNIT_LENGTH) {
            return "La unidad no puede superar "
                    + MAX_UNIT_LENGTH
                    + " caracteres.";
        }

        if (normalizedQuantity.isEmpty()) {
            return "Introduce una cantidad o elimina la unidad.";
        }

        return null;
    }

    public interface EditableEntry {

        long getStableId();

        String getQuantityText();

        String getUnitText();
    }
}
