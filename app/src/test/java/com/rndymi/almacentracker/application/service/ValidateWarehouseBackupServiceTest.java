package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.application.port.out.WarehouseBackupCsvReadCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseBackupCsvReader;
import com.rndymi.almacentracker.application.result.WarehouseBackupCsvRow;
import com.rndymi.almacentracker.application.result.WarehouseBackupReadResult;
import com.rndymi.almacentracker.application.result.WarehouseBackupValidationResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public final class ValidateWarehouseBackupServiceTest {

    private FakeBackupCsvReader reader;
    private ValidateWarehouseBackupService service;

    @Before
    public void setUp() {
        reader = new FakeBackupCsvReader();
        service = new ValidateWarehouseBackupService(reader);
    }

    @Test
    public void validateBackup_invalidSource_rejectsWithoutReading() {
        ResultCaptor captor = new ResultCaptor();

        service.validateBackup("   ", captor::capture);

        assertEquals(
                WarehouseBackupValidationResult.Status.INVALID_SOURCE,
                captor.result.getStatus()
        );
        assertEquals(0, reader.calls);
    }

    @Test
    public void validateBackup_validRows_normalizesAndReturnsItems() {
        reader.result = WarehouseBackupReadResult.success(
                Collections.singletonList(
                        row(
                                2,
                                " mr ",
                                " ab-10 ",
                                " a1 ",
                                " Nivel 2 ",
                                " Revisar ",
                                "1000",
                                "2000"
                        )
                )
        );
        ResultCaptor captor = new ResultCaptor();

        service.validateBackup("content://backup", captor::capture);

        assertEquals(
                WarehouseBackupValidationResult.Status.VALID,
                captor.result.getStatus()
        );
        assertEquals(1, captor.result.getRestorableCount());

        WarehouseItem item =
                captor.result.getWarehouseItems().get(0);

        assertEquals("MR", item.getCategory());
        assertEquals("AB-10", item.getCode());
        assertEquals("A1", item.getSite());
        assertEquals("Nivel 2", item.getPosition());
        assertEquals("Revisar", item.getObservations());
        assertEquals(1000L, item.getCreatedAt());
        assertEquals(2000L, item.getUpdatedAt());
    }

    @Test
    public void validateBackup_headerOnly_returnsValidEmptyBackup() {
        reader.result = WarehouseBackupReadResult.success(
                Collections.emptyList()
        );
        ResultCaptor captor = new ResultCaptor();

        service.validateBackup("content://backup", captor::capture);

        assertEquals(
                WarehouseBackupValidationResult.Status.VALID,
                captor.result.getStatus()
        );
        assertTrue(captor.result.getWarehouseItems().isEmpty());
    }

    @Test
    public void validateBackup_duplicateIdentity_rejectsSecondRow() {
        reader.result = WarehouseBackupReadResult.success(
                Arrays.asList(
                        row(2, "MR", "1050", "A1", null, null,
                                "1000", "1000"),
                        row(3, " mr ", " 1050 ", "B1", null, null,
                                "2000", "2000")
                )
        );
        ResultCaptor captor = new ResultCaptor();

        service.validateBackup("content://backup", captor::capture);

        assertEquals(
                WarehouseBackupValidationResult.Status.DUPLICATE_DATA,
                captor.result.getStatus()
        );
        assertEquals(3, captor.result.getInvalidRowNumber());
    }

    @Test
    public void validateBackup_invalidDate_reportsSourceRow() {
        reader.result = WarehouseBackupReadResult.success(
                Collections.singletonList(
                        row(7, "MR", "1050", "A1", null, null,
                                "invalid", "2000")
                )
        );
        ResultCaptor captor = new ResultCaptor();

        service.validateBackup("content://backup", captor::capture);

        assertEquals(
                WarehouseBackupValidationResult.Status.INVALID_DATA,
                captor.result.getStatus()
        );
        assertEquals(7, captor.result.getInvalidRowNumber());
        assertTrue(captor.result.getDetail().contains("created_at"));
    }

    @Test
    public void validateBackup_updatedBeforeCreated_rejectsData() {
        reader.result = WarehouseBackupReadResult.success(
                Collections.singletonList(
                        row(4, "MR", "1050", "A1", null, null,
                                "2000", "1000")
                )
        );
        ResultCaptor captor = new ResultCaptor();

        service.validateBackup("content://backup", captor::capture);

        assertEquals(
                WarehouseBackupValidationResult.Status.INVALID_DATA,
                captor.result.getStatus()
        );
        assertEquals(4, captor.result.getInvalidRowNumber());
    }

    @Test
    public void validateBackup_readerFailures_preserveStatusAndCause() {
        IllegalStateException cause =
                new IllegalStateException("Cannot read");
        reader.result = WarehouseBackupReadResult.readError(cause);
        ResultCaptor captor = new ResultCaptor();

        service.validateBackup("content://backup", captor::capture);

        assertEquals(
                WarehouseBackupValidationResult.Status.READ_ERROR,
                captor.result.getStatus()
        );
        assertSame(cause, captor.result.getCause());
        assertTrue(captor.result.getWarehouseItems().isEmpty());
    }

    @Test
    public void validateBackup_incompatibleVersion_preservesStatus() {
        reader.result =
                WarehouseBackupReadResult.incompatibleVersion();
        ResultCaptor captor = new ResultCaptor();

        service.validateBackup("content://backup", captor::capture);

        assertEquals(
                WarehouseBackupValidationResult.Status.INCOMPATIBLE_VERSION,
                captor.result.getStatus()
        );
        assertNull(captor.result.getCause());
    }

    private WarehouseBackupCsvRow row(
            int rowNumber,
            String category,
            String code,
            String site,
            String position,
            String observations,
            String createdAt,
            String updatedAt
    ) {
        return new WarehouseBackupCsvRow(
                rowNumber,
                "1",
                category,
                code,
                site,
                position,
                observations,
                createdAt,
                updatedAt
        );
    }

    private static final class ResultCaptor {
        private WarehouseBackupValidationResult result;

        private void capture(
                WarehouseBackupValidationResult result
        ) {
            this.result = result;
        }
    }

    private static final class FakeBackupCsvReader
            implements WarehouseBackupCsvReader {

        private int calls;
        private WarehouseBackupReadResult result;

        @Override
        public void readBackup(
                String sourceReference,
                WarehouseBackupCsvReadCallback callback
        ) {
            calls++;
            callback.onResult(result);
        }
    }
}
