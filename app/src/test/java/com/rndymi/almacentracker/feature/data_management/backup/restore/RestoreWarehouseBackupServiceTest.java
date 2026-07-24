package com.rndymi.almacentracker.feature.data_management.backup.restore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.application.port.out.RepositoryCallback;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RestoreWarehouseBackupServiceTest {

    private FakeRepository repository;
    private RestoreWarehouseBackupService service;

    @Before
    public void setUp() {
        repository = new FakeRepository();
        service = new RestoreWarehouseBackupService(repository);
    }

    @Test
    public void restoreBackup_nullBackup_rejectsWithoutRepositoryCall() {
        ResultCaptor captor = new ResultCaptor();

        service.restoreBackup(null, captor::capture);

        assertEquals(
                RestoreWarehouseBackupResult.Status.INVALID_BACKUP,
                captor.result.getStatus()
        );
        assertEquals(0, repository.replaceCalls);
    }

    @Test
    public void restoreBackup_success_returnsReplacedCount() {
        List<WarehouseItem> items =
                Collections.singletonList(item("MR", "1050"));
        ResultCaptor captor = new ResultCaptor();

        service.restoreBackup(items, captor::capture);

        assertEquals(1, repository.replaceCalls);
        assertNotSame(items, repository.replacement);
        assertEquals(items, repository.replacement);

        repository.callback.onSuccess(1);

        assertEquals(
                RestoreWarehouseBackupResult.Status.SUCCESS,
                captor.result.getStatus()
        );
        assertEquals(1, captor.result.getRestoredCount());
    }

    @Test
    public void restoreBackup_copiesInputBeforeRepositoryUsesIt() {
        List<WarehouseItem> items = new ArrayList<>();
        items.add(item("MR", "1050"));

        service.restoreBackup(items, ignored -> { });
        items.clear();

        assertEquals(1, repository.replacement.size());

        try {
            repository.replacement.clear();
        } catch (UnsupportedOperationException expected) {
            return;
        }

        throw new AssertionError("Replacement snapshot must be immutable");
    }

    @Test
    public void restoreBackup_unexpectedCount_returnsPersistenceError() {
        ResultCaptor captor = new ResultCaptor();

        service.restoreBackup(
                Collections.singletonList(item("MR", "1050")),
                captor::capture
        );
        repository.callback.onSuccess(0);

        assertEquals(
                RestoreWarehouseBackupResult.Status.PERSISTENCE_ERROR,
                captor.result.getStatus()
        );
        assertTrue(captor.result.getCause()
                instanceof IllegalStateException);
    }

    @Test
    public void restoreBackup_duplicate_returnsDuplicateData() {
        IllegalStateException cause =
                new IllegalStateException("Duplicate");
        ResultCaptor captor = new ResultCaptor();

        service.restoreBackup(
                Collections.singletonList(item("MR", "1050")),
                captor::capture
        );
        repository.callback.onDuplicate(cause);

        assertEquals(
                RestoreWarehouseBackupResult.Status.DUPLICATE_DATA,
                captor.result.getStatus()
        );
        assertSame(cause, captor.result.getCause());
    }

    @Test
    public void restoreBackup_repositoryError_returnsPersistenceError() {
        IllegalStateException cause =
                new IllegalStateException("Database failure");
        ResultCaptor captor = new ResultCaptor();

        service.restoreBackup(
                Collections.emptyList(),
                captor::capture
        );
        repository.callback.onError(cause);

        assertEquals(
                RestoreWarehouseBackupResult.Status.PERSISTENCE_ERROR,
                captor.result.getStatus()
        );
        assertSame(cause, captor.result.getCause());
    }

    private WarehouseItem item(String category, String code) {
        return new WarehouseItem(
                0L,
                category,
                code,
                "A1",
                null,
                null,
                1000L,
                2000L
        );
    }

    private static final class ResultCaptor {
        private RestoreWarehouseBackupResult result;

        private void capture(RestoreWarehouseBackupResult result) {
            this.result = result;
        }
    }

    private static final class FakeRepository
            extends WarehouseItemRepositoryStub {

        private int replaceCalls;
        private List<WarehouseItem> replacement;
        private RepositoryCallback<Integer> callback;

        @Override
        public void replaceAll(
                List<WarehouseItem> warehouseItems,
                RepositoryCallback<Integer> callback
        ) {
            replaceCalls++;
            replacement = warehouseItems;
            this.callback = callback;
        }

    }
}
