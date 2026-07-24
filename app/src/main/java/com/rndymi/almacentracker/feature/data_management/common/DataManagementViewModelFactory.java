package com.rndymi.almacentracker.feature.data_management.common;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.feature.data_management.backup.create.CreateWarehouseBackupService;
import com.rndymi.almacentracker.feature.data_management.export.ExportWarehouseItemsService;
import com.rndymi.almacentracker.feature.data_management.import_data.ImportWarehouseItemsService;
import com.rndymi.almacentracker.feature.data_management.backup.restore.RestoreWarehouseBackupService;
import com.rndymi.almacentracker.feature.data_management.share.ShareWarehouseItemsService;
import com.rndymi.almacentracker.feature.data_management.backup.restore.ValidateWarehouseBackupService;

import java.util.Objects;
import java.util.function.Supplier;

public final class DataManagementViewModelFactory
        implements ViewModelProvider.Factory {

    private final ExportWarehouseItemsService exportService;
    private final ShareWarehouseItemsService shareService;
    private final ImportWarehouseItemsService importService;
    private final CreateWarehouseBackupService createBackupService;
    private final ValidateWarehouseBackupService validateBackupService;
    private final RestoreWarehouseBackupService restoreBackupService;
    private final Supplier<String> exportFileNameSupplier;
    private final Supplier<String> backupFileNameSupplier;

    public DataManagementViewModelFactory(
            ExportWarehouseItemsService exportService,
            ShareWarehouseItemsService shareService,
            ImportWarehouseItemsService importService,
            CreateWarehouseBackupService createBackupService,
            ValidateWarehouseBackupService validateBackupService,
            RestoreWarehouseBackupService restoreBackupService,
            Supplier<String> exportFileNameSupplier,
            Supplier<String> backupFileNameSupplier
    ) {
        this.exportService =
                Objects.requireNonNull(exportService);

        this.shareService =
                Objects.requireNonNull(shareService);

        this.importService =
                Objects.requireNonNull(importService);

        this.createBackupService =
                Objects.requireNonNull(
                        createBackupService
                );

        this.validateBackupService =
                Objects.requireNonNull(
                        validateBackupService
                );

        this.restoreBackupService =
                Objects.requireNonNull(
                        restoreBackupService
                );

        this.exportFileNameSupplier =
                Objects.requireNonNull(
                        exportFileNameSupplier
                );

        this.backupFileNameSupplier =
                Objects.requireNonNull(
                        backupFileNameSupplier
                );
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {
        if (!modelClass.isAssignableFrom(
                DataManagementViewModel.class
        )) {
            throw new IllegalArgumentException(
                    "Unknown ViewModel class: "
                            + modelClass.getName()
            );
        }

        return modelClass.cast(
                new DataManagementViewModel(
                        exportService,
                        shareService,
                        importService,
                        createBackupService,
                        validateBackupService,
                        restoreBackupService,
                        exportFileNameSupplier,
                        backupFileNameSupplier
                )
        );
    }
}
