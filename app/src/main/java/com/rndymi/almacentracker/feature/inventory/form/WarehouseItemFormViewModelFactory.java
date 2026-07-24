package com.rndymi.almacentracker.feature.inventory.form;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;

import java.util.Objects;

public final class WarehouseItemFormViewModelFactory
        implements ViewModelProvider.Factory {

    private final WarehouseItemSaveService saveService;

    private final WarehouseItemRepository repository;

    private final long warehouseItemId;

    public WarehouseItemFormViewModelFactory(
            WarehouseItemSaveService saveService,
            WarehouseItemRepository repository,
            long warehouseItemId
    ) {
        this.saveService = Objects.requireNonNull(saveService);

        this.repository = Objects.requireNonNull(repository);

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

        return modelClass.cast(
                new WarehouseItemFormViewModel(
                        saveService,
                        repository,
                        warehouseItemId
                )
        );
    }
}
