package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.in.GetWarehouseItemDetailUseCase;
import com.rndymi.almacentracker.application.port.in.UpdateWarehouseItemUseCase;

import java.util.Objects;

public final class WarehouseItemFormViewModelFactory
        implements ViewModelProvider.Factory {

    private final CreateWarehouseItemUseCase
            createWarehouseItemUseCase;

    private final UpdateWarehouseItemUseCase
            updateWarehouseItemUseCase;

    private final GetWarehouseItemDetailUseCase
            getWarehouseItemDetailUseCase;

    private final long warehouseItemId;

    public WarehouseItemFormViewModelFactory(
            CreateWarehouseItemUseCase
                    createWarehouseItemUseCase,
            UpdateWarehouseItemUseCase
                    updateWarehouseItemUseCase,
            GetWarehouseItemDetailUseCase
                    getWarehouseItemDetailUseCase,
            long warehouseItemId
    ) {
        this.createWarehouseItemUseCase =
                Objects.requireNonNull(
                        createWarehouseItemUseCase
                );

        this.updateWarehouseItemUseCase =
                Objects.requireNonNull(
                        updateWarehouseItemUseCase
                );

        this.getWarehouseItemDetailUseCase =
                Objects.requireNonNull(
                        getWarehouseItemDetailUseCase
                );

        this.warehouseItemId = warehouseItemId;
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
                        createWarehouseItemUseCase,
                        updateWarehouseItemUseCase,
                        getWarehouseItemDetailUseCase,
                        warehouseItemId
                );

        return viewModel;
    }
}