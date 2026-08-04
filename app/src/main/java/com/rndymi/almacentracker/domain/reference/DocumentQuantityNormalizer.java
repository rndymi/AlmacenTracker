package com.rndymi.almacentracker.domain.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DocumentQuantityNormalizer {

    private static final int MAXIMUM_LENGTH = 9;

    public Integer normalize(
            String observedValue
    ) {
        String digits = normalizedDigits(observedValue);

        return parsePositive(digits);
    }

    public List<Integer> suggestZeroSixAlternatives(
            String observedValue
    ) {
        String compact = compact(observedValue);
        String digits = normalizedDigits(observedValue);

        if (compact == null || digits == null) {
            return Collections.emptyList();
        }

        Integer observedQuantity = parsePositive(digits);
        List<Integer> suggestions = new ArrayList<>();

        for (int index = 0; index < digits.length(); index++) {
            char observedCharacter = compact.charAt(index);

            if (observedCharacter != '0'
                    && observedCharacter != '6') {
                continue;
            }

            char current = digits.charAt(index);

            StringBuilder alternative = new StringBuilder(digits);
            alternative.setCharAt(
                    index,
                    current == '0' ? '6' : '0'
            );

            Integer quantity = parsePositive(alternative.toString());

            if (quantity != null
                    && !quantity.equals(observedQuantity)
                    && !suggestions.contains(quantity)) {
                suggestions.add(quantity);
            }
        }

        return suggestions.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(suggestions);
    }

    private String normalizedDigits(
            String observedValue
    ) {
        String compact = compact(observedValue);

        if (compact == null) {
            return null;
        }

        StringBuilder digits =
                new StringBuilder(
                        compact.length()
                );

        for (int index = 0;
             index < compact.length();
             index++) {

            Character normalizedDigit =
                    normalizeCharacter(
                            compact.charAt(index)
                    );

            if (normalizedDigit == null) {
                return null;
            }

            digits.append(normalizedDigit);
        }

        return digits.toString();
    }

    private String compact(String observedValue) {
        if (observedValue == null) {
            return null;
        }

        String compact =
                observedValue
                        .replaceAll(
                                "[\\p{Z}\\s]+",
                                ""
                        )
                        .toUpperCase();

        return compact.isEmpty()
                || compact.length() > MAXIMUM_LENGTH
                ? null
                : compact;
    }

    private Integer parsePositive(String digits) {
        if (digits == null || digits.isEmpty()) {
            return null;
        }

        try {
            int quantity =
                    Integer.parseInt(
                            digits
                    );

            return quantity > 0
                    ? quantity
                    : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Character normalizeCharacter(
            char observed
    ) {
        if (Character.isDigit(observed)) {
            return observed;
        }

        switch (observed) {
            case 'I':
            case 'L':
            case '(':
            case ')':
                return '1';

            case 'Z':
                return '2';

            case 'S':
                return '5';

            case 'G':
                return '6';

            case 'J':
            case 'T':
            case 'F':
            case '王':
                return '7';

            case 'B':
                return '8';

            case 'O':
                return '0';

            default:
                return null;
        }
    }
}
