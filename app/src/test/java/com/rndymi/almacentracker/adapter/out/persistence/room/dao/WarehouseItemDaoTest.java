package com.rndymi.almacentracker.adapter.out.persistence.room.dao;

import static org.junit.Assert.assertEquals;

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
        long now = System.currentTimeMillis();

        return new WarehouseItemEntity(
                0L,
                category,
                code,
                site,
                null,
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
}