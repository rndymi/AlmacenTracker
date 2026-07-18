package com.rndymi.almacentracker.configuration;

import android.content.Context;

import androidx.room.Room;

import com.rndymi.almacentracker.adapter.in.ui.viewmodel.DataManagementViewModelFactory;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemDetailViewModelFactory;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemFormViewModelFactory;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemListViewModelFactory;
import com.rndymi.almacentracker.adapter.out.file.csv.AndroidCsvDocumentExporter;
import com.rndymi.almacentracker.adapter.out.file.csv.WarehouseItemCsvCodec;
import com.rndymi.almacentracker.adapter.out.file.csv.WarehouseItemCsvMapper;
import com.rndymi.almacentracker.adapter.out.persistence.room.database.AlmacenTrackerDatabase;
import com.rndymi.almacentracker.adapter.out.persistence.room.mapper.WarehouseItemPersistenceMapper;
import com.rndymi.almacentracker.adapter.out.persistence.room.repository.RoomWarehouseItemRepository;
import com.rndymi.almacentracker.application.port.in.DeleteWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.in.DeleteWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.in.ExportWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.FilterWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.GetWarehouseItemDetailUseCase;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemFilterOptionsUseCase;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.SearchWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.UpdateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.service.DeleteWarehouseItemService;
import com.rndymi.almacentracker.application.service.DeleteWarehouseItemsService;
import com.rndymi.almacentracker.application.service.CreateWarehouseItemService;
import com.rndymi.almacentracker.application.service.ExportWarehouseItemsService;
import com.rndymi.almacentracker.application.service.FilterWarehouseItemsService;
import com.rndymi.almacentracker.application.service.GetWarehouseItemDetailService;
import com.rndymi.almacentracker.application.service.ObserveWarehouseItemFilterOptionsService;
import com.rndymi.almacentracker.application.service.ObserveWarehouseItemsService;
import com.rndymi.almacentracker.application.service.SearchWarehouseItemsService;
import com.rndymi.almacentracker.application.service.UpdateWarehouseItemService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppContainer {

    private final AlmacenTrackerDatabase database;
    private final ExecutorService databaseExecutor;

    private final WarehouseItemRepository
            warehouseItemRepository;

    private final ObserveWarehouseItemsUseCase
            observeWarehouseItemsUseCase;

    private final SearchWarehouseItemsUseCase
            searchWarehouseItemsUseCase;

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

    private final ExportWarehouseItemsUseCase
            exportWarehouseItemsUseCase;


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

        AndroidCsvDocumentExporter csvExporter =
                new AndroidCsvDocumentExporter(
                        applicationContext.getContentResolver(),
                        csvCodec,
                        databaseExecutor
                );

        exportWarehouseItemsUseCase =
                new ExportWarehouseItemsService(
                        warehouseItemRepository,
                        csvExporter
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
                () -> "almacentracker-export-"
                        + LocalDate.now().format(
                        DateTimeFormatter.ISO_LOCAL_DATE
                )
                        + ".csv"
        );
    }
}