package com.rndymi.almacentracker.feature.withdrawal_history.create;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WithdrawalHistoryCreateUiState {

    public enum Status {
        INITIALIZING,
        READY,
        INVALID_INPUT,
        ERROR
    }

    private final Status status;
    private final String title;
    private final long registeredAt;
    private final List<WithdrawalHistoryDraftEntryUiModel>
            entries;
    private final String titleError;
    private final String dateError;

    private WithdrawalHistoryCreateUiState(
            Status status,
            String title,
            long registeredAt,
            List<WithdrawalHistoryDraftEntryUiModel> entries,
            String titleError,
            String dateError
    ) {
        this.status = status;
        this.title = title;
        this.registeredAt = registeredAt;
        this.entries =
                Collections.unmodifiableList(
                        new ArrayList<>(entries)
                );
        this.titleError = titleError;
        this.dateError = dateError;
    }

    public static WithdrawalHistoryCreateUiState
    initializing() {
        return new WithdrawalHistoryCreateUiState(
                Status.INITIALIZING,
                "",
                0L,
                Collections.emptyList(),
                null,
                null
        );
    }

    public static WithdrawalHistoryCreateUiState ready(
            String title,
            long registeredAt,
            List<WithdrawalHistoryDraftEntryUiModel> entries
    ) {
        return new WithdrawalHistoryCreateUiState(
                Status.READY,
                title,
                registeredAt,
                entries,
                null,
                null
        );
    }

    public static WithdrawalHistoryCreateUiState invalid(
            String title,
            long registeredAt,
            List<WithdrawalHistoryDraftEntryUiModel> entries,
            String titleError,
            String dateError
    ) {
        return new WithdrawalHistoryCreateUiState(
                Status.INVALID_INPUT,
                title,
                registeredAt,
                entries,
                titleError,
                dateError
        );
    }

    public static WithdrawalHistoryCreateUiState error() {
        return new WithdrawalHistoryCreateUiState(
                Status.ERROR,
                "",
                0L,
                Collections.emptyList(),
                null,
                null
        );
    }

    public Status getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public long getRegisteredAt() {
        return registeredAt;
    }

    public List<WithdrawalHistoryDraftEntryUiModel>
    getEntries() {
        return entries;
    }

    public String getTitleError() {
        return titleError;
    }

    public String getDateError() {
        return dateError;
    }

    public boolean canContinue() {
        return status == Status.READY
                || status == Status.INVALID_INPUT;
    }
}
