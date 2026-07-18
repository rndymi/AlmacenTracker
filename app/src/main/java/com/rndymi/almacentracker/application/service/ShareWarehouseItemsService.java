package com.rndymi.almacentracker.application.service;

import com.rndymi.almacentracker.application.port.in
        .ShareWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.out
        .WarehouseItemCsvShareFileCallback;
import com.rndymi.almacentracker.application.port.out
        .WarehouseItemCsvShareFileGateway;
import com.rndymi.almacentracker.application.port.out
        .WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out
        .WarehouseItemsFindCallback;
import com.rndymi.almacentracker.application.result
        .ShareWarehouseItemsResult;
import com.rndymi.almacentracker.application.result.ShareableCsvFile;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class ShareWarehouseItemsService
        implements ShareWarehouseItemsUseCase {

    private final WarehouseItemRepository repository;
    private final WarehouseItemCsvShareFileGateway shareFileGateway;
    private final Supplier<String> fileNameSupplier;

    public ShareWarehouseItemsService(
            WarehouseItemRepository repository,
            WarehouseItemCsvShareFileGateway shareFileGateway,
            Supplier<String> fileNameSupplier
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.shareFileGateway =
                Objects.requireNonNull(shareFileGateway);
        this.fileNameSupplier =
                Objects.requireNonNull(fileNameSupplier);
    }

    @Override
    public void prepareWarehouseItemsForSharing(
            Callback callback
    ) {
        Objects.requireNonNull(callback);

        repository.findAll(new WarehouseItemsFindCallback() {

            @Override
            public void onSuccess(
                    List<WarehouseItem> warehouseItems
            ) {
                if (warehouseItems == null
                        || warehouseItems.isEmpty()) {
                    emit(
                            callback,
                            ShareWarehouseItemsResult.Status
                                    .EMPTY_DATABASE
                    );
                    return;
                }

                createShareableFile(
                        warehouseItems,
                        callback
                );
            }

            @Override
            public void onError(Throwable throwable) {
                emit(
                        callback,
                        ShareWarehouseItemsResult.Status
                                .READ_ERROR
                );
            }
        });
    }

    private void createShareableFile(
            List<WarehouseItem> warehouseItems,
            Callback callback
    ) {
        final String fileName;

        try {
            fileName = fileNameSupplier.get();
        } catch (RuntimeException exception) {
            emit(
                    callback,
                    ShareWarehouseItemsResult.Status.UNKNOWN_ERROR
            );
            return;
        }

        shareFileGateway.createShareableFile(
                warehouseItems,
                fileName,
                new WarehouseItemCsvShareFileCallback() {

                    @Override
                    public void onSuccess(
                            ShareableCsvFile shareableFile
                    ) {
                        callback.onResult(
                                ShareWarehouseItemsResult.success(
                                        shareableFile
                                )
                        );
                    }

                    @Override
                    public void onSerializationError(
                            Throwable throwable
                    ) {
                        emit(
                                callback,
                                ShareWarehouseItemsResult.Status
                                        .SERIALIZATION_ERROR
                        );
                    }

                    @Override
                    public void onTemporaryFileError(
                            Throwable throwable
                    ) {
                        emit(
                                callback,
                                ShareWarehouseItemsResult.Status
                                        .TEMP_FILE_ERROR
                        );
                    }

                    @Override
                    public void onFileProviderError(
                            Throwable throwable
                    ) {
                        emit(
                                callback,
                                ShareWarehouseItemsResult.Status
                                        .FILE_PROVIDER_ERROR
                        );
                    }

                    @Override
                    public void onUnknownError(
                            Throwable throwable
                    ) {
                        emit(
                                callback,
                                ShareWarehouseItemsResult.Status
                                        .UNKNOWN_ERROR
                        );
                    }
                }
        );
    }

    private void emit(
            Callback callback,
            ShareWarehouseItemsResult.Status status
    ) {
        callback.onResult(
                ShareWarehouseItemsResult.of(status)
        );
    }
}