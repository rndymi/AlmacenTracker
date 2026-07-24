package com.rndymi.almacentracker.feature.data_management.export;

import com.rndymi.almacentracker.core.csv.exchange.WarehouseItemCsvExporter.ExportCallback;
import com.rndymi.almacentracker.core.csv.exchange.WarehouseItemCsvExporter;
import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ExportWarehouseItemsService {

    private final WarehouseItemRepository repository;
    private final WarehouseItemCsvExporter csvExporter;

    public ExportWarehouseItemsService(
            WarehouseItemRepository repository,
            WarehouseItemCsvExporter csvExporter
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.csvExporter = Objects.requireNonNull(csvExporter);
    }

    public void exportWarehouseItems(
            String destinationReference,
            Consumer<ExportWarehouseItemsResult> callback
    ) {
        Objects.requireNonNull(callback);

        if (destinationReference == null
                || destinationReference.trim().isEmpty()) {
            callback.accept(
                    ExportWarehouseItemsResult.of(
                            ExportWarehouseItemsResult.Status
                                    .INVALID_DESTINATION
                    )
            );
            return;
        }

        repository.findAll(new RepositoryCallback<List<WarehouseItem>>() {
            @Override
            public void onSuccess(List<WarehouseItem> warehouseItems) {
                if (warehouseItems == null || warehouseItems.isEmpty()) {
                    callback.accept(
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
                callback.accept(
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
            Consumer<ExportWarehouseItemsResult> callback
    ) {
        csvExporter.export(
                destinationReference,
                warehouseItems,
                new ExportCallback() {
                    @Override
                    public void onSuccess() {
                        callback.accept(
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
            Consumer<ExportWarehouseItemsResult> callback,
            ExportWarehouseItemsResult.Status status
    ) {
        callback.accept(ExportWarehouseItemsResult.of(status));
    }
}
