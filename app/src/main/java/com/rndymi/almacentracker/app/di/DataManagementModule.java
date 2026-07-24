package com.rndymi.almacentracker.app.di;

import android.content.Context;

import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
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
import com.rndymi.almacentracker.feature.data_management.backup.restore.RestoreWarehouseBackupService;
import com.rndymi.almacentracker.feature.data_management.backup.restore.ValidateWarehouseBackupService;
import com.rndymi.almacentracker.feature.data_management.common.DataManagementViewModelFactory;
import com.rndymi.almacentracker.feature.data_management.export.ExportWarehouseItemsService;
import com.rndymi.almacentracker.feature.data_management.import_data.ImportWarehouseItemsService;
import com.rndymi.almacentracker.feature.data_management.share.ShareWarehouseItemsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.Executor;

public final class DataManagementModule {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");

    private final ExportWarehouseItemsService exportService;
    private final ShareWarehouseItemsService shareService;
    private final ImportWarehouseItemsService importService;
    private final CreateWarehouseBackupService createBackupService;
    private final ValidateWarehouseBackupService validateBackupService;
    private final RestoreWarehouseBackupService restoreBackupService;

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
        importService =
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
        exportService =
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
        shareService =
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
        createBackupService =
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
        validateBackupService =
                new ValidateWarehouseBackupService(
                        backupDocumentReader
                );
        restoreBackupService =
                new RestoreWarehouseBackupService(repository);
    }

    public DataManagementViewModelFactory
    provideDataManagementViewModelFactory() {
        return new DataManagementViewModelFactory(
                exportService,
                shareService,
                importService,
                createBackupService,
                validateBackupService,
                restoreBackupService,
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
