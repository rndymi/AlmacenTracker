package com.rndymi.almacentracker.application.service;

import com.rndymi.almacentracker.application.port.in.ExportWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvExportCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvExporter;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsFindCallback;
import com.rndymi.almacentracker.application.result.ExportWarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;
import java.util.Objects;

public final class ExportWarehouseItemsService
        implements ExportWarehouseItemsUseCase {

    private final WarehouseItemRepository repository;
    private final WarehouseItemCsvExporter csvExporter;

    public ExportWarehouseItemsService(
            WarehouseItemRepository repository,
            WarehouseItemCsvExporter csvExporter
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.csvExporter = Objects.requireNonNull(csvExporter);
    }

    @Override
    public void exportWarehouseItems(
            String destinationReference,
            Callback callback
    ) {
        Objects.requireNonNull(callback);

        if (destinationReference == null
                || destinationReference.trim().isEmpty()) {
            callback.onResult(
                    ExportWarehouseItemsResult.of(
                            ExportWarehouseItemsResult.Status
                                    .INVALID_DESTINATION
                    )
            );
            return;
        }

        repository.findAll(new WarehouseItemsFindCallback() {
            @Override
            public void onSuccess(List<WarehouseItem> warehouseItems) {
                if (warehouseItems == null || warehouseItems.isEmpty()) {
                    callback.onResult(
                            ExportWarehouseItemsResult.of(
                                    ExportWarehouseItemsResult.Status
                                            .EMPTY_DATABASE
                            )
                    );
                    return;
                }

                exportToDestination(
                        destinationReference,
                        warehouseItems,
                        callback
                );
            }

            @Override
            public void onError(Throwable throwable) {
                callback.onResult(
                        ExportWarehouseItemsResult.of(
                                ExportWarehouseItemsResult.Status
                                        .READ_ERROR
                        )
                );
            }
        });
    }

    private void exportToDestination(
            String destinationReference,
            List<WarehouseItem> warehouseItems,
            Callback callback
    ) {
        csvExporter.export(
                destinationReference,
                warehouseItems,
                new WarehouseItemCsvExportCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onResult(
                                ExportWarehouseItemsResult.success(
                                        warehouseItems.size()
                                )
                        );
                    }

                    @Override
                    public void onInvalidDestination() {
                        emit(
                                callback,
                                ExportWarehouseItemsResult.Status
                                        .INVALID_DESTINATION
                        );
                    }

                    @Override
                    public void onSerializationError(Throwable throwable) {
                        emit(
                                callback,
                                ExportWarehouseItemsResult.Status
                                        .SERIALIZATION_ERROR
                        );
                    }

                    @Override
                    public void onWriteError(Throwable throwable) {
                        emit(
                                callback,
                                ExportWarehouseItemsResult.Status
                                        .WRITE_ERROR
                        );
                    }

                    @Override
                    public void onUnknownError(Throwable throwable) {
                        emit(
                                callback,
                                ExportWarehouseItemsResult.Status
                                        .UNKNOWN_ERROR
                        );
                    }
                }
        );
    }

    private void emit(
            Callback callback,
            ExportWarehouseItemsResult.Status status
    ) {
        callback.onResult(ExportWarehouseItemsResult.of(status));
    }
}