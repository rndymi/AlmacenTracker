package com.rndymi.almacentracker.application.service;

import com.rndymi.almacentracker.application.port.in.CreateWarehouseBackupUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseBackupCsvExportCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseBackupCsvExporter;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsFindCallback;
import com.rndymi.almacentracker.application.result.CreateWarehouseBackupResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CreateWarehouseBackupService
        implements CreateWarehouseBackupUseCase {

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

    @Override
    public void createBackup(
            String destinationReference,
            Callback callback
    ) {
        Objects.requireNonNull(callback);

        if (destinationReference == null
                || destinationReference.trim().isEmpty()) {
            callback.onResult(
                    CreateWarehouseBackupResult.of(
                            CreateWarehouseBackupResult.Status
                                    .INVALID_DESTINATION
                    )
            );
            return;
        }

        repository.findAll(
                new WarehouseItemsFindCallback() {
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
            Callback callback
    ) {
        backupExporter.exportBackup(
                destinationReference,
                warehouseItems,
                new WarehouseBackupCsvExportCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onResult(
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
            Callback callback,
            CreateWarehouseBackupResult.Status status
    ) {
        callback.onResult(
                CreateWarehouseBackupResult.of(status)
        );
    }
}