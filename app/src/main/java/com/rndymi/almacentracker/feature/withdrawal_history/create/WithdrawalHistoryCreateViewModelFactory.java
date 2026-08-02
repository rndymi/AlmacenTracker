package com.rndymi.almacentracker.feature.withdrawal_history.create;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraftValidator;

import java.util.Objects;

public final class WithdrawalHistoryCreateViewModelFactory
        implements ViewModelProvider.Factory {

    private final WithdrawalHistorySaveService saveService;
    private final WithdrawalHistoryDraftValidator validator;

    public WithdrawalHistoryCreateViewModelFactory(
            WithdrawalHistorySaveService saveService,
            WithdrawalHistoryDraftValidator validator
    ) {
        this.saveService =
                Objects.requireNonNull(
                        saveService,
                        "saveService"
                );

        this.validator =
                Objects.requireNonNull(
                        validator,
                        "validator"
                );
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {
        if (!modelClass.isAssignableFrom(
                WithdrawalHistoryCreateViewModel.class
        )) {
            throw new IllegalArgumentException(
                    "Unknown ViewModel class"
            );
        }

        return (T)
                new WithdrawalHistoryCreateViewModel(
                        saveService,
                        validator
                );
    }
}