package com.rndymi.almacentracker.adapter.out.file.csv;

import android.content.ContentResolver;
import android.net.Uri;

import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvReadCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvReader;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvReadResult;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.Executor;

public final class AndroidCsvDocumentReader
        implements WarehouseItemCsvReader {

    private static final int BUFFER_SIZE = 8 * 1024;

    private static final int MAX_FILE_SIZE_BYTES =
            10 * 1024 * 1024;

    private final ContentResolver contentResolver;
    private final WarehouseItemCsvCodec codec;
    private final Executor fileExecutor;

    public AndroidCsvDocumentReader(
            ContentResolver contentResolver,
            WarehouseItemCsvCodec codec,
            Executor fileExecutor
    ) {
        this.contentResolver =
                Objects.requireNonNull(contentResolver);

        this.codec = Objects.requireNonNull(codec);
        this.fileExecutor =
                Objects.requireNonNull(fileExecutor);
    }

    @Override
    public void read(
            String sourceReference,
            WarehouseItemCsvReadCallback callback
    ) {
        Objects.requireNonNull(callback);

        fileExecutor.execute(
                () -> readInternal(
                        sourceReference,
                        callback
                )
        );
    }

    private void readInternal(
            String sourceReference,
            WarehouseItemCsvReadCallback callback
    ) {
        final Uri sourceUri;

        try {
            sourceUri = Uri.parse(sourceReference);
        } catch (RuntimeException exception) {
            callback.onReadError(exception);
            return;
        }

        if (!"content".equals(sourceUri.getScheme())) {
            callback.onReadError(
                    new IllegalArgumentException(
                            "Source must use content URI"
                    )
            );
            return;
        }

        try (InputStream inputStream =
                     contentResolver.openInputStream(
                             sourceUri
                     )) {

            if (inputStream == null) {
                callback.onReadError(
                        new IOException(
                                "ContentResolver returned null stream"
                        )
                );
                return;
            }

            byte[] csvBytes =
                    readAllBytes(inputStream);

            WarehouseItemCsvReadResult result =
                    codec.decode(csvBytes);

            callback.onSuccess(result);
        } catch (WarehouseItemCsvFormatException exception) {
            callback.onInvalidFormat();
        } catch (FileNotFoundException
                 | SecurityException exception) {
            callback.onReadError(exception);
        } catch (IOException exception) {
            callback.onReadError(exception);
        } catch (RuntimeException exception) {
            callback.onUnknownError(exception);
        }
    }

    private byte[] readAllBytes(
            InputStream inputStream
    ) throws IOException {
        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int totalBytes = 0;
        int readCount;

        while ((readCount = inputStream.read(buffer)) != -1) {
            totalBytes += readCount;

            if (totalBytes > MAX_FILE_SIZE_BYTES) {
                throw new IOException(
                        "CSV file exceeds supported size"
                );
            }

            outputStream.write(
                    buffer,
                    0,
                    readCount
            );
        }

        return outputStream.toByteArray();
    }
}