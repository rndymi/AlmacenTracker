package com.rndymi.almacentracker.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.data.local.room.dao.WithdrawalHistoryDao;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntity;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntryEntity;
import com.rndymi.almacentracker.data.local.room.mapper.WithdrawalHistoryRoomMapper;
import com.rndymi.almacentracker.data.local.room.projection.WithdrawalHistorySummaryRow;
import com.rndymi.almacentracker.data.local.room.relation.WithdrawalHistoryWithEntries;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySummary;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public final class RoomWithdrawalHistoryRepositoryTest {

    private FakeWithdrawalHistoryDao dao;
    private TestExecutor executor;
    private RoomWithdrawalHistoryRepository repository;

    @Before
    public void setUp() {
        dao = new FakeWithdrawalHistoryDao();
        executor = new TestExecutor();
        repository =
                new RoomWithdrawalHistoryRepository(
                        dao,
                        new WithdrawalHistoryRoomMapper(),
                        executor
                );
    }

    @Test
    public void findAllSummaries_emptyRows_returnsSuccessfulEmptyList() {
        dao.summaryRows = Collections.emptyList();

        TestRepositoryCallback<
                List<WithdrawalHistorySummary>> callback =
                new TestRepositoryCallback<>();

        repository.findAllSummaries(callback);
        executor.runPendingCommand();

        assertTrue(callback.wasSuccessful());
        assertNotNull(callback.getValue());
        assertTrue(callback.getValue().isEmpty());
        assertFalse(callback.wasNotFound());
    }

    @Test
    public void findAllSummaries_mapsRowsAndPreservesOrder() {
        WithdrawalHistorySummaryRow first =
                new WithdrawalHistorySummaryRow(
                        9L,
                        "Nueva",
                        200L,
                        200L,
                        200L,
                        2,
                        2,
                        0
                );

        WithdrawalHistorySummaryRow second =
                new WithdrawalHistorySummaryRow(
                        4L,
                        null,
                        100L,
                        100L,
                        100L,
                        1,
                        0,
                        1
                );

        dao.summaryRows = Arrays.asList(first, second);

        TestRepositoryCallback<
                List<WithdrawalHistorySummary>> callback =
                new TestRepositoryCallback<>();

        repository.findAllSummaries(callback);
        executor.runPendingCommand();

        assertTrue(callback.wasSuccessful());
        assertEquals(2, callback.getValue().size());
        assertEquals(
                9L,
                callback.getValue().get(0).getId()
        );
        assertEquals(
                4L,
                callback.getValue().get(1).getId()
        );
    }

    @Test
    public void findAllSummaries_whenDaoFails_returnsError() {
        RuntimeException expected =
                new RuntimeException("Database error");

        dao.summaryError = expected;

        TestRepositoryCallback<
                List<WithdrawalHistorySummary>> callback =
                new TestRepositoryCallback<>();

        repository.findAllSummaries(callback);
        executor.runPendingCommand();

        assertSame(expected, callback.getError());
        assertFalse(callback.wasSuccessful());
    }

    @Test
    public void findAllSummaries_nullCallback_isRejected() {
        assertThrows(
                NullPointerException.class,
                () -> repository.findAllSummaries(null)
        );
    }

    private static final class TestExecutor
            implements Executor {

        private Runnable pendingCommand;

        @Override
        public void execute(Runnable command) {
            pendingCommand = command;
        }

        void runPendingCommand() {
            assertNotNull(pendingCommand);

            Runnable command = pendingCommand;
            pendingCommand = null;
            command.run();
        }
    }

    private static final class TestRepositoryCallback<T>
            implements RepositoryCallback<T> {

        private T value;
        private Throwable error;
        private boolean successful;
        private boolean notFound;

        @Override
        public void onSuccess(T value) {
            this.value = value;
            successful = true;
        }

        @Override
        public void onError(Throwable cause) {
            error = cause;
        }

        @Override
        public void onNotFound() {
            notFound = true;
        }

        T getValue() {
            return value;
        }

        Throwable getError() {
            return error;
        }

        boolean wasSuccessful() {
            return successful;
        }

        boolean wasNotFound() {
            return notFound;
        }
    }

    private static final class FakeWithdrawalHistoryDao
            implements WithdrawalHistoryDao {

        private List<WithdrawalHistorySummaryRow> summaryRows =
                Collections.emptyList();
        private RuntimeException summaryError;

        @Override
        public List<WithdrawalHistorySummaryRow>
        findAllSummaries() {
            if (summaryError != null) {
                throw summaryError;
            }

            return new ArrayList<>(summaryRows);
        }

        @Override
        public long insertHistory(
                WithdrawalHistoryEntity entity
        ) {
            throw unsupported();
        }

        @Override
        public List<Long> insertEntries(
                List<WithdrawalHistoryEntryEntity> entities
        ) {
            throw unsupported();
        }

        @Override
        public WithdrawalHistoryWithEntries
        findByIdWithEntries(long historyId) {
            throw unsupported();
        }

        @Override
        public List<WithdrawalHistoryEntryEntity>
        findEntriesByHistoryId(long historyId) {
            throw unsupported();
        }

        @Override
        public int deleteById(long historyId) {
            throw unsupported();
        }

        @Override
        public int countHistories() {
            throw unsupported();
        }

        @Override
        public int countEntriesByHistoryId(
                long historyId
        ) {
            throw unsupported();
        }

        @Override
        public int countAllEntries() {
            throw unsupported();
        }

        private UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException(
                    "Not required by this unit test"
            );
        }
    }
}
