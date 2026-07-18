package com.rndymi.almacentracker.adapter.out.file.csv;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvShareFileCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvShareFileGateway;
import com.rndymi.almacentracker.application.result.ShareableCsvFile;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

public final class AndroidCsvShareFileGateway
        implements WarehouseItemCsvShareFileGateway {

    static final String SHARED_DIRECTORY = "shared_csv";
    static final String MIME_TYPE = "text/csv";

    static final long MAX_FILE_AGE_MILLIS =
            24L * 60L * 60L * 1000L;

    static final int MAX_RECENT_FILES = 10;

    private final Context applicationContext;
    private final WarehouseItemCsvCodec codec;
    private final Executor executor;
    private final String fileProviderAuthority;
    private final TimeProvider timeProvider;

    public AndroidCsvShareFileGateway(
            Context applicationContext,
            WarehouseItemCsvCodec codec,
            Executor executor,
            String fileProviderAuthority
    ) {
        this(
                applicationContext,
                codec,
                executor,
                fileProviderAuthority,
                System::currentTimeMillis
        );
    }

    AndroidCsvShareFileGateway(
            Context applicationContext,
            WarehouseItemCsvCodec codec,
            Executor executor,
            String fileProviderAuthority,
            TimeProvider timeProvider
    ) {
        this.applicationContext =
                Objects.requireNonNull(applicationContext)
                        .getApplicationContext();

        this.codec = Objects.requireNonNull(codec);
        this.executor = Objects.requireNonNull(executor);

        this.fileProviderAuthority = requireText(
                fileProviderAuthority,
                "fileProviderAuthority"
        );

        this.timeProvider = Objects.requireNonNull(timeProvider);
    }

    @Override
    public void createShareableFile(
            List<WarehouseItem> warehouseItems,
            String suggestedFileName,
            WarehouseItemCsvShareFileCallback callback
    ) {
        Objects.requireNonNull(warehouseItems);
        Objects.requireNonNull(callback);

        final String safeFileName;

        try {
            safeFileName = validateFileName(
                    suggestedFileName
            );
        } catch (IllegalArgumentException exception) {
            callback.onTemporaryFileError(exception);
            return;
        }

        executor.execute(
                () -> createFile(
                        warehouseItems,
                        safeFileName,
                        callback
                )
        );
    }

    private void createFile(
            List<WarehouseItem> warehouseItems,
            String fileName,
            WarehouseItemCsvShareFileCallback callback
    ) {
        final byte[] content;

        try {
            content = codec.encode(warehouseItems);
        } catch (RuntimeException exception) {
            callback.onSerializationError(exception);
            return;
        }

        File sharedDirectory = new File(
                applicationContext.getCacheDir(),
                SHARED_DIRECTORY
        );

        if (!ensureDirectory(sharedDirectory)) {
            callback.onTemporaryFileError(
                    new IOException(
                            "Unable to create shared CSV directory"
                    )
            );
            return;
        }

        cleanOldFiles(sharedDirectory);

        File shareFile = new File(
                sharedDirectory,
                fileName
        );

        try (OutputStream outputStream =
                     new FileOutputStream(shareFile, false)) {

            outputStream.write(content);
            outputStream.flush();

        } catch (IOException | SecurityException exception) {
            deleteQuietly(shareFile);
            callback.onTemporaryFileError(exception);
            return;

        } catch (RuntimeException exception) {
            deleteQuietly(shareFile);
            callback.onUnknownError(exception);
            return;
        }

        final Uri contentUri;

        try {
            contentUri = FileProvider.getUriForFile(
                    applicationContext,
                    fileProviderAuthority,
                    shareFile
            );

        } catch (IllegalArgumentException
                 | SecurityException exception) {

            deleteQuietly(shareFile);
            callback.onFileProviderError(exception);
            return;

        } catch (RuntimeException exception) {
            deleteQuietly(shareFile);
            callback.onUnknownError(exception);
            return;
        }

        callback.onSuccess(
                new ShareableCsvFile(
                        contentUri.toString(),
                        fileName,
                        MIME_TYPE,
                        warehouseItems.size()
                )
        );
    }

    private boolean ensureDirectory(File directory) {
        if (directory.isDirectory()) {
            return true;
        }

        return !directory.exists() && directory.mkdirs();
    }

    private void cleanOldFiles(File directory) {
        File[] files = directory.listFiles(File::isFile);

        if (files == null || files.length == 0) {
            return;
        }

        long oldestAllowedTimestamp =
                timeProvider.currentTimeMillis()
                        - MAX_FILE_AGE_MILLIS;

        for (File file : files) {
            if (file.lastModified()
                    < oldestAllowedTimestamp) {
                deleteQuietly(file);
            }
        }

        File[] remainingFiles =
                directory.listFiles(File::isFile);

        if (remainingFiles == null
                || remainingFiles.length
                < MAX_RECENT_FILES) {
            return;
        }

        Arrays.sort(
                remainingFiles,
                Comparator.comparingLong(
                        File::lastModified
                ).reversed()
        );

        for (
                int index = MAX_RECENT_FILES - 1;
                index < remainingFiles.length;
                index++
        ) {
            deleteQuietly(remainingFiles[index]);
        }
    }

    private String validateFileName(String fileName) {
        String value = requireText(
                fileName,
                "suggestedFileName"
        );

        if (!value.endsWith(".csv")
                || value.contains("/")
                || value.contains("\\")) {
            throw new IllegalArgumentException(
                    "Invalid CSV file name"
            );
        }

        return value;
    }

    private String requireText(
            String value,
            String fieldName
    ) {
        Objects.requireNonNull(value, fieldName);

        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }

        return value;
    }

    private void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    interface TimeProvider {
        long currentTimeMillis();
    }
}