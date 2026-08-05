package com.rndymi.almacentracker.feature.withdrawal_history.create;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WithdrawalHistoryRepository;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraft;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraftEntry;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySearchCriteria;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySummary;
import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class WithdrawalHistorySaveServiceTest {

    @Test
    public void save_createsRecordWithSingleTimestamp() {
        FakeRepository repository =
                new FakeRepository();

        WithdrawalHistorySaveService service =
                new WithdrawalHistorySaveService(
                        repository,
                        () -> 2000L
                );

        RecordingCallback callback =
                new RecordingCallback();

        service.save(
                validDraft(),
                callback
        );

        assertNotNull(
                repository.insertedRecord
        );

        assertEquals(
                1000L,
                repository.insertedRecord
                        .getHistory()
                        .getRegisteredAt()
        );

        assertEquals(
                2000L,
                repository.insertedRecord
                        .getHistory()
                        .getCreatedAt()
        );

        assertEquals(
                2000L,
                repository.insertedRecord
                        .getHistory()
                        .getUpdatedAt()
        );

        assertEquals(
                2,
                repository.insertedRecord
                        .getEntries()
                        .size()
        );

        assertEquals(
                Long.valueOf(8L),
                callback.value
        );
    }

    @Test
    public void save_preservesFoundSnapshot() {
        FakeRepository repository =
                new FakeRepository();

        WithdrawalHistorySaveService service =
                new WithdrawalHistorySaveService(
                        repository,
                        () -> 2000L
                );

        service.save(
                validDraft(),
                new RecordingCallback()
        );

        assertEquals(
                Long.valueOf(7L),
                repository.insertedRecord
                        .getEntries()
                        .get(0)
                        .getWarehouseItemIdSnapshot()
        );

        assertEquals(
                "A1",
                repository.insertedRecord
                        .getEntries()
                        .get(0)
                        .getSiteSnapshot()
        );

        assertEquals(
                "2",
                repository.insertedRecord
                        .getEntries()
                        .get(0)
                        .getPositionSnapshot()
        );
        assertEquals(
                Arrays.asList("Tienda 2"),
                repository.insertedRecord
                        .getEntries()
                        .get(0)
                        .getDestinations()
        );
    }

    @Test
    public void save_preservesNotFoundEntry() {
        FakeRepository repository =
                new FakeRepository();

        WithdrawalHistorySaveService service =
                new WithdrawalHistorySaveService(
                        repository,
                        () -> 2000L
                );

        service.save(
                validDraft(),
                new RecordingCallback()
        );

        assertEquals(
                WithdrawalLocationStatus.NOT_FOUND,
                repository.insertedRecord
                        .getEntries()
                        .get(1)
                        .getLocationStatus()
        );

        assertEquals(
                null,
                repository.insertedRecord
                        .getEntries()
                        .get(1)
                        .getWarehouseItemIdSnapshot()
        );
    }

    @Test
    public void save_propagatesRepositoryError() {
        RuntimeException expected =
                new RuntimeException(
                        "Room failure"
                );

        FakeRepository repository =
                new FakeRepository();

        repository.error = expected;

        WithdrawalHistorySaveService service =
                new WithdrawalHistorySaveService(
                        repository,
                        () -> 2000L
                );

        RecordingCallback callback =
                new RecordingCallback();

        service.save(
                validDraft(),
                callback
        );

        assertSame(
                expected,
                callback.error
        );
    }

    @Test
    public void save_rejectsNullDraft() {
        FakeRepository repository =
                new FakeRepository();

        WithdrawalHistorySaveService service =
                new WithdrawalHistorySaveService(
                        repository,
                        () -> 2000L
                );

        RecordingCallback callback =
                new RecordingCallback();

        service.save(
                null,
                callback
        );

        assertNotNull(
                callback.error
        );

        assertTrue(
                callback.error
                        instanceof IllegalArgumentException
        );

        assertEquals(
                0,
                repository.insertCalls
        );
    }

    private static WithdrawalHistoryDraft
    validDraft() {
        WithdrawalHistoryDraftEntry found =
                new WithdrawalHistoryDraftEntry(
                        0,
                        "MR",
                        "1210",
                        4,
                        "CAJAS",
                        7L,
                        "A1",
                        "2",
                        WithdrawalLocationStatus.FOUND,
                        Arrays.asList("Tienda 2")
                );

        WithdrawalHistoryDraftEntry notFound =
                new WithdrawalHistoryDraftEntry(
                        1,
                        "MZ",
                        "1300",
                        null,
                        null,
                        null,
                        null,
                        null,
                        WithdrawalLocationStatus.NOT_FOUND
                );

        return new WithdrawalHistoryDraft(
                "Reposición centro",
                1000L,
                Arrays.asList(
                        found,
                        notFound
                )
        );
    }

    private static final class FakeRepository
            implements WithdrawalHistoryRepository {

        private WithdrawalHistoryRecord insertedRecord;
        private RuntimeException error;
        private int insertCalls;

        @Override
        public void insert(
                WithdrawalHistoryRecord record,
                RepositoryCallback<Long> callback
        ) {
            insertCalls++;
            insertedRecord = record;

            if (error != null) {
                callback.onError(error);
                return;
            }

            callback.onSuccess(8L);
        }

        @Override
        public void findById(
                long historyId,
                RepositoryCallback<WithdrawalHistoryRecord> callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void findAllSummaries(
                RepositoryCallback<
                        List<WithdrawalHistorySummary>> callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void searchSummaries(
                WithdrawalHistorySearchCriteria criteria,
                RepositoryCallback<
                        List<WithdrawalHistorySummary>> callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteById(
                long historyId,
                RepositoryCallback<Void> callback
        ) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class RecordingCallback
            implements RepositoryCallback<Long> {

        private Long value;
        private Throwable error;

        @Override
        public void onSuccess(
                Long value
        ) {
            this.value = value;
        }

        @Override
        public void onError(
                Throwable cause
        ) {
            error = cause;
        }
    }
}
