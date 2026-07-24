package com.rndymi.almacentracker.feature.inventory.list;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.feature.inventory.common.WarehouseItemDeleteService;

import java.util.Objects;

public final class WarehouseItemListViewModelFactory
        implements ViewModelProvider.Factory {

    private final WarehouseItemRepository repository;
    private final WarehouseItemDeleteService deleteService;

    public WarehouseItemListViewModelFactory(
            WarehouseItemRepository repository,
            WarehouseItemDeleteService deleteService
    ) {
        this.repository = Objects.requireNonNull(repository);

        this.deleteService = Objects.requireNonNull(deleteService);
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {
        if (modelClass.isAssignableFrom(
                WarehouseItemListViewModel.class
        )) {
            return modelClass.cast(
                    new WarehouseItemListViewModel(
                            repository,
                            deleteService
                    )
            );
        }

        throw new IllegalArgumentException(
                "Unknown ViewModel class: "
                        + modelClass.getName()
        );
    }
}
