package com.rndymi.almacentracker.feature.withdrawal_history.list;

import com.rndymi.almacentracker.domain.history.WithdrawalHistorySummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WithdrawalHistoryListUiState {

    public enum Status {
        LOADING,
        CONTENT,
        EMPTY,
        ERROR
    }

    private final Status status;

    private final List<WithdrawalHistorySummary>
            summaries;

    private WithdrawalHistoryListUiState(
            Status status,
            List<WithdrawalHistorySummary> summaries
    ) {
        this.status = Objects.requireNonNull(
                status,
                "status"
        );

        this.summaries =
                Collections.unmodifiableList(
                        new ArrayList<>(
                                Objects.requireNonNull(
                                        summaries,
                                        "summaries"
                                )
                        )
                );
    }

    public static WithdrawalHistoryListUiState loading(
            List<WithdrawalHistorySummary>
                    previousSummaries
    ) {
        return new WithdrawalHistoryListUiState(
                Status.LOADING,
                safeList(previousSummaries)
        );
    }

    public static WithdrawalHistoryListUiState content(
            List<WithdrawalHistorySummary> summaries
    ) {
        if (summaries == null || summaries.isEmpty()) {
            throw new IllegalArgumentException(
                    "Content state requires summaries"
            );
        }

        return new WithdrawalHistoryListUiState(
                Status.CONTENT,
                summaries
        );
    }

    public static WithdrawalHistoryListUiState empty() {
        return new WithdrawalHistoryListUiState(
                Status.EMPTY,
                Collections.emptyList()
        );
    }

    public static WithdrawalHistoryListUiState error(
            List<WithdrawalHistorySummary>
                    previousSummaries
    ) {
        return new WithdrawalHistoryListUiState(
                Status.ERROR,
                safeList(previousSummaries)
        );
    }

    public Status getStatus() {
        return status;
    }

    public List<WithdrawalHistorySummary>
    getSummaries() {
        return summaries;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean hasContent() {
        return !summaries.isEmpty();
    }

    public boolean isEmpty() {
        return status == Status.EMPTY;
    }

    public boolean hasError() {
        return status == Status.ERROR;
    }

    private static List<WithdrawalHistorySummary>
    safeList(
            List<WithdrawalHistorySummary> values
    ) {
        return values == null
                ? Collections.emptyList()
                : values;
    }
}