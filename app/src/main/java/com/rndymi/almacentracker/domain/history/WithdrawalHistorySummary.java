package com.rndymi.almacentracker.domain.history;

import java.util.Objects;

public final class WithdrawalHistorySummary {

    private final long id;
    private final String title;
    private final long registeredAt;
    private final long createdAt;
    private final long updatedAt;
    private final int entryCount;
    private final int foundCount;
    private final int notFoundCount;

    public WithdrawalHistorySummary(
            long id,
            String title,
            long registeredAt,
            long createdAt,
            long updatedAt,
            int entryCount,
            int foundCount,
            int notFoundCount
    ) {
        if (id <= 0L) {
            throw new IllegalArgumentException(
                    "History id must be positive"
            );
        }

        if (registeredAt <= 0L) {
            throw new IllegalArgumentException(
                    "Registered timestamp must be positive"
            );
        }

        if (createdAt <= 0L) {
            throw new IllegalArgumentException(
                    "Created timestamp must be positive"
            );
        }

        if (updatedAt <= 0L) {
            throw new IllegalArgumentException(
                    "Updated timestamp must be positive"
            );
        }

        if (entryCount < 0
                || foundCount < 0
                || notFoundCount < 0) {
            throw new IllegalArgumentException(
                    "History counters cannot be negative"
            );
        }

        if (foundCount + notFoundCount != entryCount) {
            throw new IllegalArgumentException(
                    "History counters are inconsistent"
            );
        }

        this.id = id;
        this.title = normalizeNullableTitle(title);
        this.registeredAt = registeredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.entryCount = entryCount;
        this.foundCount = foundCount;
        this.notFoundCount = notFoundCount;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getRegisteredAt() {
        return registeredAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public int getFoundCount() {
        return foundCount;
    }

    public int getNotFoundCount() {
        return notFoundCount;
    }

    public boolean hasTitle() {
        return title != null;
    }

    private static String normalizeNullableTitle(
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

    @Override
    public boolean equals(Object value) {
        if (this == value) {
            return true;
        }

        if (!(value instanceof WithdrawalHistorySummary)) {
            return false;
        }

        WithdrawalHistorySummary other =
                (WithdrawalHistorySummary) value;

        return id == other.id
                && registeredAt == other.registeredAt
                && createdAt == other.createdAt
                && updatedAt == other.updatedAt
                && entryCount == other.entryCount
                && foundCount == other.foundCount
                && notFoundCount == other.notFoundCount
                && Objects.equals(title, other.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                title,
                registeredAt,
                createdAt,
                updatedAt,
                entryCount,
                foundCount,
                notFoundCount
        );
    }
}
