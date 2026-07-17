package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.application.port.in.GetWarehouseItemDetailUseCase;

import java.util.Objects;

public final class WarehouseItemDetailViewModelFactory
        implements ViewModelProvider.Factory {
    private final GetWarehouseItemDetailUseCase useCase;
    private final long warehouseItemId;

    public WarehouseItemDetailViewModelFactory(
            GetWarehouseItemDetailUseCase useCase,
            long warehouseItemId
    ) {
        this.useCase = Objects.requireNonNull(useCase);
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
                        useCase,
                        warehouseItemId
                )
        );
    }
}
