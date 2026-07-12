package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemsUseCase;

import java.util.Objects;

public final class WarehouseItemListViewModelFactory
        implements ViewModelProvider.Factory {
    private final ObserveWarehouseItemsUseCase useCase;

    public WarehouseItemListViewModelFactory(
            ObserveWarehouseItemsUseCase useCase
    ) {
        this.useCase = Objects.requireNonNull(useCase);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {
        if (modelClass.isAssignableFrom(
                WarehouseItemListViewModel.class
        )) {
            return (T) new WarehouseItemListViewModel(useCase);
        }

        throw new IllegalArgumentException(
                "Unknown ViewModel class: " + modelClass.getName()
        );
    }
}