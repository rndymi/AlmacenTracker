package com.rndymi.almacentracker.adapter.out.persistence.room.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rndymi.almacentracker.adapter.out.persistence.room.database.AlmacenTrackerDatabase;
import com.rndymi.almacentracker.adapter.out.persistence.room.entity.WarehouseItemEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class WarehouseItemDaoTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    private AlmacenTrackerDatabase database;
    private WarehouseItemDao dao;

    @Before
    public void setUp() {
        Context context =
                ApplicationProvider.getApplicationContext();

        database = Room.inMemoryDatabaseBuilder(
                context,
                AlmacenTrackerDatabase.class
        ).allowMainThreadQueries().build();

        dao = database.warehouseItemDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void observeAllReturnsItemsOrderedByCategoryAndCode()
            throws Exception {

        dao.insert(createEntity("MR", "1050", "A1"));
        dao.insert(createEntity("CA", "2048", "C1"));
        dao.insert(createEntity("CA", "1000", "B1"));

        List<WarehouseItemEntity> items =
                getOrAwaitValue(dao.observeAll());

        assertEquals(3, items.size());
        assertEquals("CA", items.get(0).getCategory());
        assertEquals("1000", items.get(0).getCode());
        assertEquals("CA", items.get(1).getCategory());
        assertEquals("2048", items.get(1).getCode());
        assertEquals("MR", items.get(2).getCategory());
    }

    @Test
    public void observeAllReturnsEmptyListWhenDatabaseIsEmpty()
            throws Exception {

        List<WarehouseItemEntity> items =
                getOrAwaitValue(dao.observeAll());

        assertEquals(0, items.size());
    }

    @Test
    public void sameCodeCanExistInDifferentCategories()
            throws Exception {

        dao.insert(createEntity("MR", "1050", "A1"));
        dao.insert(createEntity("MD", "1050", "B3"));

        List<WarehouseItemEntity> items =
                getOrAwaitValue(dao.observeAll());

        assertEquals(2, items.size());
    }

    private WarehouseItemEntity createEntity(
            String category,
            String code,
            String site
    ) {
        return createEntity(
                category,
                code,
                site,
                null
        );
    }

    private WarehouseItemEntity createEntity(
            String category,
            String code,
            String site,
            String position
    ) {
        long now = System.currentTimeMillis();

        return new WarehouseItemEntity(
                0L,
                category,
                code,
                site,
                position,
                null,
                now,
                now
        );
    }

    private <T> T getOrAwaitValue(
            LiveData<T> liveData
    ) throws InterruptedException {

        Object[] data = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);

        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T value) {
                data[0] = value;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };

        liveData.observeForever(observer);

        boolean completed = latch.await(
                2,
                TimeUnit.SECONDS
        );

        if (!completed) {
            liveData.removeObserver(observer);
            throw new AssertionError(
                    "LiveData value was not emitted"
            );
        }

        @SuppressWarnings("unchecked")
        T value = (T) data[0];

        return value;
    }

    @Test
    public void observeByIdReturnsRequestedWarehouseItem()
            throws InterruptedException {

        long firstId = dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1"
                )
        );

        dao.insert(
                createEntity(
                        "MD",
                        "1050",
                        "B3"
                )
        );

        WarehouseItemEntity result =
                getOrAwaitValue(
                        dao.observeById(firstId)
                );

        assertNotNull(result);
        assertEquals(firstId, result.getId());
        assertEquals("MR", result.getCategory());
        assertEquals("1050", result.getCode());
        assertEquals("A1", result.getSite());
    }

    @Test
    public void observeByIdReturnsNullWhenWarehouseItemDoesNotExist()
            throws InterruptedException {

        WarehouseItemEntity result =
                getOrAwaitValueAllowingNull(
                        dao.observeById(999L)
                );

        assertNull(result);
    }

    private <T> T getOrAwaitValueAllowingNull(
            LiveData<T> liveData
    ) throws InterruptedException {

        Object[] data = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);

        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T value) {
                data[0] = value;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };

        liveData.observeForever(observer);

        boolean completed = latch.await(
                2,
                TimeUnit.SECONDS
        );

        if (!completed) {
            liveData.removeObserver(observer);
            throw new AssertionError(
                    "LiveData value was not emitted"
            );
        }

        @SuppressWarnings("unchecked")
        T value = (T) data[0];

        return value;
    }

    @Test
    public void searchFindsPartialCategoryIgnoringCase()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        "Nivel 2"
                )
        );

        dao.insert(
                createEntity(
                        "MD",
                        "2050",
                        "B1",
                        null
                )
        );

        List<WarehouseItemEntity> items =
                getOrAwaitValue(dao.search("mr"));

        assertEquals(1, items.size());
        assertEquals("MR", items.get(0).getCategory());
    }

    @Test
    public void searchFindsPartialCode()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        null
                )
        );

        dao.insert(
                createEntity(
                        "MD",
                        "2105",
                        "B1",
                        null
                )
        );

        List<WarehouseItemEntity> items =
                getOrAwaitValue(dao.search("105"));

        assertEquals(2, items.size());
    }

    @Test
    public void searchFindsPartialSiteIgnoringCase()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        null
                )
        );

        dao.insert(
                createEntity(
                        "MD",
                        "2050",
                        "B3",
                        null
                )
        );

        List<WarehouseItemEntity> items =
                getOrAwaitValue(dao.search("a1"));

        assertEquals(1, items.size());
        assertEquals("A1", items.get(0).getSite());
    }

    @Test
    public void searchFindsPositionAndHandlesNullValues()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        "Nivel 2"
                )
        );

        dao.insert(
                createEntity(
                        "MD",
                        "2050",
                        "B3",
                        null
                )
        );

        List<WarehouseItemEntity> items =
                getOrAwaitValue(
                        dao.search("nivel 2")
                );

        assertEquals(1, items.size());
        assertEquals(
                "Nivel 2",
                items.get(0).getPosition()
        );
    }

    @Test
    public void searchReturnsEmptyListWhenNothingMatches()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        null
                )
        );

        List<WarehouseItemEntity> items =
                getOrAwaitValue(
                        dao.search("ZZZ")
                );

        assertEquals(0, items.size());
    }

    @Test
    public void searchKeepsCategoryAndCodeOrder()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        null
                )
        );

        dao.insert(
                createEntity(
                        "CA",
                        "2048",
                        "A1",
                        null
                )
        );

        dao.insert(
                createEntity(
                        "CA",
                        "1000",
                        "A1",
                        null
                )
        );

        List<WarehouseItemEntity> items =
                getOrAwaitValue(
                        dao.search("A1")
                );

        assertEquals(3, items.size());
        assertEquals("CA", items.get(0).getCategory());
        assertEquals("1000", items.get(0).getCode());
        assertEquals("CA", items.get(1).getCategory());
        assertEquals("2048", items.get(1).getCode());
        assertEquals("MR", items.get(2).getCategory());
    }

    @Test
    public void filterMatchesExactCategory()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        "Nivel 2"
                )
        );

        dao.insert(
                createEntity(
                        "MRA",
                        "2000",
                        "A1",
                        "Nivel 2"
                )
        );

        List<WarehouseItemEntity> items =
                getOrAwaitValue(
                        dao.filter(
                                "",
                                "MR",
                                null,
                                0,
                                null
                        )
                );

        assertEquals(1, items.size());
        assertEquals("MR", items.get(0).getCategory());
    }

    @Test
    public void filterMatchesExactSiteWithoutIncludingPartialSite()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        null
                )
        );

        dao.insert(
                createEntity(
                        "MD",
                        "2000",
                        "A10",
                        null
                )
        );

        List<WarehouseItemEntity> items =
                getOrAwaitValue(
                        dao.filter(
                                "",
                                null,
                                "A1",
                                0,
                                null
                        )
                );

        assertEquals(1, items.size());
        assertEquals("A1", items.get(0).getSite());
    }

    @Test
    public void filterMatchesExactPosition()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        "Nivel 2"
                )
        );

        dao.insert(
                createEntity(
                        "MD",
                        "2000",
                        "A1",
                        "Nivel 3"
                )
        );

        List<WarehouseItemEntity> items =
                getOrAwaitValue(
                        dao.filter(
                                "",
                                null,
                                null,
                                2,
                                "Nivel 2"
                        )
                );

        assertEquals(1, items.size());
        assertEquals(
                "Nivel 2",
                items.get(0).getPosition()
        );
    }

    @Test
    public void filterMatchesItemsWithoutPosition()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        null
                )
        );

        dao.insert(
                createEntity(
                        "MD",
                        "2000",
                        "A1",
                        "Nivel 2"
                )
        );

        List<WarehouseItemEntity> items =
                getOrAwaitValue(
                        dao.filter(
                                "",
                                null,
                                null,
                                1,
                                null
                        )
                );

        assertEquals(1, items.size());
        assertNull(items.get(0).getPosition());
    }

    @Test
    public void filterCombinesQueryAndExactFilters()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        "Nivel 2"
                )
        );

        dao.insert(
                createEntity(
                        "MR",
                        "5000",
                        "A1",
                        "Nivel 2"
                )
        );

        dao.insert(
                createEntity(
                        "MD",
                        "1050",
                        "A1",
                        "Nivel 2"
                )
        );

        List<WarehouseItemEntity> items =
                getOrAwaitValue(
                        dao.filter(
                                "105",
                                "MR",
                                "A1",
                                2,
                                "Nivel 2"
                        )
                );

        assertEquals(1, items.size());
        assertEquals("MR", items.get(0).getCategory());
        assertEquals("1050", items.get(0).getCode());
    }

    @Test
    public void observeCategoriesReturnsDistinctOrderedValues()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1"
                )
        );

        dao.insert(
                createEntity(
                        "MR",
                        "2000",
                        "A2"
                )
        );

        dao.insert(
                createEntity(
                        "CA",
                        "3000",
                        "A3"
                )
        );

        List<String> categories =
                getOrAwaitValue(
                        dao.observeCategories()
                );

        assertEquals(2, categories.size());
        assertEquals("CA", categories.get(0));
        assertEquals("MR", categories.get(1));
    }

    @Test
    public void observeSitesReturnsDistinctOrderedValues()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "B1"
                )
        );

        dao.insert(
                createEntity(
                        "MD",
                        "2000",
                        "A1"
                )
        );

        dao.insert(
                createEntity(
                        "CA",
                        "3000",
                        "A1"
                )
        );

        List<String> sites =
                getOrAwaitValue(
                        dao.observeSites()
                );

        assertEquals(2, sites.size());
        assertEquals("A1", sites.get(0));
        assertEquals("B1", sites.get(1));
    }

    @Test
    public void observePositionsExcludesNullAndDuplicateValues()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        "Nivel 2"
                )
        );

        dao.insert(
                createEntity(
                        "MD",
                        "2000",
                        "A1",
                        "Nivel 2"
                )
        );

        dao.insert(
                createEntity(
                        "CA",
                        "3000",
                        "A1",
                        null
                )
        );

        List<String> positions =
                getOrAwaitValue(
                        dao.observePositions()
                );

        assertEquals(1, positions.size());
        assertEquals("Nivel 2", positions.get(0));
    }

    @Test
    public void observeWithoutPositionCountDetectsMissingPositions()
            throws Exception {

        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        null
                )
        );

        dao.insert(
                createEntity(
                        "MD",
                        "2000",
                        "A1",
                        "Nivel 2"
                )
        );

        Integer count =
                getOrAwaitValue(
                        dao.observeWithoutPositionCount()
                );

        assertEquals(Integer.valueOf(1), count);
    }

    @Test
    public void updateChangesExistingWarehouseItem()
            throws InterruptedException {

        long originalCreatedAt = 100L;

        long warehouseItemId = dao.insert(
                new WarehouseItemEntity(
                        0L,
                        "MR",
                        "1050",
                        "A1",
                        "Nivel 1",
                        "Observación inicial",
                        originalCreatedAt,
                        200L
                )
        );

        int affectedRows = dao.update(
                new WarehouseItemEntity(
                        warehouseItemId,
                        "MD",
                        "2050",
                        "B2",
                        null,
                        "Observación actualizada",
                        originalCreatedAt,
                        500L
                )
        );

        WarehouseItemEntity updated =
                getOrAwaitValue(
                        dao.observeById(warehouseItemId)
                );

        assertEquals(1, affectedRows);
        assertEquals(warehouseItemId, updated.getId());
        assertEquals("MD", updated.getCategory());
        assertEquals("2050", updated.getCode());
        assertEquals("B2", updated.getSite());
        assertNull(updated.getPosition());

        assertEquals(
                "Observación actualizada",
                updated.getObservations()
        );

        assertEquals(
                originalCreatedAt,
                updated.getCreatedAt()
        );

        assertEquals(
                500L,
                updated.getUpdatedAt()
        );
    }

    @Test
    public void updateDoesNotCreateAdditionalRow()
            throws InterruptedException {

        long warehouseItemId =
                dao.insert(
                        createEntity(
                                "MR",
                                "1050",
                                "A1"
                        )
                );

        WarehouseItemEntity original =
                getOrAwaitValue(
                        dao.observeById(warehouseItemId)
                );

        dao.update(
                new WarehouseItemEntity(
                        warehouseItemId,
                        original.getCategory(),
                        original.getCode(),
                        "B2",
                        null,
                        null,
                        original.getCreatedAt(),
                        original.getUpdatedAt() + 1L
                )
        );

        List<WarehouseItemEntity> items =
                getOrAwaitValue(dao.observeAll());

        assertEquals(1, items.size());
        assertEquals(warehouseItemId, items.get(0).getId());
    }

    @Test
    public void updateReturnsZeroWhenWarehouseItemDoesNotExist() {
        int affectedRows = dao.update(
                new WarehouseItemEntity(
                        999L,
                        "MR",
                        "1050",
                        "A1",
                        null,
                        null,
                        100L,
                        200L
                )
        );

        assertEquals(0, affectedRows);
    }

    @Test
    public void updateAllowsKeepingSameCategoryAndCode()
            throws InterruptedException {

        long warehouseItemId =
                dao.insert(
                        createEntity(
                                "MR",
                                "1050",
                                "A1"
                        )
                );

        WarehouseItemEntity original =
                getOrAwaitValue(
                        dao.observeById(warehouseItemId)
                );

        int affectedRows = dao.update(
                new WarehouseItemEntity(
                        warehouseItemId,
                        "MR",
                        "1050",
                        "B3",
                        null,
                        null,
                        original.getCreatedAt(),
                        original.getUpdatedAt() + 1L
                )
        );

        assertEquals(1, affectedRows);
    }

    @Test
    public void updateRejectsCategoryAndCodeOwnedByAnotherItem() {
        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1"
                )
        );

        long secondId =
                dao.insert(
                        createEntity(
                                "MD",
                                "1050",
                                "B1"
                        )
                );

        try {
            dao.update(
                    new WarehouseItemEntity(
                            secondId,
                            "MR",
                            "1050",
                            "B1",
                            null,
                            null,
                            100L,
                            200L
                    )
            );

            fail("Expected SQLiteConstraintException");
        } catch (SQLiteConstraintException expected) {
            // Expected unique index violation.
        }
    }

    @Test
    public void deleteById_deletesOnlySelectedWarehouseItem()
            throws InterruptedException {

        long firstId = dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        "Nivel 2"
                )
        );

        long secondId = dao.insert(
                createEntity(
                        "MD",
                        "1050",
                        "B3",
                        null
                )
        );

        int affectedRows =
                dao.deleteById(firstId);

        assertEquals(1, affectedRows);

        WarehouseItemEntity deleted =
                dao.findById(firstId);

        WarehouseItemEntity preserved =
                dao.findById(secondId);

        assertNull(deleted);
        assertNotNull(preserved);
        assertEquals("MD", preserved.getCategory());
        assertEquals("1050", preserved.getCode());
    }

    @Test
    public void deleteById_returnsZero_whenWarehouseItemDoesNotExist() {
        int affectedRows =
                dao.deleteById(999L);

        assertEquals(0, affectedRows);
    }

    @Test
    public void deleteById_updatesObservedWarehouseItems()
            throws InterruptedException {

        long warehouseItemId =
                dao.insert(
                        createEntity(
                                "MR",
                                "1050",
                                "A1",
                                null
                        )
                );

        List<WarehouseItemEntity> beforeDelete =
                getOrAwaitValue(
                        dao.observeAll()
                );

        assertEquals(1, beforeDelete.size());

        dao.deleteById(warehouseItemId);

        List<WarehouseItemEntity> afterDelete =
                getOrAwaitValue(
                        dao.observeAll()
                );

        assertTrue(afterDelete.isEmpty());
    }

    @Test
    public void existsByCategoryAndCodeReturnsTrueForExistingIdentity() {
        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1"
                )
        );

        boolean exists =
                dao.existsByCategoryAndCode(
                        "MR",
                        "1050"
                );

        assertTrue(exists);
    }

    @Test
    public void existsByCategoryAndCodeReturnsFalseForAvailableIdentity() {
        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1"
                )
        );

        boolean exists =
                dao.existsByCategoryAndCode(
                        "MR",
                        "2050"
                );

        assertFalse(exists);
    }

    @Test
    public void duplicateCheckIgnoresCategoryAndCodeCase() {
        dao.insert(
                createEntity(
                        "MR",
                        "AB-1050",
                        "A1"
                )
        );

        boolean exists =
                dao.existsByCategoryAndCode(
                        "mr",
                        "ab-1050"
                );

        assertTrue(exists);
    }

    @Test
    public void duplicateCheckAllowsSameCodeInDifferentCategory() {
        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1"
                )
        );

        boolean exists =
                dao.existsByCategoryAndCode(
                        "MD",
                        "1050"
                );

        assertFalse(exists);
    }

    @Test
    public void duplicateCheckExcludingIdIgnoresCurrentWarehouseItem() {
        long warehouseItemId = dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1"
                )
        );

        boolean exists =
                dao.existsByCategoryAndCodeExcludingId(
                        "MR",
                        "1050",
                        warehouseItemId
                );

        assertFalse(exists);
    }

    @Test
    public void duplicateCheckExcludingIdDetectsAnotherWarehouseItem() {
        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1"
                )
        );

        long secondId = dao.insert(
                createEntity(
                        "MD",
                        "1050",
                        "B1"
                )
        );

        boolean exists =
                dao.existsByCategoryAndCodeExcludingId(
                        "MR",
                        "1050",
                        secondId
                );

        assertTrue(exists);
    }

    @Test
    public void deletingWarehouseItemReleasesItsIdentity() {
        long warehouseItemId = dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1"
                )
        );

        assertTrue(
                dao.existsByCategoryAndCode(
                        "MR",
                        "1050"
                )
        );

        int affectedRows =
                dao.deleteById(warehouseItemId);

        assertEquals(1, affectedRows);

        assertFalse(
                dao.existsByCategoryAndCode(
                        "MR",
                        "1050"
                )
        );

        long recreatedId = dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "B2"
                )
        );

        assertTrue(recreatedId > 0L);
    }
}
