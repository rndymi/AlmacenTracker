package com.rndymi.almacentracker.feature.data_management.share;

import com.rndymi.almacentracker.core.csv.share.WarehouseItemCsvShareFileGateway.ShareFileCallback;
import com.rndymi.almacentracker.core.csv.share.WarehouseItemCsvShareFileGateway;
import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.core.csv.share.ShareableCsvFile;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ShareWarehouseItemsService {

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

    public void prepareWarehouseItemsForSharing(
            Consumer<ShareWarehouseItemsResult> callback
    ) {
        Objects.requireNonNull(callback);

        repository.findAll(new RepositoryCallback<List<WarehouseItem>>() {

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
            Consumer<ShareWarehouseItemsResult> callback
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
                new ShareFileCallback() {

                    @Override
                    public void onSuccess(
                            ShareableCsvFile shareableFile
                    ) {
                        callback.accept(
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
            Consumer<ShareWarehouseItemsResult> callback,
            ShareWarehouseItemsResult.Status status
    ) {
        callback.accept(
                ShareWarehouseItemsResult.of(status)
        );
    }
}
