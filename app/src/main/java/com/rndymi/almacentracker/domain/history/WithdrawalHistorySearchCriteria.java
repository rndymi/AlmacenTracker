package com.rndymi.almacentracker.domain.history;

import java.util.Objects;

public final class WithdrawalHistorySearchCriteria {

    private final String query;
    private final Long registeredFromInclusive;
    private final Long registeredToExclusive;

    public WithdrawalHistorySearchCriteria(
            String query,
            Long registeredFromInclusive,
            Long registeredToExclusive
    ) {
        this.query = normalizeQuery(query);
        this.registeredFromInclusive =
                registeredFromInclusive;
        this.registeredToExclusive =
                registeredToExclusive;

        validateDates(
                registeredFromInclusive,
                registeredToExclusive
        );
    }

    public static WithdrawalHistorySearchCriteria empty() {
        return new WithdrawalHistorySearchCriteria(
                "",
                null,
                null
        );
    }

    public String getQuery() {
        return query;
    }

    public Long getRegisteredFromInclusive() {
        return registeredFromInclusive;
    }

    public Long getRegisteredToExclusive() {
        return registeredToExclusive;
    }

    public boolean hasQuery() {
        return !query.isEmpty();
    }

    public boolean hasActiveCriteria() {
        return hasQuery()
                || registeredFromInclusive != null
                || registeredToExclusive != null;
    }

    public String createLikePattern() {
        if (!hasQuery()) {
            return "";
        }

        return "%"
                + escapeLikeValue(query)
                + "%";
    }

    private static String normalizeQuery(
            String query
    ) {
        if (query == null) {
            return "";
        }

        return query.trim();
    }

    private static void validateDates(
            Long fromInclusive,
            Long toExclusive
    ) {
        if (fromInclusive != null
                && fromInclusive < 0L) {
            throw new IllegalArgumentException(
                    "registeredFromInclusive cannot be negative"
            );
        }

        if (toExclusive != null
                && toExclusive <= 0L) {
            throw new IllegalArgumentException(
                    "registeredToExclusive must be positive"
            );
        }

        if (fromInclusive != null
                && toExclusive != null
                && fromInclusive >= toExclusive) {
            throw new IllegalArgumentException(
                    "Invalid registered date interval"
            );
        }
    }

    private static String escapeLikeValue(
            String value
    ) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other
                instanceof WithdrawalHistorySearchCriteria)) {
            return false;
        }

        WithdrawalHistorySearchCriteria criteria =
                (WithdrawalHistorySearchCriteria) other;

        return query.equals(criteria.query)
                && Objects.equals(
                registeredFromInclusive,
                criteria.registeredFromInclusive
        )
                && Objects.equals(
                registeredToExclusive,
                criteria.registeredToExclusive
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                query,
                registeredFromInclusive,
                registeredToExclusive
        );
    }
}
