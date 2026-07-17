package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.application.port.in.DeleteWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.in.GetWarehouseItemDetailUseCase;

import java.util.Objects;

public final class WarehouseItemDetailViewModelFactory
        implements ViewModelProvider.Factory {

    private final GetWarehouseItemDetailUseCase
            getWarehouseItemDetailUseCase;

    private final DeleteWarehouseItemUseCase
            deleteWarehouseItemUseCase;

    private final long warehouseItemId;

    public WarehouseItemDetailViewModelFactory(
            GetWarehouseItemDetailUseCase
                    getWarehouseItemDetailUseCase,
            DeleteWarehouseItemUseCase
                    deleteWarehouseItemUseCase,
            long warehouseItemId
    ) {
        this.getWarehouseItemDetailUseCase =
                Objects.requireNonNull(
                        getWarehouseItemDetailUseCase
                );

        this.deleteWarehouseItemUseCase =
                Objects.requireNonNull(
                        deleteWarehouseItemUseCase
                );

        this.warehouseItemId = warehouseItemId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {
        if (!modelClass.isAssignableFrom(
                WarehouseItemDetailViewModel.class
        )) {
            throw new IllegalArgumentException(
                    "Unknown ViewModel class: "
                            + modelClass.getName()
            );
        }

        return modelClass.cast(
                new WarehouseItemDetailViewModel(
                        getWarehouseItemDetailUseCase,
                        deleteWarehouseItemUseCase,
                        warehouseItemId
                )
        );
    }
}