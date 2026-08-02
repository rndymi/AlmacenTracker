package com.rndymi.almacentracker.feature.withdrawal_history.create;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraftValidator;

public final class WithdrawalHistoryCreateViewModelFactory
        implements ViewModelProvider.Factory {

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
                        new WithdrawalHistoryDraftValidator()
                );
    }
}
