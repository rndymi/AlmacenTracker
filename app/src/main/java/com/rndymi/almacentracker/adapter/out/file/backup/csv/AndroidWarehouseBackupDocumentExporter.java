package com.rndymi.almacentracker.adapter.out.file.backup.csv;

import android.content.ContentResolver;
import android.net.Uri;

import com.rndymi.almacentracker.application.port.out.WarehouseBackupCsvExportCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseBackupCsvExporter;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

public final class AndroidWarehouseBackupDocumentExporter
        implements WarehouseBackupCsvExporter {

    private final ContentResolver contentResolver;
    private final WarehouseBackupCsvCodec codec;
    private final Executor executor;

    public AndroidWarehouseBackupDocumentExporter(
            ContentResolver contentResolver,
            WarehouseBackupCsvCodec codec,
            Executor executor
    ) {
        this.contentResolver =
                Objects.requireNonNull(contentResolver);

        this.codec = Objects.requireNonNull(codec);
        this.executor = Objects.requireNonNull(executor);
    }

    @Override
    public void exportBackup(
            String destinationReference,
            List<WarehouseItem> warehouseItems,
            WarehouseBackupCsvExportCallback callback
    ) {
        Objects.requireNonNull(warehouseItems);
        Objects.requireNonNull(callback);

        if (destinationReference == null
                || destinationReference.trim().isEmpty()) {
            callback.onInvalidDestination();
            return;
        }

        final Uri destinationUri;

        try {
            destinationUri = Uri.parse(
                    destinationReference
            );
        } catch (RuntimeException exception) {
            callback.onInvalidDestination();
            return;
        }

        if (destinationUri.getScheme() == null) {
            callback.onInvalidDestination();
            return;
        }

        executor.execute(
                () -> writeBackup(
                        destinationUri,
                        warehouseItems,
                        callback
                )
        );
    }

    private void writeBackup(
            Uri destinationUri,
            List<WarehouseItem> warehouseItems,
            WarehouseBackupCsvExportCallback callback
    ) {
        final byte[] content;

        try {
            content = codec.encode(warehouseItems);
        } catch (IllegalArgumentException exception) {
            callback.onInvalidData(exception);
            return;
        } catch (RuntimeException exception) {
            callback.onSerializationError(exception);
            return;
        }

        try (OutputStream outputStream =
                     contentResolver.openOutputStream(
                             destinationUri,
                             "wt"
                     )) {

            if (outputStream == null) {
                callback.onWriteError(
                        new IOException(
                                "ContentResolver returned null OutputStream"
                        )
                );
                return;
            }

            outputStream.write(content);
            outputStream.flush();

            callback.onSuccess();

        } catch (IOException | SecurityException exception) {
            callback.onWriteError(exception);
        } catch (RuntimeException exception) {
            callback.onUnknownError(exception);
        }
    }
}