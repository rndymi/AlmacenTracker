package com.rndymi.almacentracker.configuration;

import android.content.Context;

import androidx.room.Room;

import com.rndymi.almacentracker.adapter.in.ui.viewmodel.DataManagementViewModelFactory;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemDetailViewModelFactory;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemFormViewModelFactory;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemListViewModelFactory;
import com.rndymi.almacentracker.adapter.out.file.backup.csv.AndroidWarehouseBackupDocumentExporter;
import com.rndymi.almacentracker.adapter.out.file.backup.csv.WarehouseBackupCsvCodec;
import com.rndymi.almacentracker.adapter.out.file.backup.csv.WarehouseBackupCsvMapper;
import com.rndymi.almacentracker.adapter.out.file.csv.AndroidCsvDocumentExporter;
import com.rndymi.almacentracker.adapter.out.file.csv.AndroidCsvShareFileGateway;
import com.rndymi.almacentracker.adapter.out.file.csv.AndroidCsvDocumentReader;
import com.rndymi.almacentracker.adapter.out.file.csv.WarehouseItemCsvCodec;
import com.rndymi.almacentracker.adapter.out.file.csv.WarehouseItemCsvMapper;
import com.rndymi.almacentracker.adapter.out.persistence.room.database.AlmacenTrackerDatabase;
import com.rndymi.almacentracker.adapter.out.persistence.room.mapper.WarehouseItemPersistenceMapper;
import com.rndymi.almacentracker.adapter.out.persistence.room.repository.RoomWarehouseItemRepository;
import com.rndymi.almacentracker.application.port.in.DeleteWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.in.DeleteWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.CreateWarehouseBackupUseCase;
import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.in.ExportWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.FilterWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.GetWarehouseItemDetailUseCase;
import com.rndymi.almacentracker.application.port.in.ImportWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemFilterOptionsUseCase;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.SearchWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.ShareWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.UpdateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.service.CreateWarehouseBackupService;
import com.rndymi.almacentracker.application.service.CreateWarehouseItemService;
import com.rndymi.almacentracker.application.service.DeleteWarehouseItemService;
import com.rndymi.almacentracker.application.service.DeleteWarehouseItemsService;
import com.rndymi.almacentracker.application.service.ExportWarehouseItemsService;
import com.rndymi.almacentracker.application.service.FilterWarehouseItemsService;
import com.rndymi.almacentracker.application.service.GetWarehouseItemDetailService;
import com.rndymi.almacentracker.application.service.ImportWarehouseItemsService;
import com.rndymi.almacentracker.application.service.ObserveWarehouseItemFilterOptionsService;
import com.rndymi.almacentracker.application.service.ObserveWarehouseItemsService;
import com.rndymi.almacentracker.application.service.SearchWarehouseItemsService;
import com.rndymi.almacentracker.application.service.ShareWarehouseItemsService;
import com.rndymi.almacentracker.application.service.UpdateWarehouseItemService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppContainer {

    private final AlmacenTrackerDatabase database;
    private final ExecutorService databaseExecutor;
    private final ExecutorService fileExecutor;

    private final WarehouseItemRepository warehouseItemRepository;
    private final ObserveWarehouseItemsUseCase observeWarehouseItemsUseCase;
    private final SearchWarehouseItemsUseCase searchWarehouseItemsUseCase;
    private final FilterWarehouseItemsUseCase filterWarehouseItemsUseCase;
    private final ObserveWarehouseItemFilterOptionsUseCase observeFilterOptionsUseCase;
    private final CreateWarehouseItemUseCase createWarehouseItemUseCase;
    private final UpdateWarehouseItemUseCase updateWarehouseItemUseCase;
    private final DeleteWarehouseItemUseCase deleteWarehouseItemUseCase;
    private final DeleteWarehouseItemsUseCase deleteWarehouseItemsUseCase;
    private final GetWarehouseItemDetailUseCase getWarehouseItemDetailUseCase;
    private final ExportWarehouseItemsUseCase exportWarehouseItemsUseCase;
    private final ShareWarehouseItemsUseCase shareWarehouseItemsUseCase;
    private final ImportWarehouseItemsUseCase importWarehouseItemsUseCase;
    private final CreateWarehouseBackupUseCase createWarehouseBackupUseCase;

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

        WarehouseItemPersistenceMapper mapper =
                new WarehouseItemPersistenceMapper();

        warehouseItemRepository =
                new RoomWarehouseItemRepository(
                        database.warehouseItemDao(),
                        mapper,
                        databaseExecutor
                );

        observeWarehouseItemsUseCase =
                new ObserveWarehouseItemsService(
                        warehouseItemRepository
                );

        searchWarehouseItemsUseCase =
                new SearchWarehouseItemsService(
                        warehouseItemRepository
                );

        filterWarehouseItemsUseCase =
                new FilterWarehouseItemsService(
                        warehouseItemRepository
                );

        observeFilterOptionsUseCase =
                new ObserveWarehouseItemFilterOptionsService(
                        warehouseItemRepository
                );

        createWarehouseItemUseCase =
                new CreateWarehouseItemService(
                        warehouseItemRepository,
                        System::currentTimeMillis
                );

        updateWarehouseItemUseCase =
                new UpdateWarehouseItemService(
                        warehouseItemRepository,
                        System::currentTimeMillis
                );

        deleteWarehouseItemUseCase =
                new DeleteWarehouseItemService(
                        warehouseItemRepository
                );

        deleteWarehouseItemsUseCase =
                new DeleteWarehouseItemsService(
                        warehouseItemRepository
                );

        getWarehouseItemDetailUseCase =
                new GetWarehouseItemDetailService(
                        warehouseItemRepository
                );

        WarehouseItemCsvMapper csvMapper =
                new WarehouseItemCsvMapper();

        WarehouseItemCsvCodec csvCodec =
                new WarehouseItemCsvCodec(csvMapper);

        AndroidCsvDocumentReader csvReader =
                new AndroidCsvDocumentReader(
                        applicationContext.getContentResolver(),
                        csvCodec,
                        fileExecutor
                );

        importWarehouseItemsUseCase =
                new ImportWarehouseItemsService(
                        csvReader,
                        warehouseItemRepository,
                        System::currentTimeMillis
                );

        AndroidCsvDocumentExporter csvExporter =
                new AndroidCsvDocumentExporter(
                        applicationContext.getContentResolver(),
                        csvCodec,
                        fileExecutor
                );

        exportWarehouseItemsUseCase =
                new ExportWarehouseItemsService(
                        warehouseItemRepository,
                        csvExporter
                );

        AndroidCsvShareFileGateway shareFileGateway =
                new AndroidCsvShareFileGateway(
                        applicationContext,
                        csvCodec,
                        fileExecutor,
                        applicationContext.getPackageName()
                                + ".fileprovider"
                );

        shareWarehouseItemsUseCase =
                new ShareWarehouseItemsService(
                        warehouseItemRepository,
                        shareFileGateway,
                        () -> "almacentracker-share-"
                                + LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern(
                                        "yyyy-MM-dd-HHmmss"
                                )
                        )
                                + ".csv"
                );

        WarehouseBackupCsvMapper backupCsvMapper =
                new WarehouseBackupCsvMapper();

        WarehouseBackupCsvCodec backupCsvCodec =
                new WarehouseBackupCsvCodec(
                        backupCsvMapper
                );

        AndroidWarehouseBackupDocumentExporter
                backupDocumentExporter =
                new AndroidWarehouseBackupDocumentExporter(
                        applicationContext.getContentResolver(),
                        backupCsvCodec,
                        fileExecutor
                );

        createWarehouseBackupUseCase =
                new CreateWarehouseBackupService(
                        warehouseItemRepository,
                        backupDocumentExporter
                );
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

    public DataManagementViewModelFactory
    provideDataManagementViewModelFactory() {
        return new DataManagementViewModelFactory(
                exportWarehouseItemsUseCase,
                shareWarehouseItemsUseCase,
                importWarehouseItemsUseCase,
                createWarehouseBackupUseCase,
                () -> "almacentracker-export-"
                        + LocalDate.now().format(
                        DateTimeFormatter.ISO_LOCAL_DATE
                )
                        + ".csv",
                () -> "almacentracker-backup-"
                        + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd-HHmmss"
                        )
                )
                        + ".csv"
        );
    }
}