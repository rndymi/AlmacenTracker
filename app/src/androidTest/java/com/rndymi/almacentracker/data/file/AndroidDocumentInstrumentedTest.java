package com.rndymi.almacentracker.data.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rndymi.almacentracker.core.csv.exchange.WarehouseItemCsvExporter.ExportCallback;
import com.rndymi.almacentracker.core.csv.exchange.WarehouseItemCsvReader.ReadCallback;
import com.rndymi.almacentracker.core.csv.share.WarehouseItemCsvShareFileGateway.ShareFileCallback;
import com.rndymi.almacentracker.core.csv.backup.WarehouseBackupCsvExporter;
import com.rndymi.almacentracker.core.csv.share.ShareableCsvFile;
import com.rndymi.almacentracker.core.csv.backup.WarehouseBackupReadResult;
import com.rndymi.almacentracker.core.csv.exchange.WarehouseItemCsvReadResult;
import com.rndymi.almacentracker.data.file.csv.backup.WarehouseBackupCsvCodec;
import com.rndymi.almacentracker.data.file.csv.backup.WarehouseBackupCsvMapper;
import com.rndymi.almacentracker.data.file.csv.exchange.WarehouseItemCsvCodec;
import com.rndymi.almacentracker.data.file.csv.exchange.WarehouseItemCsvMapper;
import com.rndymi.almacentracker.data.file.document.AndroidCsvDocumentExporter;
import com.rndymi.almacentracker.data.file.document.AndroidCsvDocumentReader;
import com.rndymi.almacentracker.data.file.document.AndroidWarehouseBackupDocumentExporter;
import com.rndymi.almacentracker.data.file.document.AndroidWarehouseBackupDocumentReader;
import com.rndymi.almacentracker.data.file.share.AndroidCsvShareFileGateway;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public final class AndroidDocumentInstrumentedTest {

    private Context context;
    private File sharedDirectory;
    private WarehouseItemCsvCodec codec;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        sharedDirectory = new File(
                context.getCacheDir(),
                "shared_csv"
        );
        deleteSharedFiles();
        codec = new WarehouseItemCsvCodec(
                new WarehouseItemCsvMapper()
        );
    }

    @After
    public void tearDown() {
        deleteSharedFiles();
    }

    @Test
    public void documentsRoundTripThroughFileProvider() {
        ShareableCsvFile shareableFile =
                createShareableFile(
                        warehouseItem(
                                "MR",
                                "1050",
                                "A1"
                        )
        );
        Uri contentUri = Uri.parse(
                shareableFile.getContentReference()
        );

        assertEquals("content", contentUri.getScheme());
        assertEquals(
                context.getPackageName() + ".fileprovider",
                contentUri.getAuthority()
        );
        assertEquals(
                "text/csv",
                shareableFile.getMimeType()
        );

        export(
                contentUri,
                warehouseItem(
                        "MD",
                        "2050",
                        "B2"
                )
        );
        WarehouseItemCsvReadResult result =
                read(contentUri);

        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getRows().size());
        assertEquals(
                "MD",
                result.getRows().get(0).getCategory()
        );
        assertEquals(
                "2050",
                result.getRows().get(0).getCode()
        );
        assertEquals(
                "B2",
                result.getRows().get(0).getSite()
        );
    }

    @Test
    public void backupDocumentsRoundTripPreservesDatesAndOmitsIds() {
        Uri contentUri = Uri.parse(
                createShareableFile(
                        warehouseItem("SEED", "1", "A1")
                ).getContentReference()
        );
        WarehouseItem original = new WarehouseItem(
                999L,
                "MR",
                "Ñ-1050",
                "Almacén",
                null,
                "Revisión ✅",
                1_700_000_000_000L,
                1_700_000_100_000L
        );

        exportBackup(
                contentUri,
                Collections.singletonList(original)
        );

        WarehouseBackupReadResult result =
                readBackup(contentUri);

        assertEquals(
                WarehouseBackupReadResult.Status.SUCCESS,
                result.getStatus()
        );
        assertEquals(1, result.getRows().size());
        assertEquals(
                "1",
                result.getRows().get(0).getFormatVersion()
        );
        assertEquals(
                "MR",
                result.getRows().get(0).getCategory()
        );
        assertEquals(
                "Ñ-1050",
                result.getRows().get(0).getCode()
        );
        assertEquals(
                "1700000000000",
                result.getRows().get(0).getCreatedAt()
        );
        assertEquals(
                "1700000100000",
                result.getRows().get(0).getUpdatedAt()
        );
    }

    @Test
    public void emptyBackupDocumentsRoundTripAsValidBackup() {
        Uri contentUri = Uri.parse(
                createShareableFile(
                        warehouseItem("SEED", "1", "A1")
                ).getContentReference()
        );

        exportBackup(contentUri, Collections.emptyList());

        WarehouseBackupReadResult result =
                readBackup(contentUri);

        assertEquals(
                WarehouseBackupReadResult.Status.SUCCESS,
                result.getStatus()
        );
        assertTrue(result.getRows().isEmpty());
    }

    @Test
    public void creatingSharedFileRemovesExpiredTemporaryFiles()
            throws IOException {
        assertTrue(
                sharedDirectory.mkdirs()
                        || sharedDirectory.isDirectory()
        );

        File expired = new File(
                sharedDirectory,
                "expired.csv"
        );

        assertTrue(expired.createNewFile());
        assertTrue(
                expired.setLastModified(
                        System.currentTimeMillis()
                                - 2L * 24L * 60L * 60L * 1000L
                )
        );

        ShareableCsvFile result =
                createShareableFile(
                        warehouseItem("MR", "1050", "A1")
                );

        assertFalse(expired.exists());
        assertEquals(
                "content",
                Uri.parse(result.getContentReference())
                        .getScheme()
        );
        assertTrue(
                new File(
                        sharedDirectory,
                        result.getFileName()
                ).isFile()
        );
    }

    private ShareableCsvFile createShareableFile(
            WarehouseItem warehouseItem
    ) {
        AtomicReference<ShareableCsvFile> result =
                new AtomicReference<>();
        AndroidCsvShareFileGateway gateway =
                new AndroidCsvShareFileGateway(
                        context,
                        codec,
                        Runnable::run,
                        context.getPackageName()
                                + ".fileprovider"
                );

        gateway.createShareableFile(
                Collections.singletonList(warehouseItem),
                "instrumented-share.csv",
                new ShareFileCallback() {
                    @Override
                    public void onSuccess(
                            ShareableCsvFile shareableFile
                    ) {
                        result.set(shareableFile);
                    }

                    @Override
                    public void onSerializationError(
                            Throwable throwable
                    ) {
                        failWithCause(throwable);
                    }

                    @Override
                    public void onTemporaryFileError(
                            Throwable throwable
                    ) {
                        failWithCause(throwable);
                    }

                    @Override
                    public void onFileProviderError(
                            Throwable throwable
                    ) {
                        failWithCause(throwable);
                    }

                    @Override
                    public void onUnknownError(
                            Throwable throwable
                    ) {
                        failWithCause(throwable);
                    }
                }
        );

        assertNotNull(result.get());
        return result.get();
    }

    private void export(
            Uri destination,
            WarehouseItem warehouseItem
    ) {
        AndroidCsvDocumentExporter exporter =
                new AndroidCsvDocumentExporter(
                        context.getContentResolver(),
                        codec,
                        Runnable::run
                );

        exporter.export(
                destination.toString(),
                Collections.singletonList(warehouseItem),
                new ExportCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onInvalidDestination() {
                        fail("Destination URI was rejected");
                    }

                    @Override
                    public void onSerializationError(
                            Throwable throwable
                    ) {
                        failWithCause(throwable);
                    }

                    @Override
                    public void onWriteError(
                            Throwable throwable
                    ) {
                        failWithCause(throwable);
                    }

                    @Override
                    public void onUnknownError(
                            Throwable throwable
                    ) {
                        failWithCause(throwable);
                    }
                }
        );
    }

    private WarehouseItemCsvReadResult read(Uri source) {
        AtomicReference<WarehouseItemCsvReadResult> result =
                new AtomicReference<>();
        AndroidCsvDocumentReader reader =
                new AndroidCsvDocumentReader(
                        context.getContentResolver(),
                        codec,
                        Runnable::run
                );

        reader.read(
                source.toString(),
                new ReadCallback() {
                    @Override
                    public void onSuccess(
                            WarehouseItemCsvReadResult readResult
                    ) {
                        result.set(readResult);
                    }

                    @Override
                    public void onInvalidFormat() {
                        fail("Exported CSV was invalid");
                    }

                    @Override
                    public void onReadError(
                            Throwable throwable
                    ) {
                        failWithCause(throwable);
                    }

                    @Override
                    public void onUnknownError(
                            Throwable throwable
                    ) {
                        failWithCause(throwable);
                    }
                }
        );

        assertNotNull(result.get());
        return result.get();
    }

    private void exportBackup(
            Uri destination,
            java.util.List<WarehouseItem> warehouseItems
    ) {
        WarehouseBackupCsvCodec backupCodec =
                new WarehouseBackupCsvCodec(
                        new WarehouseBackupCsvMapper()
                );
        AtomicReference<Throwable> failure =
                new AtomicReference<>();
        AtomicReference<Boolean> success =
                new AtomicReference<>(false);

        new AndroidWarehouseBackupDocumentExporter(
                context.getContentResolver(),
                backupCodec,
                Runnable::run
        ).exportBackup(
                destination.toString(),
                warehouseItems,
                new WarehouseBackupCsvExporter.ExportCallback() {
                    @Override
                    public void onSuccess() {
                        success.set(true);
                    }

                    @Override
                    public void onInvalidDestination() {
                        fail("Backup destination URI was rejected");
                    }

                    @Override
                    public void onInvalidData(Throwable throwable) {
                        failure.set(throwable);
                    }

                    @Override
                    public void onSerializationError(
                            Throwable throwable
                    ) {
                        failure.set(throwable);
                    }

                    @Override
                    public void onWriteError(Throwable throwable) {
                        failure.set(throwable);
                    }

                    @Override
                    public void onUnknownError(Throwable throwable) {
                        failure.set(throwable);
                    }
                }
        );

        if (failure.get() != null) {
            failWithCause(failure.get());
        }

        assertTrue(success.get());
    }

    private WarehouseBackupReadResult readBackup(Uri source) {
        AtomicReference<WarehouseBackupReadResult> result =
                new AtomicReference<>();

        new AndroidWarehouseBackupDocumentReader(
                context.getContentResolver(),
                new WarehouseBackupCsvCodec(
                        new WarehouseBackupCsvMapper()
                ),
                Runnable::run
        ).readBackup(
                source.toString(),
                result::set
        );

        assertNotNull(result.get());
        return result.get();
    }

    private WarehouseItem warehouseItem(
            String category,
            String code,
            String site
    ) {
        return new WarehouseItem(
                1L,
                category,
                code,
                site,
                null,
                null,
                100L,
                100L
        );
    }

    private void deleteSharedFiles() {
        if (!sharedDirectory.isDirectory()) {
            return;
        }

        File[] files = sharedDirectory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                file.delete();
            }
        }
    }

    private static void failWithCause(Throwable throwable) {
        AssertionError assertionError =
                new AssertionError(
                        "Unexpected Android document error",
                        throwable
                );
        throw assertionError;
    }
}
