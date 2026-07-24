package com.rndymi.almacentracker.feature.data_management.backup.create;

import com.rndymi.almacentracker.core.csv.backup.WarehouseBackupCsvExporter.ExportCallback;
import com.rndymi.almacentracker.core.csv.backup.WarehouseBackupCsvExporter;
import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class CreateWarehouseBackupService {

    private final WarehouseItemRepository repository;
    private final WarehouseBackupCsvExporter backupExporter;

    public CreateWarehouseBackupService(
            WarehouseItemRepository repository,
            WarehouseBackupCsvExporter backupExporter
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.backupExporter =
                Objects.requireNonNull(backupExporter);
    }

    public void createBackup(
            String destinationReference,
            Consumer<CreateWarehouseBackupResult> callback
    ) {
        Objects.requireNonNull(callback);

        if (destinationReference == null
                || destinationReference.trim().isEmpty()) {
            callback.accept(
                    CreateWarehouseBackupResult.of(
                            CreateWarehouseBackupResult.Status
                                    .INVALID_DESTINATION
                    )
            );
            return;
        }

        repository.findAll(
                new RepositoryCallback<List<WarehouseItem>>() {
                    @Override
                    public void onSuccess(
                            List<WarehouseItem> warehouseItems
                    ) {
                        List<WarehouseItem> safeItems =
                                warehouseItems == null
                                        ? Collections.emptyList()
                                        : warehouseItems;

                        exportBackup(
                                destinationReference,
                                safeItems,
                                callback
                        );
                    }

                    @Override
                    public void onError(
                            Throwable throwable
                    ) {
                        emit(
                                callback,
                                CreateWarehouseBackupResult.Status
                                        .READ_ERROR
                        );
                    }
                }
        );
    }

    private void exportBackup(
            String destinationReference,
            List<WarehouseItem> warehouseItems,
            Consumer<CreateWarehouseBackupResult> callback
    ) {
        backupExporter.exportBackup(
                destinationReference,
                warehouseItems,
                new ExportCallback() {
                    @Override
                    public void onSuccess() {
                        callback.accept(
                                CreateWarehouseBackupResult
                                        .success(
                                                warehouseItems.size()
                                        )
                        );
                    }

                    @Override
                    public void onInvalidDestination() {
                        emit(
                                callback,
                                CreateWarehouseBackupResult.Status
                                        .INVALID_DESTINATION
                        );
                    }

                    @Override
                    public void onInvalidData(
                            Throwable throwable
                    ) {
                        emit(
                                callback,
                                CreateWarehouseBackupResult.Status
                                        .INVALID_DATA
                        );
                    }

                    @Override
                    public void onSerializationError(
                            Throwable throwable
                    ) {
                        emit(
                                callback,
                                CreateWarehouseBackupResult.Status
                                        .SERIALIZATION_ERROR
                        );
                    }

                    @Override
                    public void onWriteError(
                            Throwable throwable
                    ) {
                        emit(
                                callback,
                                CreateWarehouseBackupResult.Status
                                        .WRITE_ERROR
                        );
                    }

                    @Override
                    public void onUnknownError(
                            Throwable throwable
                    ) {
                        emit(
                                callback,
                                CreateWarehouseBackupResult.Status
                                        .UNKNOWN_ERROR
                        );
                    }
                }
        );
    }

    private void emit(
            Consumer<CreateWarehouseBackupResult> callback,
            CreateWarehouseBackupResult.Status status
    ) {
        callback.accept(
                CreateWarehouseBackupResult.of(status)
        );
    }
}
