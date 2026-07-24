package com.rndymi.almacentracker.feature.inventory.detail;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.feature.inventory.common.WarehouseItemDeleteService;

import java.util.Objects;

public final class WarehouseItemDetailViewModelFactory
        implements ViewModelProvider.Factory {

    private final WarehouseItemRepository repository;

    private final WarehouseItemDeleteService deleteService;

    private final long warehouseItemId;

    public WarehouseItemDetailViewModelFactory(
            WarehouseItemRepository repository,
            WarehouseItemDeleteService deleteService,
            long warehouseItemId
    ) {
        this.repository = Objects.requireNonNull(repository);

        this.deleteService = Objects.requireNonNull(deleteService);

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
                        repository,
                        deleteService,
                        warehouseItemId
                )
        );
    }
}
