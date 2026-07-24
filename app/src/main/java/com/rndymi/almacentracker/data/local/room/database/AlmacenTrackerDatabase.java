package com.rndymi.almacentracker.data.local.room.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.rndymi.almacentracker.data.local.room.dao.WarehouseItemDao;
import com.rndymi.almacentracker.data.local.room.entity.WarehouseItemEntity;

@Database(
        entities = {
                WarehouseItemEntity.class
        },
        version = 1,
        exportSchema = true
)
public abstract class AlmacenTrackerDatabase extends RoomDatabase {
    public abstract WarehouseItemDao warehouseItemDao();
}