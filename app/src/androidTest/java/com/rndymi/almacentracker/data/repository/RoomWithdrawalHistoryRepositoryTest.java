package com.rndymi.almacentracker.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rndymi.almacentracker.data.local.room.database.AlmacenTrackerDatabase;
import com.rndymi.almacentracker.data.local.room.mapper.WithdrawalHistoryRoomMapper;
import com.rndymi.almacentracker.domain.history.WithdrawalHistory;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryEntry;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;
import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class RoomWithdrawalHistoryRepositoryTest {

    private AlmacenTrackerDatabase database;
    private ExecutorService executor;
    private WithdrawalHistoryRepository repository;

    @Before
    public void setUp() {
        Context context =
                ApplicationProvider.getApplicationContext();

        database = Room.inMemoryDatabaseBuilder(
                context,
                AlmacenTrackerDatabase.class
        ).build();

        executor = Executors.newSingleThreadExecutor();

        repository =
                new RoomWithdrawalHistoryRepository(
                        database.withdrawalHistoryDao(),
                        new WithdrawalHistoryRoomMapper(),
                        executor
                );
    }

    @After
    public void tearDown() {
        executor.shutdownNow();
        database.close();
    }

    @Test
    public void insertAndFindByIdPreserveOrderedAggregate()
            throws Exception {

        WithdrawalHistoryRecord input =
                createRecord();

        long historyId = insert(input);

        assertTrue(historyId > 0L);

        WithdrawalHistoryRecord result =
                findById(historyId);

        assertNotNull(result);
        assertEquals(
                historyId,
                result.getHistory().getId()
        );
        assertEquals(
                "Lista centro",
                result.getHistory().getTitle()
        );
        assertEquals(2, result.getEntries().size());

        assertEquals(
                "001210A",
                result.getEntries().get(0).getCode()
        );
        assertEquals(
                WithdrawalLocationStatus.FOUND,
                result.getEntries().get(0)
                        .getLocationStatus()
        );

        assertEquals(
                "1300C",
                result.getEntries().get(1).getCode()
        );
        assertEquals(
                WithdrawalLocationStatus.NOT_FOUND,
                result.getEntries().get(1)
                        .getLocationStatus()
        );
    }

    @Test
    public void unknownIdProducesNotFound()
            throws Exception {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> outcome =
                new AtomicReference<>();

        repository.findById(
                999L,
                new RepositoryCallback<
                        WithdrawalHistoryRecord
                        >() {
                    @Override
                    public void onSuccess(
                            WithdrawalHistoryRecord value
                    ) {
                        outcome.set("success");
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable cause) {
                        outcome.set("error");
                        latch.countDown();
                    }

                    @Override
                    public void onNotFound() {
                        outcome.set("not-found");
                        latch.countDown();
                    }
                }
        );

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals("not-found", outcome.get());
    }

    @Test
    public void deleteRemovesHistoryAndEntries()
            throws Exception {

        long historyId = insert(createRecord());

        delete(historyId);

        assertEquals(
                0,
                database.withdrawalHistoryDao()
                        .countHistories()
        );
        assertEquals(
                0,
                database.withdrawalHistoryDao()
                        .countAllEntries()
        );
    }

    private long insert(
            WithdrawalHistoryRecord record
    ) throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Long> result =
                new AtomicReference<>();
        AtomicReference<Throwable> error =
                new AtomicReference<>();

        repository.insert(
                record,
                new RepositoryCallback<Long>() {
                    @Override
                    public void onSuccess(Long value) {
                        result.set(value);
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable cause) {
                        error.set(cause);
                        latch.countDown();
                    }
                }
        );

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertNull(error.get());
        assertNotNull(result.get());

        return result.get();
    }

    private WithdrawalHistoryRecord findById(
            long historyId
    ) throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<WithdrawalHistoryRecord> result =
                new AtomicReference<>();
        AtomicReference<Throwable> error =
                new AtomicReference<>();

        repository.findById(
                historyId,
                new RepositoryCallback<
                        WithdrawalHistoryRecord
                        >() {
                    @Override
                    public void onSuccess(
                            WithdrawalHistoryRecord value
                    ) {
                        result.set(value);
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable cause) {
                        error.set(cause);
                        latch.countDown();
                    }
                }
        );

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertNull(error.get());

        return result.get();
    }

    private void delete(
            long historyId
    ) throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error =
                new AtomicReference<>();

        repository.deleteById(
                historyId,
                new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable cause) {
                        error.set(cause);
                        latch.countDown();
                    }
                }
        );

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertNull(error.get());
    }

    private WithdrawalHistoryRecord createRecord() {
        WithdrawalHistory history =
                new WithdrawalHistory(
                        0L,
                        "Lista centro",
                        1000L,
                        1000L,
                        1000L
                );

        WithdrawalHistoryEntry second =
                new WithdrawalHistoryEntry(
                        0L,
                        0L,
                        1,
                        "MZ",
                        "1300C",
                        null,
                        null,
                        null,
                        null,
                        null,
                        WithdrawalLocationStatus.NOT_FOUND
                );

        WithdrawalHistoryEntry first =
                new WithdrawalHistoryEntry(
                        0L,
                        0L,
                        0,
                        "MR",
                        "001210A",
                        4,
                        "CAJAS",
                        7L,
                        "A1",
                        "Nivel 2",
                        WithdrawalLocationStatus.FOUND
                );

        return new WithdrawalHistoryRecord(
                history,
                Arrays.asList(second, first)
        );
    }
}