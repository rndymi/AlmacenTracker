package com.rndymi.almacentracker.app;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.room.Room;

import com.rndymi.almacentracker.app.di.DataManagementModule;
import com.rndymi.almacentracker.app.di.InventoryModule;
import com.rndymi.almacentracker.app.di.ReferenceListModule;
import com.rndymi.almacentracker.core.document.DocumentImageLoader;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.data.local.room.database.AlmacenTrackerDatabase;
import com.rndymi.almacentracker.data.local.room.database.AlmacenTrackerMigrations;
import com.rndymi.almacentracker.data.local.room.mapper.WarehouseItemRoomMapper;
import com.rndymi.almacentracker.data.local.room.mapper.WithdrawalHistoryRoomMapper;
import com.rndymi.almacentracker.data.repository.RoomWarehouseItemRepository;
import com.rndymi.almacentracker.data.repository.RoomWithdrawalHistoryRepository;
import com.rndymi.almacentracker.data.repository.WithdrawalHistoryRepository;
import com.rndymi.almacentracker.feature.data_management.common.DataManagementViewModelFactory;
import com.rndymi.almacentracker.feature.inventory.detail.WarehouseItemDetailViewModelFactory;
import com.rndymi.almacentracker.feature.inventory.form.WarehouseItemFormViewModelFactory;
import com.rndymi.almacentracker.feature.inventory.list.WarehouseItemListViewModelFactory;
import com.rndymi.almacentracker.feature.reference_list.capture.ReferenceListCaptureViewModelFactory;
import com.rndymi.almacentracker.feature.reference_list.location.ReferenceListLocationViewModelFactory;
import com.rndymi.almacentracker.feature.reference_list.review.ReferenceListReviewViewModelFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppContainer {

    private final AlmacenTrackerDatabase database;
    private final ExecutorService databaseExecutor;
    private final ExecutorService fileExecutor;
    private final WarehouseItemRepository warehouseItemRepository;
    private final WithdrawalHistoryRepository withdrawalHistoryRepository;
    private final InventoryModule inventoryModule;
    private final DataManagementModule dataManagementModule;
    private final ReferenceListModule referenceListModule;

    public AppContainer(Context context) {
        Context applicationContext =
                context.getApplicationContext();

        database = Room.databaseBuilder(
                applicationContext,
                AlmacenTrackerDatabase.class,
                "almacen_tracker.db"
        ).addMigrations(
                AlmacenTrackerMigrations.MIGRATION_1_2
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
        withdrawalHistoryRepository =
                new RoomWithdrawalHistoryRepository(
                        database.withdrawalHistoryDao(),
                        new WithdrawalHistoryRoomMapper(),
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
        referenceListModule =
                new ReferenceListModule(
                        applicationContext,
                        warehouseItemRepository
                );
    }

    public WithdrawalHistoryRepository
    provideWithdrawalHistoryRepository() {
        return withdrawalHistoryRepository;
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

    public ReferenceListCaptureViewModelFactory
    provideReferenceListCaptureViewModelFactory() {
        return referenceListModule
                .provideReferenceListCaptureViewModelFactory();
    }

    public ReferenceListReviewViewModelFactory
    provideReferenceListReviewViewModelFactory() {
        return referenceListModule
                .provideReferenceListReviewViewModelFactory();
    }

    public DocumentImageLoader<Bitmap>
    provideDocumentImageLoader() {
        return referenceListModule
                .provideDocumentImageLoader();
    }

    public ReferenceListLocationViewModelFactory
    provideReferenceListLocationViewModelFactory() {
        return inventoryModule
                .provideReferenceListLocationViewModelFactory();
    }
}
