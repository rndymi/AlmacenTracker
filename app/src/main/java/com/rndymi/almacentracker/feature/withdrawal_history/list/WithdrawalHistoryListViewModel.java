package com.rndymi.almacentracker.feature.withdrawal_history.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WithdrawalHistoryRepository;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WithdrawalHistoryListViewModel
        extends ViewModel {

    private final WithdrawalHistoryRepository repository;

    private final MutableLiveData<
            WithdrawalHistoryListUiState
            > uiState =
            new MutableLiveData<>(
                    WithdrawalHistoryListUiState.loading(
                            Collections.emptyList()
                    )
            );

    private List<WithdrawalHistorySummary>
            currentSummaries =
            Collections.emptyList();

    private boolean hasLoaded;
    private boolean loading;

    public WithdrawalHistoryListViewModel(
            WithdrawalHistoryRepository repository
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "repository"
        );
    }

    public LiveData<WithdrawalHistoryListUiState>
    getUiState() {
        return uiState;
    }

    public void load() {
        if (hasLoaded || loading) {
            return;
        }

        loadSummaries();
    }

    public void refresh() {
        if (loading) {
            return;
        }

        loadSummaries();
    }

    public void retry() {
        if (loading) {
            return;
        }

        loadSummaries();
    }

    private void loadSummaries() {
        loading = true;

        uiState.setValue(
                WithdrawalHistoryListUiState.loading(
                        currentSummaries
                )
        );

        repository.findAllSummaries(
                new RepositoryCallback<
                        List<WithdrawalHistorySummary>
                        >() {

                    @Override
                    public void onSuccess(
                            List<WithdrawalHistorySummary>
                                    value
                    ) {
                        loading = false;
                        hasLoaded = true;

                        List<WithdrawalHistorySummary>
                                safeValue =
                                value == null
                                        ? Collections.emptyList()
                                        : Collections.unmodifiableList(
                                        new ArrayList<>(value)
                                );

                        currentSummaries = safeValue;

                        if (safeValue.isEmpty()) {
                            uiState.postValue(
                                    WithdrawalHistoryListUiState
                                            .empty()
                            );
                            return;
                        }

                        uiState.postValue(
                                WithdrawalHistoryListUiState
                                        .content(safeValue)
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
                        loading = false;
                        hasLoaded = true;

                        uiState.postValue(
                                WithdrawalHistoryListUiState
                                        .error(
                                                currentSummaries
                                        )
                        );
                    }
                }
        );
    }
}
