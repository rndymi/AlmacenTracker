package com.rndymi.almacentracker.domain.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WithdrawalDestinationCodec {

    private WithdrawalDestinationCodec() {
    }

    public static String encode(List<String> values) {
        List<String> normalized = immutableCopy(values);

        if (normalized.isEmpty()) {
            return null;
        }

        StringBuilder result = new StringBuilder();

        for (String value : normalized) {
            result.append(value.length())
                    .append(':')
                    .append(value);
        }

        return result.toString();
    }

    public static List<String> decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        int index = 0;

        while (index < encoded.length()) {
            int separator = encoded.indexOf(':', index);

            if (separator <= index) {
                return Collections.emptyList();
            }

            int length;

            try {
                length = Integer.parseInt(
                        encoded.substring(index, separator)
                );
            } catch (NumberFormatException exception) {
                return Collections.emptyList();
            }

            int valueStart = separator + 1;
            int valueEnd = valueStart + length;

            if (length <= 0 || valueEnd > encoded.length()) {
                return Collections.emptyList();
            }

            String value = encoded.substring(
                    valueStart,
                    valueEnd
            );

            if (!result.contains(value)) {
                result.add(value);
            }

            index = valueEnd;
        }

        return Collections.unmodifiableList(result);
    }

    public static List<String> immutableCopy(
            List<String> values
    ) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();

        for (String value : values) {
            if (value != null
                    && !value.trim().isEmpty()
                    && !result.contains(value.trim())) {
                result.add(value.trim());
            }
        }

        return result.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(result);
    }
}
