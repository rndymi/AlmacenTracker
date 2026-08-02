package com.rndymi.almacentracker.feature.withdrawal_history.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WithdrawalHistoryRepository;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySearchCriteria;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySummary;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WithdrawalHistoryListViewModel
        extends ViewModel {

    private final WithdrawalHistoryRepository repository;
    private final ZoneId zoneId;

    private final MutableLiveData<
            WithdrawalHistoryListUiState
            > uiState =
            new MutableLiveData<>();

    private List<WithdrawalHistorySummary>
            currentSummaries =
            Collections.emptyList();

    private String query = "";
    private LocalDate fromDate;
    private LocalDate toDate;

    private boolean hasLoaded;
    private long requestGeneration;

    public WithdrawalHistoryListViewModel(
            WithdrawalHistoryRepository repository
    ) {
        this(
                repository,
                ZoneId.systemDefault()
        );
    }

    WithdrawalHistoryListViewModel(
            WithdrawalHistoryRepository repository,
            ZoneId zoneId
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "repository"
        );

        this.zoneId = Objects.requireNonNull(
                zoneId,
                "zoneId"
        );

        publishState(
                WithdrawalHistoryListUiState.Status.LOADING,
                false
        );
    }

    public LiveData<WithdrawalHistoryListUiState>
    getUiState() {
        return uiState;
    }

    public void load() {
        if (hasLoaded) {
            return;
        }

        executeCurrentSearch();
    }

    public void updateQuery(String value) {
        query = value == null
                ? ""
                : value;

        publishCurrentState();
    }

    public void search() {
        executeCurrentSearch();
    }

    public void updateFromDate(LocalDate date) {
        fromDate = date;
        executeCurrentSearch();
    }

    public void updateToDate(LocalDate date) {
        toDate = date;
        executeCurrentSearch();
    }

    public void clearQuery() {
        if (query.trim().isEmpty()) {
            query = "";
            publishCurrentState();
            return;
        }

        query = "";
        executeCurrentSearch();
    }

    public void clearFromDate() {
        if (fromDate == null) {
            return;
        }

        fromDate = null;
        executeCurrentSearch();
    }

    public void clearToDate() {
        if (toDate == null) {
            return;
        }

        toDate = null;
        executeCurrentSearch();
    }

    public void clearCriteria() {
        query = "";
        fromDate = null;
        toDate = null;

        executeCurrentSearch();
    }

    public void refresh() {
        executeCurrentSearch();
    }

    public void retry() {
        executeCurrentSearch();
    }

    private void executeCurrentSearch() {
        if (hasInvalidDateInterval()) {
            requestGeneration++;

            publishState(
                    currentStateStatus(),
                    true
            );

            return;
        }

        WithdrawalHistorySearchCriteria criteria =
                createCriteria();

        long currentRequest =
                ++requestGeneration;

        publishState(
                WithdrawalHistoryListUiState.Status.LOADING,
                false
        );

        repository.searchSummaries(
                criteria,
                new RepositoryCallback<
                        List<WithdrawalHistorySummary>
                        >() {

                    @Override
                    public void onSuccess(
                            List<WithdrawalHistorySummary>
                                    value
                    ) {
                        if (currentRequest
                                != requestGeneration) {
                            return;
                        }

                        hasLoaded = true;

                        List<WithdrawalHistorySummary>
                                safeValue =
                                value == null
                                        ? Collections.emptyList()
                                        : Collections.unmodifiableList(
                                        new ArrayList<>(value)
                                );

                        currentSummaries = safeValue;

                        if (!safeValue.isEmpty()) {
                            publishState(
                                    WithdrawalHistoryListUiState
                                            .Status.CONTENT,
                                    false
                            );
                            return;
                        }

                        publishState(
                                criteria.hasActiveCriteria()
                                        ? WithdrawalHistoryListUiState
                                          .Status.NO_RESULTS
                                        : WithdrawalHistoryListUiState
                                          .Status.EMPTY,
                                false
                        );
                    }

                    @Override
                    public void onNotFound() {
                        onSuccess(
                                Collections.emptyList()
                        );
                    }

                    @Override
                    public void onError(
                            Throwable throwable
                    ) {
                        if (currentRequest
                                != requestGeneration) {
                            return;
                        }

                        hasLoaded = true;

                        publishState(
                                WithdrawalHistoryListUiState
                                        .Status.ERROR,
                                false
                        );
                    }
                }
        );
    }

    private WithdrawalHistorySearchCriteria
    createCriteria() {
        Long fromInclusive =
                fromDate == null
                        ? null
                        : fromDate
                        .atStartOfDay(zoneId)
                        .toInstant()
                        .toEpochMilli();

        Long toExclusive =
                toDate == null
                        ? null
                        : toDate
                        .plusDays(1L)
                        .atStartOfDay(zoneId)
                        .toInstant()
                        .toEpochMilli();

        return new WithdrawalHistorySearchCriteria(
                query,
                fromInclusive,
                toExclusive
        );
    }

    private boolean hasInvalidDateInterval() {
        return fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate);
    }

    private boolean hasActiveCriteria() {
        return !query.trim().isEmpty()
                || fromDate != null
                || toDate != null;
    }

    private void publishCurrentState() {
        WithdrawalHistoryListUiState current =
                uiState.getValue();

        WithdrawalHistoryListUiState.Status status =
                current == null
                        ? WithdrawalHistoryListUiState
                          .Status.LOADING
                        : current.getStatus();

        publishState(
                status,
                hasInvalidDateInterval()
        );
    }

    private WithdrawalHistoryListUiState.Status
    currentStateStatus() {
        WithdrawalHistoryListUiState current =
                uiState.getValue();

        if (current == null) {
            return currentSummaries.isEmpty()
                    ? WithdrawalHistoryListUiState
                      .Status.EMPTY
                    : WithdrawalHistoryListUiState
                      .Status.CONTENT;
        }

        if (current.getStatus()
                == WithdrawalHistoryListUiState
                .Status.LOADING) {
            return currentSummaries.isEmpty()
                    ? WithdrawalHistoryListUiState
                      .Status.EMPTY
                    : WithdrawalHistoryListUiState
                      .Status.CONTENT;
        }

        return current.getStatus();
    }

    private void publishState(
            WithdrawalHistoryListUiState.Status status,
            boolean invalidInterval
    ) {
        uiState.postValue(
                WithdrawalHistoryListUiState.create(
                        status,
                        currentSummaries,
                        query,
                        fromDate,
                        toDate,
                        hasActiveCriteria(),
                        invalidInterval
                )
        );
    }
}
