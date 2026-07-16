package com.rndymi.almacentracker.configuration;

import android.content.Context;

import androidx.room.Room;

import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemListViewModelFactory;
import com.rndymi.almacentracker.adapter.out.persistence.room.database.AlmacenTrackerDatabase;
import com.rndymi.almacentracker.adapter.out.persistence.room.mapper.WarehouseItemPersistenceMapper;
import com.rndymi.almacentracker.adapter.out.persistence.room.repository.RoomWarehouseItemRepository;
import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.service.CreateWarehouseItemService;
import com.rndymi.almacentracker.application.service.ObserveWarehouseItemsService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppContainer {
    private final AlmacenTrackerDatabase database;
    private final ExecutorService databaseExecutor;
    private final WarehouseItemRepository warehouseItemRepository;
    private final ObserveWarehouseItemsUseCase observeWarehouseItemsUseCase;
    private final CreateWarehouseItemUseCase createWarehouseItemUseCase;

    public AppContainer(Context context) {
        Context applicationContext = context.getApplicationContext();

        database = Room.databaseBuilder(
                applicationContext,
                AlmacenTrackerDatabase.class,
                "almacen_tracker.db"
        ).build();

        databaseExecutor = Executors.newSingleThreadExecutor();

        WarehouseItemPersistenceMapper mapper =
                new WarehouseItemPersistenceMapper();

        warehouseItemRepository = new RoomWarehouseItemRepository(
                database.warehouseItemDao(),
                mapper,
                databaseExecutor
        );

        observeWarehouseItemsUseCase =
                new ObserveWarehouseItemsService(
                        warehouseItemRepository
                );

        createWarehouseItemUseCase =
                new CreateWarehouseItemService(
                        warehouseItemRepository,
                        System::currentTimeMillis
                );
    }

    public WarehouseItemListViewModelFactory
    provideWarehouseItemListViewModelFactory() {
        return new WarehouseItemListViewModelFactory(
                observeWarehouseItemsUseCase
        );
    }
}