package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.SearchWarehouseItemsUseCase;

import java.util.Objects;

public final class WarehouseItemListViewModelFactory
        implements ViewModelProvider.Factory {

    private final ObserveWarehouseItemsUseCase observeUseCase;
    private final SearchWarehouseItemsUseCase searchUseCase;

    public WarehouseItemListViewModelFactory(
            ObserveWarehouseItemsUseCase observeUseCase,
            SearchWarehouseItemsUseCase searchUseCase
    ) {
        this.observeUseCase =
                Objects.requireNonNull(observeUseCase);

        this.searchUseCase =
                Objects.requireNonNull(searchUseCase);
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
            return (T) new WarehouseItemListViewModel(
                    observeUseCase,
                    searchUseCase
            );
        }

        throw new IllegalArgumentException(
                "Unknown ViewModel class: "
                        + modelClass.getName()
        );
    }
}