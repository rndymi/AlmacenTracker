package com.rndymi.almacentracker.feature.withdrawal_history.list;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.data.repository.WithdrawalHistoryRepository;

import java.util.Objects;

public final class WithdrawalHistoryListViewModelFactory
        implements ViewModelProvider.Factory {

    private final WithdrawalHistoryRepository repository;

    public WithdrawalHistoryListViewModelFactory(
            WithdrawalHistoryRepository repository
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "repository"
        );
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {
        if (!modelClass.isAssignableFrom(
                WithdrawalHistoryListViewModel.class
        )) {
            throw new IllegalArgumentException(
                    "Unknown ViewModel class"
            );
        }

        return modelClass.cast(
                new WithdrawalHistoryListViewModel(
                        repository
                )
        );
    }
}
