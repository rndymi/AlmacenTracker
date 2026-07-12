package com.rndymi.almacentracker.adapter.out.persistence.room.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.rndymi.almacentracker.adapter.out.persistence.room.dao.WarehouseItemDao;
import com.rndymi.almacentracker.adapter.out.persistence.room.entity.WarehouseItemEntity;

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