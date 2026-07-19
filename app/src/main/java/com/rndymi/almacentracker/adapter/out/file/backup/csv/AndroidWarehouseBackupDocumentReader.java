package com.rndymi.almacentracker.adapter.out.file.backup.csv;

import android.content.ContentResolver;
import android.net.Uri;

import com.rndymi.almacentracker.application.port.out.WarehouseBackupCsvReadCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseBackupCsvReader;
import com.rndymi.almacentracker.application.result.WarehouseBackupReadResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.Executor;

public final class AndroidWarehouseBackupDocumentReader
        implements WarehouseBackupCsvReader {

    private static final int BUFFER_SIZE = 8 * 1024;

    private final ContentResolver contentResolver;
    private final WarehouseBackupCsvCodec codec;
    private final Executor fileExecutor;

    public AndroidWarehouseBackupDocumentReader(
            ContentResolver contentResolver,
            WarehouseBackupCsvCodec codec,
            Executor fileExecutor
    ) {
        this.contentResolver =
                Objects.requireNonNull(contentResolver);
        this.codec = Objects.requireNonNull(codec);
        this.fileExecutor =
                Objects.requireNonNull(fileExecutor);
    }

    @Override
    public void readBackup(
            String sourceReference,
            WarehouseBackupCsvReadCallback callback
    ) {
        Objects.requireNonNull(callback);

        fileExecutor.execute(() -> {
            if (sourceReference == null
                    || sourceReference.trim().isEmpty()) {
                callback.onResult(
                        WarehouseBackupReadResult.readError(
                                new IllegalArgumentException(
                                        "Backup source is invalid"
                                )
                        )
                );
                return;
            }

            try {
                Uri sourceUri = Uri.parse(sourceReference);

                if (sourceUri.getScheme() == null) {
                    callback.onResult(
                            WarehouseBackupReadResult.readError(
                                    new IllegalArgumentException(
                                            "Backup URI has no scheme"
                                    )
                            )
                    );
                    return;
                }

                byte[] content = readContent(sourceUri);

                callback.onResult(
                        codec.decode(content)
                );
            } catch (IOException
                     | RuntimeException exception) {
                callback.onResult(
                        WarehouseBackupReadResult.readError(
                                exception
                        )
                );
            }
        });
    }

    private byte[] readContent(Uri sourceUri)
            throws IOException {

        try (InputStream inputStream =
                     contentResolver.openInputStream(
                             sourceUri
                     )) {

            if (inputStream == null) {
                throw new IOException(
                        "ContentResolver returned no stream"
                );
            }

            try (ByteArrayOutputStream outputStream =
                         new ByteArrayOutputStream()) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int readCount;

                while ((readCount =
                        inputStream.read(buffer)) != -1) {
                    outputStream.write(
                            buffer,
                            0,
                            readCount
                    );
                }

                return outputStream.toByteArray();
            }
        }
    }
}