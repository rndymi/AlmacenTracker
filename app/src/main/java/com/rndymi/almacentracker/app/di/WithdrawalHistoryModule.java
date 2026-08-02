package com.rndymi.almacentracker.app.di;

import com.rndymi.almacentracker.data.repository.WithdrawalHistoryRepository;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraftValidator;
import com.rndymi.almacentracker.feature.withdrawal_history.create.WithdrawalHistoryCreateViewModelFactory;
import com.rndymi.almacentracker.feature.withdrawal_history.create.WithdrawalHistorySaveService;
import com.rndymi.almacentracker.feature.withdrawal_history.detail.WithdrawalHistoryDetailViewModelFactory;
import com.rndymi.almacentracker.feature.withdrawal_history.list.WithdrawalHistoryListViewModelFactory;

import java.util.Objects;

public final class WithdrawalHistoryModule {

    private final WithdrawalHistoryRepository repository;

    public WithdrawalHistoryModule(
            WithdrawalHistoryRepository repository
    ) {
        this.repository =
                Objects.requireNonNull(
                        repository,
                        "repository"
                );
    }

    public WithdrawalHistoryCreateViewModelFactory
    provideWithdrawalHistoryCreateViewModelFactory() {
        WithdrawalHistorySaveService saveService =
                new WithdrawalHistorySaveService(
                        repository,
                        System::currentTimeMillis
                );

        return new WithdrawalHistoryCreateViewModelFactory(
                saveService,
                new WithdrawalHistoryDraftValidator()
        );
    }

    public WithdrawalHistoryListViewModelFactory
    provideWithdrawalHistoryListViewModelFactory() {
        return new WithdrawalHistoryListViewModelFactory(
                repository
        );
    }

    public WithdrawalHistoryDetailViewModelFactory
    provideWithdrawalHistoryDetailViewModelFactory() {
        return new WithdrawalHistoryDetailViewModelFactory(
                repository
        );
    }
}