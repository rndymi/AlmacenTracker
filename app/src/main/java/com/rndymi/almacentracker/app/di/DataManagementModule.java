package com.rndymi.almacentracker.app.di;

import android.content.Context;

import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.data.file.csv.backup.WarehouseBackupCsvCodec;
import com.rndymi.almacentracker.data.file.csv.backup.WarehouseBackupCsvMapper;
import com.rndymi.almacentracker.data.file.csv.exchange.WarehouseItemCsvCodec;
import com.rndymi.almacentracker.data.file.csv.exchange.WarehouseItemCsvMapper;
import com.rndymi.almacentracker.data.file.document.AndroidCsvDocumentExporter;
import com.rndymi.almacentracker.data.file.document.AndroidCsvDocumentReader;
import com.rndymi.almacentracker.data.file.document.AndroidWarehouseBackupDocumentExporter;
import com.rndymi.almacentracker.data.file.document.AndroidWarehouseBackupDocumentReader;
import com.rndymi.almacentracker.data.file.share.AndroidCsvShareFileGateway;
import com.rndymi.almacentracker.feature.data_management.backup.create.CreateWarehouseBackupService;
import com.rndymi.almacentracker.feature.data_management.backup.create.CreateWarehouseBackupUseCase;
import com.rndymi.almacentracker.feature.data_management.backup.restore.RestoreWarehouseBackupService;
import com.rndymi.almacentracker.feature.data_management.backup.restore.RestoreWarehouseBackupUseCase;
import com.rndymi.almacentracker.feature.data_management.backup.restore.ValidateWarehouseBackupService;
import com.rndymi.almacentracker.feature.data_management.backup.restore.ValidateWarehouseBackupUseCase;
import com.rndymi.almacentracker.feature.data_management.common.DataManagementViewModelFactory;
import com.rndymi.almacentracker.feature.data_management.export.ExportWarehouseItemsService;
import com.rndymi.almacentracker.feature.data_management.export.ExportWarehouseItemsUseCase;
import com.rndymi.almacentracker.feature.data_management.import_data.ImportWarehouseItemsService;
import com.rndymi.almacentracker.feature.data_management.import_data.ImportWarehouseItemsUseCase;
import com.rndymi.almacentracker.feature.data_management.share.ShareWarehouseItemsService;
import com.rndymi.almacentracker.feature.data_management.share.ShareWarehouseItemsUseCase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.Executor;

public final class DataManagementModule {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");

    private final ExportWarehouseItemsUseCase
            exportWarehouseItemsUseCase;
    private final ShareWarehouseItemsUseCase
            shareWarehouseItemsUseCase;
    private final ImportWarehouseItemsUseCase
            importWarehouseItemsUseCase;
    private final CreateWarehouseBackupUseCase
            createWarehouseBackupUseCase;
    private final ValidateWarehouseBackupUseCase
            validateWarehouseBackupUseCase;
    private final RestoreWarehouseBackupUseCase
            restoreWarehouseBackupUseCase;

    public DataManagementModule(
            Context context,
            WarehouseItemRepository warehouseItemRepository,
            Executor fileExecutor
    ) {
        Context applicationContext =
                Objects.requireNonNull(context)
                        .getApplicationContext();
        WarehouseItemRepository repository =
                Objects.requireNonNull(warehouseItemRepository);
        Executor executor =
                Objects.requireNonNull(fileExecutor);

        WarehouseItemCsvCodec csvCodec =
                new WarehouseItemCsvCodec(
                        new WarehouseItemCsvMapper()
                );
        AndroidCsvDocumentReader csvReader =
                new AndroidCsvDocumentReader(
                        applicationContext.getContentResolver(),
                        csvCodec,
                        executor
                );
        importWarehouseItemsUseCase =
                new ImportWarehouseItemsService(
                        csvReader,
                        repository,
                        System::currentTimeMillis
                );

        AndroidCsvDocumentExporter csvExporter =
                new AndroidCsvDocumentExporter(
                        applicationContext.getContentResolver(),
                        csvCodec,
                        executor
                );
        exportWarehouseItemsUseCase =
                new ExportWarehouseItemsService(
                        repository,
                        csvExporter
                );

        AndroidCsvShareFileGateway shareFileGateway =
                new AndroidCsvShareFileGateway(
                        applicationContext,
                        csvCodec,
                        executor,
                        applicationContext.getPackageName()
                                + ".fileprovider"
                );
        shareWarehouseItemsUseCase =
                new ShareWarehouseItemsService(
                        repository,
                        shareFileGateway,
                        () -> "almacentracker-share-"
                                + timestamp()
                                + ".csv"
                );

        WarehouseBackupCsvCodec backupCsvCodec =
                new WarehouseBackupCsvCodec(
                        new WarehouseBackupCsvMapper()
                );
        AndroidWarehouseBackupDocumentExporter
                backupDocumentExporter =
                new AndroidWarehouseBackupDocumentExporter(
                        applicationContext.getContentResolver(),
                        backupCsvCodec,
                        executor
                );
        createWarehouseBackupUseCase =
                new CreateWarehouseBackupService(
                        repository,
                        backupDocumentExporter
                );

        AndroidWarehouseBackupDocumentReader
                backupDocumentReader =
                new AndroidWarehouseBackupDocumentReader(
                        applicationContext.getContentResolver(),
                        backupCsvCodec,
                        executor
                );
        validateWarehouseBackupUseCase =
                new ValidateWarehouseBackupService(
                        backupDocumentReader
                );
        restoreWarehouseBackupUseCase =
                new RestoreWarehouseBackupService(repository);
    }

    public DataManagementViewModelFactory
    provideDataManagementViewModelFactory() {
        return new DataManagementViewModelFactory(
                exportWarehouseItemsUseCase,
                shareWarehouseItemsUseCase,
                importWarehouseItemsUseCase,
                createWarehouseBackupUseCase,
                validateWarehouseBackupUseCase,
                restoreWarehouseBackupUseCase,
                () -> "almacentracker-export-"
                        + LocalDate.now()
                        + ".csv",
                () -> "almacentracker-backup-"
                        + timestamp()
                        + ".csv"
        );
    }

    private static String timestamp() {
        return LocalDateTime.now().format(
                TIMESTAMP_FORMATTER
        );
    }
}
