package com.rndymi.almacentracker.feature.withdrawal_history.detail;

import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;

import java.util.Objects;

public final class WithdrawalHistoryDetailUiState {

    public enum Status {
        LOADING,
        CONTENT,
        DELETING,
        NOT_FOUND,
        LOAD_ERROR,
        DELETE_ERROR
    }

    private final Status status;

    private final WithdrawalHistoryRecord record;

    private WithdrawalHistoryDetailUiState(
            Status status,
            WithdrawalHistoryRecord record
    ) {
        this.status = Objects.requireNonNull(
                status,
                "status"
        );

        this.record = record;
    }

    public static WithdrawalHistoryDetailUiState loading(
            WithdrawalHistoryRecord previousRecord
    ) {
        return new WithdrawalHistoryDetailUiState(
                Status.LOADING,
                previousRecord
        );
    }

    public static WithdrawalHistoryDetailUiState content(
            WithdrawalHistoryRecord record
    ) {
        return new WithdrawalHistoryDetailUiState(
                Status.CONTENT,
                Objects.requireNonNull(
                        record,
                        "record"
                )
        );
    }

    public static WithdrawalHistoryDetailUiState deleting(
            WithdrawalHistoryRecord record
    ) {
        return new WithdrawalHistoryDetailUiState(
                Status.DELETING,
                Objects.requireNonNull(
                        record,
                        "record"
                )
        );
    }

    public static WithdrawalHistoryDetailUiState notFound() {
        return new WithdrawalHistoryDetailUiState(
                Status.NOT_FOUND,
                null
        );
    }

    public static WithdrawalHistoryDetailUiState loadError(
            WithdrawalHistoryRecord previousRecord
    ) {
        return new WithdrawalHistoryDetailUiState(
                Status.LOAD_ERROR,
                previousRecord
        );
    }

    public static WithdrawalHistoryDetailUiState deleteError(
            WithdrawalHistoryRecord record
    ) {
        return new WithdrawalHistoryDetailUiState(
                Status.DELETE_ERROR,
                Objects.requireNonNull(
                        record,
                        "record"
                )
        );
    }

    public Status getStatus() {
        return status;
    }

    public WithdrawalHistoryRecord getRecord() {
        return record;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isDeleting() {
        return status == Status.DELETING;
    }

    public boolean hasContent() {
        return record != null;
    }

    public boolean isNotFound() {
        return status == Status.NOT_FOUND;
    }

    public boolean hasLoadError() {
        return status == Status.LOAD_ERROR;
    }

    public boolean hasDeleteError() {
        return status == Status.DELETE_ERROR;
    }

    public boolean canDelete() {
        return status == Status.CONTENT
                || status == Status.DELETE_ERROR;
    }
}
