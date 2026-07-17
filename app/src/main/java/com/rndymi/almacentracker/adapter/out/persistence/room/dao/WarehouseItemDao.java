package com.rndymi.almacentracker.adapter.out.persistence.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.rndymi.almacentracker.adapter.out.persistence.room.entity.WarehouseItemEntity;

import java.util.List;

@Dao
public interface WarehouseItemDao {
    @Query(
            "SELECT * FROM warehouse_items " +
                    "ORDER BY category COLLATE NOCASE ASC, " +
                    "code COLLATE NOCASE ASC"
    )
    LiveData<List<WarehouseItemEntity>> observeAll();

    @Query(
            "SELECT * FROM warehouse_items " +
                    "WHERE id = :warehouseItemId " +
                    "LIMIT 1"
    )
    LiveData<WarehouseItemEntity> observeById(
            long warehouseItemId
    );
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(WarehouseItemEntity entity);

    @Query("DELETE FROM warehouse_items")
    void deleteAll();
}
