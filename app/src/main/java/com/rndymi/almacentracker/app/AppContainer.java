package com.rndymi.almacentracker.app;

import ai.onnxruntime.OrtEnvironment;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.room.Room;

import com.rndymi.almacentracker.app.di.DataManagementModule;
import com.rndymi.almacentracker.app.di.InventoryModule;
import com.rndymi.almacentracker.app.di.ReferenceListModule;
import com.rndymi.almacentracker.app.di.WithdrawalHistoryModule;
import com.rndymi.almacentracker.core.document.DocumentImageLoader;
import com.rndymi.almacentracker.data.document.onnx.OnnxModelAssetLoader;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrModelConfiguration;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeInitializer;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeProvider;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrSessionMetadataValidator;
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
import com.rndymi.almacentracker.feature.withdrawal_history.create.WithdrawalHistoryCreateViewModelFactory;
import com.rndymi.almacentracker.feature.withdrawal_history.detail.WithdrawalHistoryDetailViewModelFactory;
import com.rndymi.almacentracker.feature.withdrawal_history.list.WithdrawalHistoryListViewModelFactory;

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
    private final WithdrawalHistoryModule withdrawalHistoryModule;
    private final ExecutorService ocrExecutor;
    private final PaddleOcrRuntimeProvider paddleOcrRuntimeProvider;

    public AppContainer(Context context) {
        Context applicationContext =
                context.getApplicationContext();

        database = Room.databaseBuilder(
                applicationContext,
                AlmacenTrackerDatabase.class,
                "almacen_tracker.db"
        ).addMigrations(
                AlmacenTrackerMigrations.MIGRATION_1_2,
                AlmacenTrackerMigrations.MIGRATION_2_3,
                AlmacenTrackerMigrations.MIGRATION_3_4
        ).build();
        databaseExecutor =
                Executors.newSingleThreadExecutor();
        fileExecutor =
                Executors.newSingleThreadExecutor();
        ocrExecutor =
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
        OrtEnvironment ortEnvironment =
                OrtEnvironment.getEnvironment();

        PaddleOcrRuntimeInitializer
                paddleOcrRuntimeInitializer =
                new PaddleOcrRuntimeInitializer(
                        ortEnvironment,
                        new OnnxModelAssetLoader(
                                applicationContext.getAssets()
                        ),
                        PaddleOcrModelConfiguration.bundled(),
                        new PaddleOcrSessionMetadataValidator()
                );

        paddleOcrRuntimeProvider =
                new PaddleOcrRuntimeProvider(
                        ocrExecutor,
                        paddleOcrRuntimeInitializer
                );
        referenceListModule =
                new ReferenceListModule(
                        applicationContext,
                        warehouseItemRepository,
                        ocrExecutor,
                        paddleOcrRuntimeProvider
                );
        withdrawalHistoryModule =
                new WithdrawalHistoryModule(
                        withdrawalHistoryRepository
                );
    }

    public WithdrawalHistoryRepository
    provideWithdrawalHistoryRepository() {
        return withdrawalHistoryRepository;
    }

    public PaddleOcrRuntimeProvider
    providePaddleOcrRuntimeProvider() {
        return paddleOcrRuntimeProvider;
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

    public WithdrawalHistoryCreateViewModelFactory
    provideWithdrawalHistoryCreateViewModelFactory() {
        return withdrawalHistoryModule
                .provideWithdrawalHistoryCreateViewModelFactory();
    }

    public WithdrawalHistoryListViewModelFactory
    provideWithdrawalHistoryListViewModelFactory() {
        return withdrawalHistoryModule
                .provideWithdrawalHistoryListViewModelFactory();
    }

    public WithdrawalHistoryDetailViewModelFactory
    provideWithdrawalHistoryDetailViewModelFactory() {
        return withdrawalHistoryModule
                .provideWithdrawalHistoryDetailViewModelFactory();
    }
}
