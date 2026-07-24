package com.rndymi.almacentracker.app.di;

import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.feature.inventory.common.WarehouseItemDeleteService;
import com.rndymi.almacentracker.feature.inventory.detail.WarehouseItemDetailViewModelFactory;
import com.rndymi.almacentracker.feature.inventory.form.WarehouseItemFormViewModelFactory;
import com.rndymi.almacentracker.feature.inventory.form.WarehouseItemSaveService;
import com.rndymi.almacentracker.feature.inventory.list.WarehouseItemListViewModelFactory;

import java.util.Objects;

public final class InventoryModule {

    private final WarehouseItemRepository repository;
    private final WarehouseItemSaveService saveService;
    private final WarehouseItemDeleteService deleteService;
    public InventoryModule(
            WarehouseItemRepository warehouseItemRepository
    ) {
        repository = Objects.requireNonNull(
                warehouseItemRepository
        );
        saveService = new WarehouseItemSaveService(
                repository,
                System::currentTimeMillis
        );
        deleteService = new WarehouseItemDeleteService(
                repository
        );
    }

    public WarehouseItemDetailViewModelFactory
    provideWarehouseItemDetailViewModelFactory(
            long warehouseItemId
    ) {
        return new WarehouseItemDetailViewModelFactory(
                repository,
                deleteService,
                warehouseItemId
        );
    }

    public WarehouseItemListViewModelFactory
    provideWarehouseItemListViewModelFactory() {
        return new WarehouseItemListViewModelFactory(
                repository,
                deleteService
        );
    }

    public WarehouseItemFormViewModelFactory
    provideWarehouseItemFormViewModelFactory(
            long warehouseItemId
    ) {
        return new WarehouseItemFormViewModelFactory(
                saveService,
                repository,
                warehouseItemId
        );
    }
}
