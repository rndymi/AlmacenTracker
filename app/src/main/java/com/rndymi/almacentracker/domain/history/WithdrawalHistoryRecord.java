package com.rndymi.almacentracker.domain.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class WithdrawalHistoryRecord {

    private final WithdrawalHistory history;
    private final List<WithdrawalHistoryEntry> entries;

    public WithdrawalHistoryRecord(
            WithdrawalHistory history,
            List<WithdrawalHistoryEntry> entries
    ) {
        this.history = Objects.requireNonNull(
                history,
                "History cannot be null"
        );

        Objects.requireNonNull(
                entries,
                "History entries cannot be null"
        );

        List<WithdrawalHistoryEntry> entriesCopy =
                new ArrayList<>(entries.size());

        for (WithdrawalHistoryEntry entry : entries) {
            entriesCopy.add(
                    Objects.requireNonNull(
                            entry,
                            "History entries cannot contain null values"
                    )
            );
        }

        entriesCopy.sort(
                Comparator.comparingInt(
                        WithdrawalHistoryEntry::getOrderIndex
                )
        );

        this.entries = Collections.unmodifiableList(
                entriesCopy
        );
    }

    public WithdrawalHistory getHistory() {
        return history;
    }

    public List<WithdrawalHistoryEntry> getEntries() {
        return entries;
    }

    public boolean hasEntries() {
        return !entries.isEmpty();
    }
}