package com.rndymi.almacentracker.domain.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WithdrawalHistoryDraft {

    private final String title;
    private final long registeredAt;
    private final List<WithdrawalHistoryDraftEntry> entries;

    public WithdrawalHistoryDraft(
            String title,
            long registeredAt,
            List<WithdrawalHistoryDraftEntry> entries
    ) {
        if (registeredAt <= 0L) {
            throw new IllegalArgumentException(
                    "registeredAt must be positive"
            );
        }

        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException(
                    "entries must not be empty"
            );
        }

        this.title = normalizeOptional(title);
        this.registeredAt = registeredAt;
        this.entries =
                Collections.unmodifiableList(
                        new ArrayList<>(entries)
                );
    }

    public String getTitle() {
        return title;
    }

    public long getRegisteredAt() {
        return registeredAt;
    }

    public List<WithdrawalHistoryDraftEntry>
    getEntries() {
        return entries;
    }

    private static String normalizeOptional(
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
}
