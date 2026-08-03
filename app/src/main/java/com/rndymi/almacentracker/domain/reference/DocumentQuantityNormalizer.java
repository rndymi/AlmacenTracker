package com.rndymi.almacentracker.domain.reference;

public final class DocumentQuantityNormalizer {

    private static final int MAXIMUM_LENGTH = 9;

    public Integer normalize(
            String observedValue
    ) {
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

        if (compact.isEmpty()
                || compact.length() > MAXIMUM_LENGTH) {
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

        try {
            int quantity =
                    Integer.parseInt(
                            digits.toString()
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
                return '1';

            case 'Z':
                return '2';

            case 'S':
                return '5';

            case 'G':
                return '6';

            case 'J':
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
