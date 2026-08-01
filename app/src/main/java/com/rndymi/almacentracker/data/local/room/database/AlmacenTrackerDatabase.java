package com.rndymi.almacentracker.data.local.room.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.rndymi.almacentracker.data.local.room.dao.WarehouseItemDao;
import com.rndymi.almacentracker.data.local.room.dao.WithdrawalHistoryDao;
import com.rndymi.almacentracker.data.local.room.entity.WarehouseItemEntity;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntity;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntryEntity;

@Database(
        entities = {
                WarehouseItemEntity.class,
                WithdrawalHistoryEntity.class,
                WithdrawalHistoryEntryEntity.class
        },
        version = 2,
        exportSchema = true
)
public abstract class AlmacenTrackerDatabase
        extends RoomDatabase {

    public abstract WarehouseItemDao warehouseItemDao();

    public abstract WithdrawalHistoryDao withdrawalHistoryDao();
}