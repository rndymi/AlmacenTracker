package com.rndymi.almacentracker.data.local.room.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public final class AlmacenTrackerMigrations {

    public static final Migration MIGRATION_1_2 =
            new Migration(1, 2) {
                @Override
                public void migrate(
                        SupportSQLiteDatabase database
                ) {
                    database.execSQL(
                            "CREATE TABLE IF NOT EXISTS "
                                    + "`withdrawal_history` ("
                                    + "`id` INTEGER PRIMARY KEY "
                                    + "AUTOINCREMENT NOT NULL, "
                                    + "`title` TEXT, "
                                    + "`registered_at` INTEGER NOT NULL, "
                                    + "`created_at` INTEGER NOT NULL, "
                                    + "`updated_at` INTEGER NOT NULL"
                                    + ")"
                    );

                    database.execSQL(
                            "CREATE TABLE IF NOT EXISTS "
                                    + "`withdrawal_history_entries` ("
                                    + "`id` INTEGER PRIMARY KEY "
                                    + "AUTOINCREMENT NOT NULL, "
                                    + "`history_id` INTEGER NOT NULL, "
                                    + "`order_index` INTEGER NOT NULL, "
                                    + "`category` TEXT NOT NULL, "
                                    + "`code` TEXT NOT NULL, "
                                    + "`quantity` INTEGER, "
                                    + "`unit` TEXT, "
                                    + "`warehouse_item_id_snapshot` "
                                    + "INTEGER, "
                                    + "`site_snapshot` TEXT, "
                                    + "`position_snapshot` TEXT, "
                                    + "`location_status` TEXT NOT NULL, "
                                    + "FOREIGN KEY(`history_id`) "
                                    + "REFERENCES "
                                    + "`withdrawal_history`(`id`) "
                                    + "ON UPDATE NO ACTION "
                                    + "ON DELETE CASCADE"
                                    + ")"
                    );

                    database.execSQL(
                            "CREATE INDEX IF NOT EXISTS "
                                    + "`index_withdrawal_history_entries_"
                                    + "history_id` "
                                    + "ON `withdrawal_history_entries` "
                                    + "(`history_id`)"
                    );

                    database.execSQL(
                            "CREATE UNIQUE INDEX IF NOT EXISTS "
                                    + "`index_withdrawal_history_entries_"
                                    + "history_id_order_index` "
                                    + "ON `withdrawal_history_entries` "
                                    + "(`history_id`, `order_index`)"
                    );
                }
            };

    public static final Migration MIGRATION_2_3 =
            new Migration(2, 3) {

                @Override
                public void migrate(
                        SupportSQLiteDatabase database
                ) {
                    database.execSQL(
                            "ALTER TABLE "
                                    + "`withdrawal_history_entries` "
                                    + "ADD COLUMN "
                                    + "`destinations` TEXT"
                    );
                }
            };

    private AlmacenTrackerMigrations() {
        throw new AssertionError(
                "No instances"
        );
    }
}