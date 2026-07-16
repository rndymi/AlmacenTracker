package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemUseCase;

import java.util.Objects;

public final class WarehouseItemFormViewModelFactory
        implements ViewModelProvider.Factory {

    private final CreateWarehouseItemUseCase
            createWarehouseItemUseCase;

    public WarehouseItemFormViewModelFactory(
            CreateWarehouseItemUseCase
                    createWarehouseItemUseCase
    ) {
        this.createWarehouseItemUseCase =
                Objects.requireNonNull(
                        createWarehouseItemUseCase
                );
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {
        if (!modelClass.isAssignableFrom(
                WarehouseItemFormViewModel.class
        )) {
            throw new IllegalArgumentException(
                    "Unknown ViewModel class: "
                            + modelClass.getName()
            );
        }

        @SuppressWarnings("unchecked")
        T viewModel = (T)
                new WarehouseItemFormViewModel(
                        createWarehouseItemUseCase
                );

        return viewModel;
    }
}