package com.rndymi.almacentracker.domain.rule;

import java.util.EnumSet;

public final class WarehouseItemValidator {

    public enum RequiredField {
        CATEGORY,
        CODE,
        SITE
    }

    public ValidationResult validateRequiredFields(
            String category,
            String code,
            String site
    ) {
        EnumSet<RequiredField> missingFields =
                EnumSet.noneOf(RequiredField.class);

        addIfMissing(
                missingFields,
                RequiredField.CATEGORY,
                category
        );
        addIfMissing(
                missingFields,
                RequiredField.CODE,
                code
        );
        addIfMissing(
                missingFields,
                RequiredField.SITE,
                site
        );

        return new ValidationResult(missingFields);
    }

    private void addIfMissing(
            EnumSet<RequiredField> missingFields,
            RequiredField field,
            String value
    ) {
        if (value == null || value.trim().isEmpty()) {
            missingFields.add(field);
        }
    }

    public static final class ValidationResult {

        private final EnumSet<RequiredField> missingFields;

        private ValidationResult(
                EnumSet<RequiredField> missingFields
        ) {
            this.missingFields =
                    EnumSet.copyOf(missingFields);
        }

        public boolean isValid() {
            return missingFields.isEmpty();
        }

        public boolean isMissing(RequiredField field) {
            return missingFields.contains(field);
        }
    }
}
