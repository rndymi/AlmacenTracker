package com.rndymi.almacentracker.feature.withdrawal_history.common;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WithdrawalHistoryCreateIntentContract {

    public static final String EXTRA_ENTRIES =
            "com.rndymi.almacentracker.extra."
                    + "WITHDRAWAL_HISTORY_CREATE_ENTRIES";

    private static final String SEPARATOR =
            "\u001F";
    private static final String LIST_SEPARATOR =
            "\u001E";

    private WithdrawalHistoryCreateIntentContract() {
    }

    public static void putEntries(
            Intent intent,
            List<WithdrawalHistoryCreateInput> entries
    ) {
        ArrayList<String> encoded =
                new ArrayList<>();

        if (entries != null) {
            for (
                    WithdrawalHistoryCreateInput entry
                    : entries
            ) {
                if (entry != null) {
                    encoded.add(encode(entry));
                }
            }
        }

        intent.putStringArrayListExtra(
                EXTRA_ENTRIES,
                encoded
        );
    }

    public static List<WithdrawalHistoryCreateInput>
    getEntries(
            @Nullable Intent intent
    ) {
        if (intent == null) {
            return Collections.emptyList();
        }

        ArrayList<String> values =
                intent.getStringArrayListExtra(
                        EXTRA_ENTRIES
                );

        if (values == null) {
            return Collections.emptyList();
        }

        List<WithdrawalHistoryCreateInput> result =
                new ArrayList<>();

        for (String value : values) {
            WithdrawalHistoryCreateInput decoded =
                    decode(value);

            if (decoded != null) {
                result.add(decoded);
            }
        }

        return Collections.unmodifiableList(result);
    }

    private static String encode(
            WithdrawalHistoryCreateInput value
    ) {
        return value.getOrderIndex()
                + SEPARATOR
                + value.getCategory()
                + SEPARATOR
                + value.getCode()
                + SEPARATOR
                + nullableInteger(
                value.getQuantityProposal()
        )
                + SEPARATOR
                + nullableString(
                value.getUnitProposal()
        )
                + SEPARATOR
                + nullableLong(
                value.getWarehouseItemIdSnapshot()
        )
                + SEPARATOR
                + nullableString(
                value.getSiteSnapshot()
        )
                + SEPARATOR
                + nullableString(
                value.getPositionSnapshot()
        )
                + SEPARATOR
                + value.getLocationStatus().name()
                + SEPARATOR
                + encodeDestinations(
                value.getDestinations()
        );
    }

    private static WithdrawalHistoryCreateInput decode(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String[] parts =
                value.split(SEPARATOR, -1);

        if (parts.length != 9
                && parts.length != 10) {
            return null;
        }

        try {
            return new WithdrawalHistoryCreateInput(
                    Integer.parseInt(parts[0]),
                    parts[1],
                    parts[2],
                    parseNullableInteger(parts[3]),
                    emptyToNull(parts[4]),
                    parseNullableLong(parts[5]),
                    emptyToNull(parts[6]),
                    emptyToNull(parts[7]),
                    WithdrawalLocationStatus.valueOf(
                            parts[8]
                    ),
                    parts.length == 10
                            ? decodeDestinations(parts[9])
                            : Collections.emptyList()
            );
        } catch (
                IllegalArgumentException exception
        ) {
            return null;
        }
    }

    private static String nullableString(
            String value
    ) {
        return value == null ? "" : value;
    }

    private static String nullableInteger(
            Integer value
    ) {
        return value == null
                ? ""
                : String.valueOf(value);
    }

    private static String nullableLong(
            Long value
    ) {
        return value == null
                ? ""
                : String.valueOf(value);
    }

    private static String emptyToNull(
            String value
    ) {
        return value == null
                || value.isEmpty()
                ? null
                : value;
    }

    private static Integer parseNullableInteger(
            String value
    ) {
        return value.isEmpty()
                ? null
                : Integer.valueOf(value);
    }

    private static Long parseNullableLong(
            String value
    ) {
        return value.isEmpty()
                ? null
                : Long.valueOf(value);
    }

    private static String encodeDestinations(
            List<String> values
    ) {
        if (values == null || values.isEmpty()) {
            return "";
        }

        List<String> encoded = new ArrayList<>();

        for (String value : values) {
            if (value != null
                    && !value.trim().isEmpty()) {
                encoded.add(
                        android.net.Uri.encode(value.trim())
                );
            }
        }

        return String.join(LIST_SEPARATOR, encoded);
    }

    private static List<String> decodeDestinations(
            String value
    ) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();

        for (String part : value.split(
                LIST_SEPARATOR,
                -1
        )) {
            String decoded = android.net.Uri.decode(part);

            if (decoded != null
                    && !decoded.trim().isEmpty()
                    && !result.contains(decoded.trim())) {
                result.add(decoded.trim());
            }
        }

        return result;
    }
}
