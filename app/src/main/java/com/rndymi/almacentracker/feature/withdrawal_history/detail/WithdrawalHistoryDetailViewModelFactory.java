package com.rndymi.almacentracker.feature.withdrawal_history.detail;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.data.repository.WithdrawalHistoryRepository;

import java.util.Objects;

public final class WithdrawalHistoryDetailViewModelFactory
        implements ViewModelProvider.Factory {

    private final WithdrawalHistoryRepository repository;

    public WithdrawalHistoryDetailViewModelFactory(
            WithdrawalHistoryRepository repository
    ) {
        this.repository =
                Objects.requireNonNull(
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
                WithdrawalHistoryDetailViewModel.class
        )) {
            throw new IllegalArgumentException(
                    "Unknown ViewModel class: "
                            + modelClass.getName()
            );
        }

        return modelClass.cast(
                new WithdrawalHistoryDetailViewModel(
                        repository
                )
        );
    }
}
