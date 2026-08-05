package com.rndymi.almacentracker.domain.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WithdrawalStoreInputParser {

    private static final Pattern STORE_NUMBER =
            Pattern.compile("^(?:TIENDA\\s*:?)?\\s*(\\d{1,2})$");

    private WithdrawalStoreInputParser() {
    }

    public static Result parse(String value) {
        String normalized = value == null ? "" : value.trim();

        if (normalized.isEmpty()) {
            return Result.valid(Collections.emptyList());
        }

        List<Integer> numbers = new ArrayList<>();
        String[] tokens = normalized.split("[,;\\s]+");

        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }

            Integer number = parseNumber(token);

            if (number == null || number < 1 || number > 50) {
                return Result.invalid();
            }

            if (!numbers.contains(number)) {
                numbers.add(number);
            }
        }

        List<String> destinations = new ArrayList<>();

        for (Integer number : numbers) {
            destinations.add(toCircledNumber(number));
        }

        return Result.valid(destinations);
    }

    public static String format(List<String> destinations) {
        if (destinations == null || destinations.isEmpty()) {
            return "";
        }

        List<String> numbers = new ArrayList<>();

        for (String destination : destinations) {
            Integer number = parseNumber(destination);

            if (number != null
                    && number >= 1
                    && number <= 50
                    && !numbers.contains(String.valueOf(number))) {
                numbers.add(String.valueOf(number));
            }
        }

        return String.join(", ", numbers);
    }

    private static Integer parseNumber(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.codePointCount(0, normalized.length()) == 1) {
            int codePoint = normalized.codePointAt(0);
            Integer circled = fromCircledNumber(codePoint);

            if (circled != null) {
                return circled;
            }
        }

        Matcher matcher = STORE_NUMBER.matcher(
                normalized.toUpperCase(Locale.ROOT)
        );

        if (!matcher.matches()) {
            return null;
        }

        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static Integer fromCircledNumber(int codePoint) {
        if (codePoint >= 0x2460 && codePoint <= 0x2473) {
            return codePoint - 0x2460 + 1;
        }

        if (codePoint >= 0x3251 && codePoint <= 0x325F) {
            return codePoint - 0x3251 + 21;
        }

        if (codePoint >= 0x32B1 && codePoint <= 0x32BF) {
            return codePoint - 0x32B1 + 36;
        }

        return null;
    }

    private static String toCircledNumber(int number) {
        int codePoint;

        if (number <= 20) {
            codePoint = 0x2460 + number - 1;
        } else if (number <= 35) {
            codePoint = 0x3251 + number - 21;
        } else {
            codePoint = 0x32B1 + number - 36;
        }

        return new String(Character.toChars(codePoint));
    }

    public static final class Result {

        private final boolean valid;
        private final List<String> destinations;

        private Result(boolean valid, List<String> destinations) {
            this.valid = valid;
            this.destinations = WithdrawalDestinationCodec
                    .immutableCopy(destinations);
        }

        private static Result valid(List<String> destinations) {
            return new Result(true, destinations);
        }

        private static Result invalid() {
            return new Result(false, Collections.emptyList());
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getDestinations() {
            return destinations;
        }
    }
}
