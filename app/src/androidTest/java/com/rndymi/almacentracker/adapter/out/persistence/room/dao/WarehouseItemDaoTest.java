package com.rndymi.almacentracker.adapter.out.persistence.room.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

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
import java.util.concurrent.atomic.AtomicReference;

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
}