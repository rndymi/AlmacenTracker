package com.rndymi.almacentracker.data.local.room;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rndymi.almacentracker.data.local.room.dao.WarehouseItemDao;
import com.rndymi.almacentracker.data.local.room.database.AlmacenTrackerDatabase;
import com.rndymi.almacentracker.data.local.room.entity.WarehouseItemEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WarehouseItemPersistenceTest {

    private static final String TEST_DATABASE_NAME =
            "warehouse_item_persistence_test.db";

    private Context context;
    private AlmacenTrackerDatabase database;
    private WarehouseItemDao dao;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        context.deleteDatabase(TEST_DATABASE_NAME);

        openDatabase();
    }

    @After
    public void tearDown() {
        closeDatabase();

        context.deleteDatabase(TEST_DATABASE_NAME);
    }

    @Test
    public void insertedWarehouseItemPersistsAfterDatabaseReopens() {
        long warehouseItemId = dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        "Nivel 2",
                        "Caja revisada"
                )
        );

        reopenDatabase();

        WarehouseItemEntity persistedItem =
                dao.findById(warehouseItemId);

        assertNotNull(persistedItem);
        assertEquals(
                warehouseItemId,
                persistedItem.getId()
        );
        assertEquals(
                "MR",
                persistedItem.getCategory()
        );
        assertEquals(
                "1050",
                persistedItem.getCode()
        );
        assertEquals(
                "A1",
                persistedItem.getSite()
        );
        assertEquals(
                "Nivel 2",
                persistedItem.getPosition()
        );
        assertEquals(
                "Caja revisada",
                persistedItem.getObservations()
        );
    }

    @Test
    public void warehouseItemWithoutPositionPersistsAfterReopen() {
        long warehouseItemId = dao.insert(
                createEntity(
                        "MD",
                        "2050",
                        "B1",
                        null,
                        null
                )
        );

        reopenDatabase();

        WarehouseItemEntity persistedItem =
                dao.findById(warehouseItemId);

        assertNotNull(persistedItem);
        assertEquals(
                "MD",
                persistedItem.getCategory()
        );
        assertEquals(
                "2050",
                persistedItem.getCode()
        );
        assertNull(persistedItem.getPosition());
        assertNull(persistedItem.getObservations());
    }

    @Test
    public void uniqueCategoryAndCodeConstraintSurvivesReopen() {
        dao.insert(
                createEntity(
                        "MR",
                        "1050",
                        "A1",
                        null,
                        null
                )
        );

        reopenDatabase();

        assertTrue(
                dao.existsByCategoryAndCode(
                        "MR",
                        "1050"
                )
        );

        assertThrows(
                SQLiteConstraintException.class,
                () -> dao.insert(
                        createEntity(
                                "MR",
                                "1050",
                                "B3",
                                "Nivel 3",
                                null
                        )
                )
        );

        assertFalse(
                dao.existsByCategoryAndCode(
                        "MD",
                        "1050"
                )
        );

        long differentCategoryId = dao.insert(
                createEntity(
                        "MD",
                        "1050",
                        "B3",
                        "Nivel 3",
                        null
                )
        );

        assertTrue(differentCategoryId > 0L);
    }

    private void openDatabase() {
        database = Room.databaseBuilder(
                context,
                AlmacenTrackerDatabase.class,
                TEST_DATABASE_NAME
        ).allowMainThreadQueries().build();

        dao = database.warehouseItemDao();
    }

    private void reopenDatabase() {
        closeDatabase();
        openDatabase();
    }

    private void closeDatabase() {
        if (database != null && database.isOpen()) {
            database.close();
        }

        database = null;
        dao = null;
    }

    private WarehouseItemEntity createEntity(
            String category,
            String code,
            String site,
            String position,
            String observations
    ) {
        long now = System.currentTimeMillis();

        return new WarehouseItemEntity(
                0L,
                category,
                code,
                site,
                position,
                observations,
                now,
                now
        );
    }
}
