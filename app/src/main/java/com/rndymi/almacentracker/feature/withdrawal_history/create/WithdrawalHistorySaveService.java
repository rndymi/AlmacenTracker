package com.rndymi.almacentracker.feature.withdrawal_history.create;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WithdrawalHistoryRepository;
import com.rndymi.almacentracker.domain.history.WithdrawalHistory;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraft;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraftEntry;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryEntry;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;
import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.LongSupplier;

public final class WithdrawalHistorySaveService {

    private static final int MAX_TITLE_LENGTH = 120;
    private static final int MAX_UNIT_LENGTH = 30;

    private final WithdrawalHistoryRepository repository;
    private final LongSupplier currentTimeProvider;

    public WithdrawalHistorySaveService(
            WithdrawalHistoryRepository repository,
            LongSupplier currentTimeProvider
    ) {
        this.repository =
                Objects.requireNonNull(
                        repository,
                        "repository"
                );

        this.currentTimeProvider =
                Objects.requireNonNull(
                        currentTimeProvider,
                        "currentTimeProvider"
                );
    }

    public void save(
            WithdrawalHistoryDraft draft,
            RepositoryCallback<Long> callback
    ) {
        Objects.requireNonNull(
                callback,
                "callback"
        );

        try {
            validateDraft(draft);

            long now = currentTimeProvider.getAsLong();

            if (now <= 0L) {
                throw new IllegalStateException(
                        "Current time must be positive"
                );
            }

            WithdrawalHistoryRecord record =
                    createRecord(
                            draft,
                            now
                    );

            repository.insert(
                    record,
                    new RepositoryCallback<Long>() {
                        @Override
                        public void onSuccess(
                                Long generatedId
                        ) {
                            if (generatedId == null
                                    || generatedId <= 0L) {
                                callback.onError(
                                        new IllegalStateException(
                                                "Generated history id "
                                                        + "must be positive"
                                        )
                                );
                                return;
                            }

                            callback.onSuccess(
                                    generatedId
                            );
                        }

                        @Override
                        public void onError(
                                Throwable cause
                        ) {
                            callback.onError(
                                    cause == null
                                            ? new IllegalStateException(
                                            "Unknown history save error"
                                    )
                                            : cause
                            );
                        }
                    }
            );
        } catch (RuntimeException exception) {
            callback.onError(exception);
        }
    }

    private WithdrawalHistoryRecord createRecord(
            WithdrawalHistoryDraft draft,
            long now
    ) {
        WithdrawalHistory history =
                new WithdrawalHistory(
                        0L,
                        draft.getTitle(),
                        draft.getDestination(),
                        draft.getRegisteredAt(),
                        now,
                        now
                );

        List<WithdrawalHistoryEntry> entries =
                new ArrayList<>(
                        draft.getEntries().size()
                );

        for (
                WithdrawalHistoryDraftEntry draftEntry
                : draft.getEntries()
        ) {
            entries.add(
                    new WithdrawalHistoryEntry(
                            0L,
                            0L,
                            draftEntry.getOrderIndex(),
                            draftEntry.getCategory(),
                            draftEntry.getCode(),
                            draftEntry.getQuantity(),
                            draftEntry.getUnit(),
                            draftEntry
                                    .getWarehouseItemIdSnapshot(),
                            draftEntry.getSiteSnapshot(),
                            draftEntry
                                    .getPositionSnapshot(),
                            draftEntry.getLocationStatus(),
                            draftEntry.getDestinations()
                    )
            );
        }

        return new WithdrawalHistoryRecord(
                history,
                entries
        );
    }

    private void validateDraft(
            WithdrawalHistoryDraft draft
    ) {
        if (draft == null) {
            throw new IllegalArgumentException(
                    "Draft cannot be null"
            );
        }

        if (draft.getRegisteredAt() <= 0L) {
            throw new IllegalArgumentException(
                    "Registered timestamp must be positive"
            );
        }

        if (draft.getTitle() != null
                && draft.getTitle().length()
                > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Title exceeds maximum length"
            );
        }

        if (draft.getDestination() != null
                && draft.getDestination().length()
                > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Destination exceeds maximum length"
            );
        }

        List<WithdrawalHistoryDraftEntry> entries =
                draft.getEntries();

        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException(
                    "Draft entries cannot be empty"
            );
        }

        Set<Integer> usedIndexes =
                new HashSet<>();

        for (int position = 0;
             position < entries.size();
             position++) {
            WithdrawalHistoryDraftEntry entry =
                    entries.get(position);

            if (entry == null) {
                throw new IllegalArgumentException(
                        "Draft entries cannot contain null values"
                );
            }

            validateEntry(
                    entry,
                    position,
                    usedIndexes
            );
        }
    }

    private void validateEntry(
            WithdrawalHistoryDraftEntry entry,
            int expectedIndex,
            Set<Integer> usedIndexes
    ) {
        int orderIndex =
                entry.getOrderIndex();

        if (orderIndex < 0) {
            throw new IllegalArgumentException(
                    "Order index cannot be negative"
            );
        }

        if (!usedIndexes.add(orderIndex)) {
            throw new IllegalArgumentException(
                    "Order index cannot be repeated"
            );
        }

        if (orderIndex != expectedIndex) {
            throw new IllegalArgumentException(
                    "Order indexes must be consecutive"
            );
        }

        if (entry.getQuantity() != null
                && entry.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be positive"
            );
        }

        if (entry.getUnit() != null) {
            if (entry.getUnit().length()
                    > MAX_UNIT_LENGTH) {
                throw new IllegalArgumentException(
                        "Unit exceeds maximum length"
                );
            }

            if (entry.getQuantity() == null) {
                throw new IllegalArgumentException(
                        "Unit requires a quantity"
                );
            }
        }

        if (entry.getLocationStatus()
                == WithdrawalLocationStatus.FOUND) {
            validateFoundEntry(entry);
            return;
        }

        validateNotFoundEntry(entry);
    }

    private void validateFoundEntry(
            WithdrawalHistoryDraftEntry entry
    ) {
        Long warehouseItemId =
                entry.getWarehouseItemIdSnapshot();

        if (warehouseItemId == null
                || warehouseItemId <= 0L) {
            throw new IllegalArgumentException(
                    "Found entry requires warehouse item id"
            );
        }

        String site =
                entry.getSiteSnapshot();

        if (site == null
                || site.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Found entry requires site snapshot"
            );
        }
    }

    private void validateNotFoundEntry(
            WithdrawalHistoryDraftEntry entry
    ) {
        if (entry.getWarehouseItemIdSnapshot() != null
                || entry.getSiteSnapshot() != null
                || entry.getPositionSnapshot() != null) {
            throw new IllegalArgumentException(
                    "Not found entry cannot contain "
                            + "location snapshots"
            );
        }
    }
}
