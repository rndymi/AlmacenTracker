package com.rndymi.almacentracker.feature.inventory.form;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.rule.WarehouseItemNormalizer;

import java.util.Objects;

public final class WarehouseItemFormViewModelFactory
        implements ViewModelProvider.Factory {

    private final WarehouseItemSaveService saveService;
    private final WarehouseItemRepository repository;
    private final WarehouseItemNormalizer normalizer;
    private final long warehouseItemId;

    public WarehouseItemFormViewModelFactory(
            WarehouseItemSaveService saveService,
            WarehouseItemRepository repository,
            WarehouseItemNormalizer normalizer,
            long warehouseItemId
    ) {
        this.saveService =
                Objects.requireNonNull(saveService);

        this.repository =
                Objects.requireNonNull(repository);

        this.normalizer =
                Objects.requireNonNull(normalizer);

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
                        normalizer,
                        warehouseItemId
                )
        );
    }
}