package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.application.port.in.FilterWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemFilterOptionsUseCase;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemsUseCase;

import java.util.Objects;

public final class WarehouseItemListViewModelFactory
        implements ViewModelProvider.Factory {

    private final ObserveWarehouseItemsUseCase observeUseCase;
    private final FilterWarehouseItemsUseCase filterUseCase;
    private final ObserveWarehouseItemFilterOptionsUseCase
            observeFilterOptionsUseCase;

    public WarehouseItemListViewModelFactory(
            ObserveWarehouseItemsUseCase observeUseCase,
            FilterWarehouseItemsUseCase filterUseCase,
            ObserveWarehouseItemFilterOptionsUseCase
                    observeFilterOptionsUseCase
    ) {
        this.observeUseCase =
                Objects.requireNonNull(observeUseCase);

        this.filterUseCase =
                Objects.requireNonNull(filterUseCase);

        this.observeFilterOptionsUseCase =
                Objects.requireNonNull(
                        observeFilterOptionsUseCase
                );
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
                    filterUseCase,
                    observeFilterOptionsUseCase
            );
        }

        throw new IllegalArgumentException(
                "Unknown ViewModel class: "
                        + modelClass.getName()
        );
    }
}