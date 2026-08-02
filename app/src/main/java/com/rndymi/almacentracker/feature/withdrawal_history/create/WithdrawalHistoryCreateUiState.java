package com.rndymi.almacentracker.feature.withdrawal_history.create;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WithdrawalHistoryCreateUiState {

    public enum Status {
        INITIALIZING,
        READY,
        INVALID_INPUT,
        SAVING,
        SAVED,
        ERROR
    }

    private final Status status;
    private final String title;
    private final long registeredAt;
    private final List<WithdrawalHistoryDraftEntryUiModel>
            entries;
    private final String titleError;
    private final String dateError;
    private final String saveError;

    private WithdrawalHistoryCreateUiState(
            Status status,
            String title,
            long registeredAt,
            List<WithdrawalHistoryDraftEntryUiModel> entries,
            String titleError,
            String dateError,
            String saveError
    ) {
        this.status = status;
        this.title = title == null ? "" : title;
        this.registeredAt = registeredAt;
        this.entries =
                Collections.unmodifiableList(
                        new ArrayList<>(entries)
                );
        this.titleError = titleError;
        this.dateError = dateError;
        this.saveError = saveError;
    }

    public static WithdrawalHistoryCreateUiState
    initializing() {
        return new WithdrawalHistoryCreateUiState(
                Status.INITIALIZING,
                "",
                0L,
                Collections.emptyList(),
                null,
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
                dateError,
                null
        );
    }

    public static WithdrawalHistoryCreateUiState saving(
            String title,
            long registeredAt,
            List<WithdrawalHistoryDraftEntryUiModel> entries
    ) {
        return new WithdrawalHistoryCreateUiState(
                Status.SAVING,
                title,
                registeredAt,
                entries,
                null,
                null,
                null
        );
    }

    public static WithdrawalHistoryCreateUiState saved(
            String title,
            long registeredAt,
            List<WithdrawalHistoryDraftEntryUiModel> entries
    ) {
        return new WithdrawalHistoryCreateUiState(
                Status.SAVED,
                title,
                registeredAt,
                entries,
                null,
                null,
                null
        );
    }

    public static WithdrawalHistoryCreateUiState saveError(
            String title,
            long registeredAt,
            List<WithdrawalHistoryDraftEntryUiModel> entries,
            String message
    ) {
        return new WithdrawalHistoryCreateUiState(
                Status.ERROR,
                title,
                registeredAt,
                entries,
                null,
                null,
                message
        );
    }

    public static WithdrawalHistoryCreateUiState
    invalidInitialInput() {
        return new WithdrawalHistoryCreateUiState(
                Status.ERROR,
                "",
                0L,
                Collections.emptyList(),
                null,
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

    public String getSaveError() {
        return saveError;
    }

    public boolean hasInitialInputError() {
        return status == Status.ERROR
                && entries.isEmpty();
    }

    public boolean hasSaveError() {
        return status == Status.ERROR
                && !entries.isEmpty();
    }

    public boolean isEditable() {
        return status == Status.READY
                || status == Status.INVALID_INPUT
                || hasSaveError();
    }

    public boolean isSaving() {
        return status == Status.SAVING;
    }

    public boolean isSaved() {
        return status == Status.SAVED;
    }

    public boolean canRequestSave() {
        return isEditable();
    }
}
