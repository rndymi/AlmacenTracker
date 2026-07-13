package com.rndymi.almacentracker.configuration;

import android.content.Context;

import androidx.room.Room;

import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemListViewModelFactory;
import com.rndymi.almacentracker.adapter.out.persistence.room.database.AlmacenTrackerDatabase;
import com.rndymi.almacentracker.adapter.out.persistence.room.mapper.WarehouseItemPersistenceMapper;
import com.rndymi.almacentracker.adapter.out.persistence.room.repository.RoomWarehouseItemRepository;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.service.ObserveWarehouseItemsService;

public final class AppContainer {
    private final AlmacenTrackerDatabase database;
    private final WarehouseItemRepository warehouseItemRepository;
    private final ObserveWarehouseItemsUseCase observeWarehouseItemsUseCase;

    public AppContainer(Context context) {
        Context applicationContext = context.getApplicationContext();

        database = Room.databaseBuilder(
                applicationContext,
                AlmacenTrackerDatabase.class,
                "almacen_tracker.db"
        ).build();

        WarehouseItemPersistenceMapper mapper =
                new WarehouseItemPersistenceMapper();

        warehouseItemRepository = new RoomWarehouseItemRepository(
                database.warehouseItemDao(),
                mapper
        );

        observeWarehouseItemsUseCase =
                new ObserveWarehouseItemsService(
                        warehouseItemRepository
                );
    }

    public WarehouseItemListViewModelFactory
    provideWarehouseItemListViewModelFactory() {
        return new WarehouseItemListViewModelFactory(
                observeWarehouseItemsUseCase
        );
    }
}