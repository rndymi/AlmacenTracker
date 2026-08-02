package com.rndymi.almacentracker.feature.withdrawal_history.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WithdrawalHistoryRepository;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;

import java.util.Objects;

public final class WithdrawalHistoryDetailViewModel
        extends ViewModel {

    private final WithdrawalHistoryRepository repository;

    private final MutableLiveData<
            WithdrawalHistoryDetailUiState
            > uiState =
            new MutableLiveData<>(
                    WithdrawalHistoryDetailUiState.loading(
                            null
                    )
            );

    private long historyId = -1L;

    private boolean initialized;

    private boolean loading;

    public WithdrawalHistoryDetailViewModel(
            WithdrawalHistoryRepository repository
    ) {
        this.repository =
                Objects.requireNonNull(
                        repository,
                        "repository"
                );
    }

    public LiveData<WithdrawalHistoryDetailUiState>
    getUiState() {
        return uiState;
    }

    public void load(long requestedHistoryId) {
        if (initialized) {
            return;
        }

        initialized = true;
        historyId = requestedHistoryId;

        if (historyId <= 0L) {
            uiState.setValue(
                    WithdrawalHistoryDetailUiState
                            .notFound()
            );
            return;
        }

        loadInternal();
    }

    public void retry() {
        if (historyId <= 0L || loading) {
            return;
        }

        loadInternal();
    }

    private void loadInternal() {
        if (loading) {
            return;
        }

        loading = true;

        WithdrawalHistoryRecord previousRecord =
                currentRecord();

        uiState.setValue(
                WithdrawalHistoryDetailUiState.loading(
                        previousRecord
                )
        );

        repository.findById(
                historyId,
                new RepositoryCallback<
                        WithdrawalHistoryRecord
                        >() {

                    @Override
                    public void onSuccess(
                            WithdrawalHistoryRecord record
                    ) {
                        loading = false;

                        if (record == null) {
                            uiState.postValue(
                                    WithdrawalHistoryDetailUiState
                                            .notFound()
                            );
                            return;
                        }

                        uiState.postValue(
                                WithdrawalHistoryDetailUiState
                                        .content(record)
                        );
                    }

                    @Override
                    public void onNotFound() {
                        loading = false;

                        uiState.postValue(
                                WithdrawalHistoryDetailUiState
                                        .notFound()
                        );
                    }

                    @Override
                    public void onError(Throwable cause) {
                        loading = false;

                        uiState.postValue(
                                WithdrawalHistoryDetailUiState
                                        .error(previousRecord)
                        );
                    }
                }
        );
    }

    private WithdrawalHistoryRecord currentRecord() {
        WithdrawalHistoryDetailUiState currentState =
                uiState.getValue();

        return currentState == null
                ? null
                : currentState.getRecord();
    }
}
