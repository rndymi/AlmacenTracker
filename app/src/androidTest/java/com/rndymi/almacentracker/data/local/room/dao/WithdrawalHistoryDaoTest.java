package com.rndymi.almacentracker.data.local.room.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rndymi.almacentracker.data.local.room.database.AlmacenTrackerDatabase;
import com.rndymi.almacentracker.data.local.room.entity.WarehouseItemEntity;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntity;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntryEntity;
import com.rndymi.almacentracker.data.local.room.projection.WithdrawalHistorySummaryRow;
import com.rndymi.almacentracker.data.local.room.relation.WithdrawalHistoryWithEntries;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class WithdrawalHistoryDaoTest {

    private AlmacenTrackerDatabase database;
    private WithdrawalHistoryDao historyDao;
    private WarehouseItemDao warehouseItemDao;

    @Before
    public void setUp() {
        Context context =
                ApplicationProvider.getApplicationContext();

        database = Room.inMemoryDatabaseBuilder(
                context,
                AlmacenTrackerDatabase.class
        ).allowMainThreadQueries().build();

        historyDao = database.withdrawalHistoryDao();
        warehouseItemDao = database.warehouseItemDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void insertHistoryReturnsPositiveId() {
        long historyId = historyDao.insertHistory(
                createHistory(null)
        );

        assertTrue(historyId > 0L);
    }

    @Test
    public void transactionStoresHeaderAndOrderedEntries() {
        long historyId =
                historyDao.insertHistoryWithEntries(
                        createHistory("Reposición centro"),
                        Arrays.asList(
                                createNotFoundEntry(
                                        1,
                                        "MZ",
                                        "1300C"
                                ),
                                createFoundEntry(
                                        0,
                                        "MR",
                                        "001210A"
                                )
                        )
                );

        WithdrawalHistoryWithEntries relation =
                historyDao.findByIdWithEntries(historyId);

        assertNotNull(relation);
        assertNotNull(relation.history);
        assertEquals(
                "Reposición centro",
                relation.history.getTitle()
        );

        List<WithdrawalHistoryEntryEntity> entries =
                historyDao.findEntriesByHistoryId(
                        historyId
                );

        assertEquals(2, entries.size());
        assertEquals(0, entries.get(0).getOrderIndex());
        assertEquals(
                "001210A",
                entries.get(0).getCode()
        );
        assertEquals(1, entries.get(1).getOrderIndex());
        assertEquals(
                "1300C",
                entries.get(1).getCode()
        );
    }

    @Test
    public void historyWithoutTitleCanBePersisted() {
        long historyId =
                historyDao.insertHistoryWithEntries(
                        createHistory(null),
                        Collections.singletonList(
                                createNotFoundEntry(
                                        0,
                                        "MI",
                                        "0900"
                                )
                        )
                );

        WithdrawalHistoryWithEntries relation =
                historyDao.findByIdWithEntries(historyId);

        assertNotNull(relation);
        assertNull(relation.history.getTitle());
    }

    @Test
    public void nullableQuantityAndUnitArePreserved() {
        long historyId =
                historyDao.insertHistoryWithEntries(
                        createHistory(null),
                        Collections.singletonList(
                                createNotFoundEntry(
                                        0,
                                        "MZ",
                                        "0900"
                                )
                        )
                );

        WithdrawalHistoryEntryEntity entry =
                historyDao
                        .findEntriesByHistoryId(historyId)
                        .get(0);

        assertNull(entry.getQuantity());
        assertNull(entry.getUnit());
    }

    @Test
    public void foundEntryPreservesLocationSnapshot() {
        long historyId =
                historyDao.insertHistoryWithEntries(
                        createHistory(null),
                        Collections.singletonList(
                                createFoundEntry(
                                        0,
                                        "MR",
                                        "1210A"
                                )
                        )
                );

        WithdrawalHistoryEntryEntity entry =
                historyDao
                        .findEntriesByHistoryId(historyId)
                        .get(0);

        assertEquals("FOUND", entry.getLocationStatus());
        assertEquals(
                Long.valueOf(15L),
                entry.getWarehouseItemIdSnapshot()
        );
        assertEquals("A1", entry.getSiteSnapshot());
        assertEquals(
                "Nivel 2",
                entry.getPositionSnapshot()
        );
    }

    @Test
    public void deletingHeaderDeletesOnlyItsEntries() {
        long firstHistoryId =
                historyDao.insertHistoryWithEntries(
                        createHistory("Primera"),
                        Collections.singletonList(
                                createFoundEntry(
                                        0,
                                        "MR",
                                        "1210"
                                )
                        )
                );

        long secondHistoryId =
                historyDao.insertHistoryWithEntries(
                        createHistory("Segunda"),
                        Collections.singletonList(
                                createNotFoundEntry(
                                        0,
                                        "MZ",
                                        "1300"
                                )
                        )
                );

        int deleted =
                historyDao.deleteById(firstHistoryId);

        assertEquals(1, deleted);
        assertEquals(
                0,
                historyDao.countEntriesByHistoryId(
                        firstHistoryId
                )
        );
        assertEquals(
                1,
                historyDao.countEntriesByHistoryId(
                        secondHistoryId
                )
        );
        assertEquals(1, historyDao.countHistories());
    }

    @Test
    public void deletingHistoryDoesNotDeleteWarehouseItems() {
        long warehouseItemId =
                warehouseItemDao.insert(
                        createWarehouseItem()
                );

        long historyId =
                historyDao.insertHistoryWithEntries(
                        createHistory(null),
                        Collections.singletonList(
                                new WithdrawalHistoryEntryEntity(
                                        0L,
                                        0L,
                                        0,
                                        "MR",
                                        "1210",
                                        4,
                                        "CAJAS",
                                        null,
                                        warehouseItemId,
                                        "A1",
                                        null,
                                        "FOUND"
                                )
                        )
                );

        historyDao.deleteById(historyId);

        assertNotNull(
                warehouseItemDao.findById(
                        warehouseItemId
                )
        );
    }

    @Test
    public void duplicateOrderIndexRollsBackWholeTransaction() {
        List<WithdrawalHistoryEntryEntity> entries =
                Arrays.asList(
                        createFoundEntry(
                                0,
                                "MR",
                                "1210"
                        ),
                        createNotFoundEntry(
                                0,
                                "MZ",
                                "1300"
                        )
                );

        try {
            historyDao.insertHistoryWithEntries(
                    createHistory("Invalid"),
                    entries
            );

            fail(
                    "Expected duplicate order index constraint"
            );
        } catch (SQLiteConstraintException expected) {
            assertEquals(0, historyDao.countHistories());
            assertEquals(0, historyDao.countAllEntries());
        }
    }

    @Test
    public void sameOrderIndexIsAllowedInDifferentHistories() {
        historyDao.insertHistoryWithEntries(
                createHistory("First"),
                Collections.singletonList(
                        createFoundEntry(
                                0,
                                "MR",
                                "1210"
                        )
                )
        );

        historyDao.insertHistoryWithEntries(
                createHistory("Second"),
                Collections.singletonList(
                        createFoundEntry(
                                0,
                                "MR",
                                "1210"
                        )
                )
        );

        assertEquals(2, historyDao.countHistories());
        assertEquals(2, historyDao.countAllEntries());
    }

    @Test
    public void unknownHistoryReturnsNull() {
        WithdrawalHistoryWithEntries result =
                historyDao.findByIdWithEntries(999L);

        assertNull(result);
    }

    @Test
    public void findAllSummaries_emptyDatabase_returnsEmptyList() {
        List<WithdrawalHistorySummaryRow> summaries =
                historyDao.findAllSummaries();

        assertNotNull(summaries);
        assertTrue(summaries.isEmpty());
    }

    @Test
    public void findAllSummaries_countsEntriesByStatus() {
        long historyId =
                historyDao.insertHistoryWithEntries(
                        createHistoryEntity(
                                0L,
                                "Lista centro",
                                200L
                        ),
                        Arrays.asList(
                                createFoundEntry(0),
                                createFoundEntry(1),
                                createNotFoundEntry(2)
                        )
                );

        List<WithdrawalHistorySummaryRow> summaries =
                historyDao.findAllSummaries();

        assertEquals(1, summaries.size());

        WithdrawalHistorySummaryRow summary =
                summaries.get(0);

        assertEquals(historyId, summary.getId());
        assertEquals(3, summary.getEntryCount());
        assertEquals(2, summary.getFoundCount());
        assertEquals(1, summary.getNotFoundCount());
    }

    @Test
    public void findAllSummaries_ordersByRegisteredAtAndIdDescending() {
        long firstId =
                historyDao.insertHistoryWithEntries(
                        createHistoryEntity(
                                0L,
                                "Primera",
                                100L
                        ),
                        Collections.singletonList(
                                createFoundEntry(0)
                        )
                );

        long secondId =
                historyDao.insertHistoryWithEntries(
                        createHistoryEntity(
                                0L,
                                "Segunda",
                                200L
                        ),
                        Collections.singletonList(
                                createFoundEntry(0)
                        )
                );

        long thirdId =
                historyDao.insertHistoryWithEntries(
                        createHistoryEntity(
                                0L,
                                "Tercera",
                                200L
                        ),
                        Collections.singletonList(
                                createFoundEntry(0)
                        )
                );

        List<WithdrawalHistorySummaryRow> summaries =
                historyDao.findAllSummaries();

        assertEquals(3, summaries.size());
        assertEquals(thirdId, summaries.get(0).getId());
        assertEquals(secondId, summaries.get(1).getId());
        assertEquals(firstId, summaries.get(2).getId());
    }

    @Test
    public void searchSummariesByTitleKeepsAllCounters() {
        long historyId = insertHistory(
                "Reposición tienda centro",
                timestampFor(2026, 8, 2, 12, 0)
        );

        insertFoundEntry(
                historyId,
                0,
                "MR",
                "1210A"
        );

        insertNotFoundEntry(
                historyId,
                1,
                "MZ",
                "1300C"
        );

        List<WithdrawalHistorySummaryRow> rows =
                historyDao.searchSummaries(
                        1,
                        "%centro%",
                        null,
                        null
                );

        assertEquals(1, rows.size());
        assertEquals(2, rows.get(0).getEntryCount());
        assertEquals(1, rows.get(0).getFoundCount());
        assertEquals(1, rows.get(0).getNotFoundCount());
    }

    @Test
    public void searchSummariesByCategoryUsesExistsAndKeepsAllEntries() {
        long historyId = insertHistory(
                "Lista documental",
                timestampFor(2026, 8, 2, 12, 0)
        );

        insertFoundEntry(
                historyId,
                0,
                "MR",
                "1210A"
        );

        insertFoundEntry(
                historyId,
                1,
                "MI",
                "1300"
        );

        insertNotFoundEntry(
                historyId,
                2,
                "MZ",
                "900"
        );

        List<WithdrawalHistorySummaryRow> rows =
                historyDao.searchSummaries(
                        1,
                        "%mr%",
                        null,
                        null
                );

        assertEquals(1, rows.size());
        assertEquals(3, rows.get(0).getEntryCount());
        assertEquals(2, rows.get(0).getFoundCount());
        assertEquals(1, rows.get(0).getNotFoundCount());
    }

    @Test
    public void searchSummariesByCodePreservesLeadingZeros() {
        long historyId = insertHistory(
                null,
                timestampFor(2026, 8, 2, 12, 0)
        );

        insertFoundEntry(
                historyId,
                0,
                "MR",
                "001210A"
        );

        List<WithdrawalHistorySummaryRow> rows =
                historyDao.searchSummaries(
                        1,
                        "%001210%",
                        null,
                        null
                );

        assertEquals(1, rows.size());
        assertEquals(historyId, rows.get(0).getId());
    }

    @Test
    public void searchSummariesIncludesWholeFinalDay() {
        long firstHistoryId = insertHistory(
                "Primera",
                timestampFor(2026, 8, 2, 0, 0)
        );

        long secondHistoryId = insertHistory(
                "Segunda",
                timestampFor(2026, 8, 2, 23, 59)
        );

        insertFoundEntry(
                firstHistoryId,
                0,
                "MR",
                "1000"
        );

        insertFoundEntry(
                secondHistoryId,
                0,
                "MR",
                "2000"
        );

        long from =
                timestampFor(
                        2026,
                        8,
                        2,
                        0,
                        0
                );

        long toExclusive =
                timestampFor(
                        2026,
                        8,
                        3,
                        0,
                        0
        );

        List<WithdrawalHistorySummaryRow> rows =
                historyDao.searchSummaries(
                        0,
                        "",
                        from,
                        toExclusive
                );

        assertEquals(2, rows.size());
    }

    @Test
    public void searchSummariesTreatsPercentAsLiteral() {
        long literalHistoryId = insertHistory(
                "Lista 100% revisada",
                timestampFor(2026, 8, 2, 12, 0)
        );

        long otherHistoryId = insertHistory(
                "Lista normal",
                timestampFor(2026, 8, 1, 12, 0)
        );

        insertFoundEntry(
                literalHistoryId,
                0,
                "MR",
                "1000"
        );

        insertFoundEntry(
                otherHistoryId,
                0,
                "MI",
                "2000"
        );

        List<WithdrawalHistorySummaryRow> rows =
                historyDao.searchSummaries(
                        1,
                        "%\\%%",
                        null,
                        null
                );

        assertEquals(1, rows.size());
        assertEquals(
                literalHistoryId,
                rows.get(0).getId()
        );
    }

    @Test
    public void deleteByIdRemovesHistoryAndEntries() {
        long historyId =
                historyDao.insertHistoryWithEntries(
                        createHistory(null),
                        Collections.singletonList(
                                createFoundEntry(0)
                        )
                );

        int deletedRows =
                historyDao.deleteById(historyId);

        assertEquals(
                1,
                deletedRows
        );

        assertNull(
                historyDao.findByIdWithEntries(
                        historyId
                )
        );

        assertEquals(
                0,
                historyDao.countEntriesByHistoryId(
                        historyId
                )
        );
    }

    private long insertHistory(
            String title,
            long registeredAt
    ) {
        return historyDao.insertHistory(
                createHistoryEntity(
                        0L,
                        title,
                        registeredAt
                )
        );
    }

    private void insertFoundEntry(
            long historyId,
            int orderIndex,
            String category,
            String code
    ) {
        historyDao.insertEntries(
                Collections.singletonList(
                        createFoundEntry(
                                orderIndex,
                                category,
                                code
                        ).withHistoryId(historyId)
                )
        );
    }

    private void insertNotFoundEntry(
            long historyId,
            int orderIndex,
            String category,
            String code
    ) {
        historyDao.insertEntries(
                Collections.singletonList(
                        createNotFoundEntry(
                                orderIndex,
                                category,
                                code
                        ).withHistoryId(historyId)
                )
        );
    }

    private static long timestampFor(
            int year,
            int month,
            int day,
            int hour,
            int minute
    ) {
        return LocalDateTime.of(
                year,
                month,
                day,
                hour,
                minute
        ).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private WithdrawalHistoryEntity createHistory(
            String title
    ) {
        return new WithdrawalHistoryEntity(
                0L,
                title,
                1000L,
                1000L,
                1000L
        );
    }

    private WithdrawalHistoryEntity createHistoryEntity(
            long id,
            String title,
            long registeredAt
    ) {
        return new WithdrawalHistoryEntity(
                id,
                title,
                registeredAt,
                registeredAt,
                registeredAt
        );
    }

    private WithdrawalHistoryEntryEntity
    createFoundEntry(
            int orderIndex
    ) {
        return createFoundEntry(
                orderIndex,
                "MR",
                String.valueOf(1210 + orderIndex)
        );
    }

    private WithdrawalHistoryEntryEntity
    createNotFoundEntry(
            int orderIndex
    ) {
        return createNotFoundEntry(
                orderIndex,
                "MZ",
                String.valueOf(1300 + orderIndex)
        );
    }

    private WithdrawalHistoryEntryEntity
    createFoundEntry(
            int orderIndex,
            String category,
            String code
    ) {
        return new WithdrawalHistoryEntryEntity(
                0L,
                0L,
                orderIndex,
                category,
                code,
                4,
                "CAJAS",
                null,
                15L,
                "A1",
                "Nivel 2",
                "FOUND"
        );
    }

    private WithdrawalHistoryEntryEntity
    createNotFoundEntry(
            int orderIndex,
            String category,
            String code
    ) {
        return new WithdrawalHistoryEntryEntity(
                0L,
                0L,
                orderIndex,
                category,
                code,
                null,
                null,
                null,
                null,
                null,
                null,
                "NOT_FOUND"
        );
    }

    private WarehouseItemEntity createWarehouseItem() {
        return new WarehouseItemEntity(
                0L,
                "MR",
                "1210",
                "A1",
                null,
                null,
                1000L,
                1000L
        );
    }
}
