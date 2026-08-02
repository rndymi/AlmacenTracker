package com.rndymi.almacentracker.feature.withdrawal_history.list;

import com.rndymi.almacentracker.domain.history.WithdrawalHistorySummary;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WithdrawalHistoryListUiState {

    public enum Status {
        LOADING,
        CONTENT,
        EMPTY,
        NO_RESULTS,
        ERROR
    }

    private final Status status;

    private final List<WithdrawalHistorySummary>
            summaries;

    private final String query;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final boolean activeCriteria;
    private final boolean invalidDateInterval;

    private WithdrawalHistoryListUiState(
            Status status,
            List<WithdrawalHistorySummary> summaries,
            String query,
            LocalDate fromDate,
            LocalDate toDate,
            boolean activeCriteria,
            boolean invalidDateInterval
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

        this.query =
                query == null
                        ? ""
                        : query;

        this.fromDate = fromDate;
        this.toDate = toDate;
        this.activeCriteria = activeCriteria;
        this.invalidDateInterval =
                invalidDateInterval;
    }

    public static WithdrawalHistoryListUiState create(
            Status status,
            List<WithdrawalHistorySummary> summaries,
            String query,
            LocalDate fromDate,
            LocalDate toDate,
            boolean activeCriteria,
            boolean invalidDateInterval
    ) {
        return new WithdrawalHistoryListUiState(
                status,
                safeList(summaries),
                query,
                fromDate,
                toDate,
                activeCriteria,
                invalidDateInterval
        );
    }

    public Status getStatus() {
        return status;
    }

    public List<WithdrawalHistorySummary>
    getSummaries() {
        return summaries;
    }

    public String getQuery() {
        return query;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public boolean hasActiveCriteria() {
        return activeCriteria;
    }

    public boolean hasInvalidDateInterval() {
        return invalidDateInterval;
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

    public boolean hasNoResults() {
        return status == Status.NO_RESULTS;
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
