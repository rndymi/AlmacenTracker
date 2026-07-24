package com.rndymi.almacentracker.app;

import android.content.Context;

import androidx.room.Room;

import com.rndymi.almacentracker.app.di.DataManagementModule;
import com.rndymi.almacentracker.app.di.InventoryModule;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.data.local.room.database.AlmacenTrackerDatabase;
import com.rndymi.almacentracker.data.local.room.mapper.WarehouseItemRoomMapper;
import com.rndymi.almacentracker.data.repository.RoomWarehouseItemRepository;
import com.rndymi.almacentracker.feature.data_management.common.DataManagementViewModelFactory;
import com.rndymi.almacentracker.feature.inventory.detail.WarehouseItemDetailViewModelFactory;
import com.rndymi.almacentracker.feature.inventory.form.WarehouseItemFormViewModelFactory;
import com.rndymi.almacentracker.feature.inventory.list.WarehouseItemListViewModelFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppContainer {

    private final AlmacenTrackerDatabase database;
    private final ExecutorService databaseExecutor;
    private final ExecutorService fileExecutor;
    private final WarehouseItemRepository warehouseItemRepository;
    private final InventoryModule inventoryModule;
    private final DataManagementModule dataManagementModule;

    public AppContainer(Context context) {
        Context applicationContext =
                context.getApplicationContext();

        database = Room.databaseBuilder(
                applicationContext,
                AlmacenTrackerDatabase.class,
                "almacen_tracker.db"
        ).build();
        databaseExecutor =
                Executors.newSingleThreadExecutor();
        fileExecutor =
                Executors.newSingleThreadExecutor();
        warehouseItemRepository =
                new RoomWarehouseItemRepository(
                        database.warehouseItemDao(),
                        new WarehouseItemRoomMapper(),
                        databaseExecutor
                );

        inventoryModule =
                new InventoryModule(warehouseItemRepository);
        dataManagementModule =
                new DataManagementModule(
                        applicationContext,
                        warehouseItemRepository,
                        fileExecutor
                );
    }

    public WarehouseItemDetailViewModelFactory
    provideWarehouseItemDetailViewModelFactory(
            long warehouseItemId
    ) {
        return inventoryModule
                .provideWarehouseItemDetailViewModelFactory(
                        warehouseItemId
                );
    }

    public WarehouseItemListViewModelFactory
    provideWarehouseItemListViewModelFactory() {
        return inventoryModule
                .provideWarehouseItemListViewModelFactory();
    }

    public WarehouseItemFormViewModelFactory
    provideWarehouseItemFormViewModelFactory(
            long warehouseItemId
    ) {
        return inventoryModule
                .provideWarehouseItemFormViewModelFactory(
                        warehouseItemId
                );
    }

    public DataManagementViewModelFactory
    provideDataManagementViewModelFactory() {
        return dataManagementModule
                .provideDataManagementViewModelFactory();
    }
}
