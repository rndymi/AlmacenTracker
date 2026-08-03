package com.rndymi.almacentracker.data.local.room.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;

import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class AlmacenTrackerDatabaseMigrationTest {

    private static final String TEST_DATABASE =
            "almacen-tracker-migration-test";

    private Context context;
    private AlmacenTrackerDatabase roomDatabase;

    @Before
    public void setUp() {
        context = InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext();
        context.deleteDatabase(TEST_DATABASE);
    }

    @After
    public void tearDown() {
        if (roomDatabase != null) {
            roomDatabase.close();
        }
        context.deleteDatabase(TEST_DATABASE);
    }

    @Test
    public void migrationFrom1To2PreservesWarehouseItems()
            throws IOException {

        SupportSQLiteDatabase versionOneDatabase =
                createVersionOneDatabase();

        versionOneDatabase.execSQL(
                "INSERT INTO `warehouse_items` ("
                        + "`id`, "
                        + "`category`, "
                        + "`code`, "
                        + "`site`, "
                        + "`position`, "
                        + "`observations`, "
                        + "`created_at`, "
                        + "`updated_at`"
                        + ") VALUES ("
                        + "7, "
                        + "'MR', "
                        + "'001210A', "
                        + "'A1', "
                        + "'Nivel 2', "
                        + "'Revisar embalaje', "
                        + "1000, "
                        + "2000"
                        + ")"
        );

        versionOneDatabase.close();

        SupportSQLiteDatabase versionTwoDatabase =
                migrateToVersionTwo();

        assertWarehouseItemWasPreserved(
                versionTwoDatabase
        );

        assertTableExists(
                versionTwoDatabase,
                "withdrawal_history"
        );
        assertTableExists(
                versionTwoDatabase,
                "withdrawal_history_entries"
        );

        assertTableIsEmpty(
                versionTwoDatabase,
                "withdrawal_history"
        );
        assertTableIsEmpty(
                versionTwoDatabase,
                "withdrawal_history_entries"
        );

    }

    @Test
    public void migratedDatabaseEnforcesHistoryForeignKey()
            throws IOException {

        SupportSQLiteDatabase versionOneDatabase =
                createVersionOneDatabase();

        versionOneDatabase.close();

        SupportSQLiteDatabase versionTwoDatabase =
                migrateToVersionTwo();

        versionTwoDatabase.execSQL(
                "INSERT INTO `withdrawal_history` ("
                        + "`id`, "
                        + "`title`, "
                        + "`registered_at`, "
                        + "`created_at`, "
                        + "`updated_at`"
                        + ") VALUES ("
                        + "1, "
                        + "NULL, "
                        + "1000, "
                        + "1000, "
                        + "1000"
                        + ")"
        );

        versionTwoDatabase.execSQL(
                "INSERT INTO `withdrawal_history_entries` ("
                        + "`id`, "
                        + "`history_id`, "
                        + "`order_index`, "
                        + "`category`, "
                        + "`code`, "
                        + "`quantity`, "
                        + "`unit`, "
                        + "`warehouse_item_id_snapshot`, "
                        + "`site_snapshot`, "
                        + "`position_snapshot`, "
                        + "`location_status`"
                        + ") VALUES ("
                        + "1, "
                        + "1, "
                        + "0, "
                        + "'MR', "
                        + "'001210A', "
                        + "4, "
                        + "'CAJAS', "
                        + "7, "
                        + "'A1', "
                        + "NULL, "
                        + "'FOUND'"
                        + ")"
        );

        versionTwoDatabase.execSQL(
                "DELETE FROM `withdrawal_history` "
                        + "WHERE `id` = 1"
        );

        assertTableIsEmpty(
                versionTwoDatabase,
                "withdrawal_history_entries"
        );

    }

    private SupportSQLiteDatabase createVersionOneDatabase() {
        SupportSQLiteOpenHelper.Configuration configuration =
                SupportSQLiteOpenHelper.Configuration
                        .builder(context)
                        .name(TEST_DATABASE)
                        .callback(
                                new SupportSQLiteOpenHelper.Callback(1) {
                                    @Override
                                    public void onCreate(
                                            SupportSQLiteDatabase database
                                    ) {
                                        createVersionOneSchema(database);
                                    }

                                    @Override
                                    public void onUpgrade(
                                            SupportSQLiteDatabase database,
                                            int oldVersion,
                                            int newVersion
                                    ) {
                                    }
                                }
                        )
                        .build();

        return new FrameworkSQLiteOpenHelperFactory()
                .create(configuration)
                .getWritableDatabase();
    }

    private void createVersionOneSchema(
            SupportSQLiteDatabase database
    ) {
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `warehouse_items` ("
                        + "`id` INTEGER PRIMARY KEY AUTOINCREMENT "
                        + "NOT NULL, "
                        + "`category` TEXT, "
                        + "`code` TEXT, "
                        + "`site` TEXT, "
                        + "`position` TEXT, "
                        + "`observations` TEXT, "
                        + "`created_at` INTEGER NOT NULL, "
                        + "`updated_at` INTEGER NOT NULL"
                        + ")"
        );
        database.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS "
                        + "`index_warehouse_items_category_code` "
                        + "ON `warehouse_items` (`category`, `code`)"
        );
    }

    private SupportSQLiteDatabase migrateToVersionTwo() {
        roomDatabase = Room.databaseBuilder(
                context,
                AlmacenTrackerDatabase.class,
                TEST_DATABASE
        ).addMigrations(
                AlmacenTrackerMigrations.MIGRATION_1_2
        ).allowMainThreadQueries().build();

        return roomDatabase
                .getOpenHelper()
                .getWritableDatabase();
    }

    private void assertWarehouseItemWasPreserved(
            SupportSQLiteDatabase database
    ) {
        try (
                Cursor cursor = database.query(
                        "SELECT * FROM `warehouse_items` "
                                + "WHERE `id` = 7"
                )
        ) {
            assertTrue(cursor.moveToFirst());

            assertEquals(
                    "MR",
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "category"
                            )
                    )
            );
            assertEquals(
                    "001210A",
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "code"
                            )
                    )
            );
            assertEquals(
                    "A1",
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "site"
                            )
                    )
            );
            assertEquals(
                    "Nivel 2",
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "position"
                            )
                    )
            );
            assertEquals(
                    "Revisar embalaje",
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "observations"
                            )
                    )
            );
            assertEquals(
                    1000L,
                    cursor.getLong(
                            cursor.getColumnIndexOrThrow(
                                    "created_at"
                            )
                    )
            );
            assertEquals(
                    2000L,
                    cursor.getLong(
                            cursor.getColumnIndexOrThrow(
                                    "updated_at"
                            )
                    )
            );
        }
    }

    private void assertTableExists(
            SupportSQLiteDatabase database,
            String tableName
    ) {
        try (
                Cursor cursor = database.query(
                        "SELECT name FROM sqlite_master "
                                + "WHERE type = 'table' "
                                + "AND name = ?",
                        new Object[]{tableName}
                )
        ) {
            assertTrue(cursor.moveToFirst());
        }
    }

    private void assertTableIsEmpty(
            SupportSQLiteDatabase database,
            String tableName
    ) {
        try (
                Cursor cursor = database.query(
                        "SELECT COUNT(*) FROM `"
                                + tableName
                                + "`"
                )
        ) {
            assertTrue(cursor.moveToFirst());
            assertEquals(0, cursor.getInt(0));
            assertFalse(cursor.moveToNext());
        }
    }
}
