package com.rndymi.almacentracker.app.di;

import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.in.DeleteWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.in.DeleteWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.FilterWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.GetWarehouseItemDetailUseCase;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemFilterOptionsUseCase;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.UpdateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.service.CreateWarehouseItemService;
import com.rndymi.almacentracker.application.service.DeleteWarehouseItemService;
import com.rndymi.almacentracker.application.service.DeleteWarehouseItemsService;
import com.rndymi.almacentracker.application.service.FilterWarehouseItemsService;
import com.rndymi.almacentracker.application.service.GetWarehouseItemDetailService;
import com.rndymi.almacentracker.application.service.ObserveWarehouseItemFilterOptionsService;
import com.rndymi.almacentracker.application.service.ObserveWarehouseItemsService;
import com.rndymi.almacentracker.application.service.UpdateWarehouseItemService;
import com.rndymi.almacentracker.feature.inventory.detail.WarehouseItemDetailViewModelFactory;
import com.rndymi.almacentracker.feature.inventory.form.WarehouseItemFormViewModelFactory;
import com.rndymi.almacentracker.feature.inventory.list.WarehouseItemListViewModelFactory;

import java.util.Objects;

public final class InventoryModule {

    private final ObserveWarehouseItemsUseCase
            observeWarehouseItemsUseCase;
    private final FilterWarehouseItemsUseCase
            filterWarehouseItemsUseCase;
    private final ObserveWarehouseItemFilterOptionsUseCase
            observeFilterOptionsUseCase;
    private final CreateWarehouseItemUseCase
            createWarehouseItemUseCase;
    private final UpdateWarehouseItemUseCase
            updateWarehouseItemUseCase;
    private final DeleteWarehouseItemUseCase
            deleteWarehouseItemUseCase;
    private final DeleteWarehouseItemsUseCase
            deleteWarehouseItemsUseCase;
    private final GetWarehouseItemDetailUseCase
            getWarehouseItemDetailUseCase;

    public InventoryModule(
            WarehouseItemRepository warehouseItemRepository
    ) {
        WarehouseItemRepository repository =
                Objects.requireNonNull(warehouseItemRepository);

        observeWarehouseItemsUseCase =
                new ObserveWarehouseItemsService(repository);
        filterWarehouseItemsUseCase =
                new FilterWarehouseItemsService(repository);
        observeFilterOptionsUseCase =
                new ObserveWarehouseItemFilterOptionsService(
                        repository
                );
        createWarehouseItemUseCase =
                new CreateWarehouseItemService(
                        repository,
                        System::currentTimeMillis
                );
        updateWarehouseItemUseCase =
                new UpdateWarehouseItemService(
                        repository,
                        System::currentTimeMillis
                );
        deleteWarehouseItemUseCase =
                new DeleteWarehouseItemService(repository);
        deleteWarehouseItemsUseCase =
                new DeleteWarehouseItemsService(repository);
        getWarehouseItemDetailUseCase =
                new GetWarehouseItemDetailService(repository);
    }

    public WarehouseItemDetailViewModelFactory
    provideWarehouseItemDetailViewModelFactory(
            long warehouseItemId
    ) {
        return new WarehouseItemDetailViewModelFactory(
                getWarehouseItemDetailUseCase,
                deleteWarehouseItemUseCase,
                warehouseItemId
        );
    }

    public WarehouseItemListViewModelFactory
    provideWarehouseItemListViewModelFactory() {
        return new WarehouseItemListViewModelFactory(
                observeWarehouseItemsUseCase,
                filterWarehouseItemsUseCase,
                observeFilterOptionsUseCase,
                deleteWarehouseItemsUseCase
        );
    }

    public WarehouseItemFormViewModelFactory
    provideWarehouseItemFormViewModelFactory(
            long warehouseItemId
    ) {
        return new WarehouseItemFormViewModelFactory(
                createWarehouseItemUseCase,
                updateWarehouseItemUseCase,
                getWarehouseItemDetailUseCase,
                warehouseItemId
        );
    }
}
