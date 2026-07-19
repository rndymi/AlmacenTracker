package com.rndymi.almacentracker.adapter.out.persistence.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

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
                    "ORDER BY category COLLATE NOCASE ASC, " +
                    "code COLLATE NOCASE ASC"
    )
    List<WarehouseItemEntity> findAll();

    @Query(
            "SELECT * FROM warehouse_items " +
                    "WHERE category LIKE '%' || :query || '%' " +
                    "COLLATE NOCASE " +
                    "OR code LIKE '%' || :query || '%' " +
                    "COLLATE NOCASE " +
                    "OR site LIKE '%' || :query || '%' " +
                    "COLLATE NOCASE " +
                    "OR position LIKE '%' || :query || '%' " +
                    "COLLATE NOCASE " +
                    "ORDER BY category COLLATE NOCASE ASC, " +
                    "code COLLATE NOCASE ASC"
    )
    LiveData<List<WarehouseItemEntity>> search(String query);

    @Query(
            "SELECT * FROM warehouse_items " +
                    "WHERE (" +
                    "    :query = '' " +
                    "    OR category LIKE '%' || :query || '%' COLLATE NOCASE " +
                    "    OR code LIKE '%' || :query || '%' COLLATE NOCASE " +
                    "    OR site LIKE '%' || :query || '%' COLLATE NOCASE " +
                    "    OR position LIKE '%' || :query || '%' COLLATE NOCASE" +
                    ") " +
                    "AND (" +
                    "    :category IS NULL " +
                    "    OR category = :category COLLATE NOCASE" +
                    ") " +
                    "AND (" +
                    "    :site IS NULL " +
                    "    OR site = :site COLLATE NOCASE" +
                    ") " +
                    "AND (" +
                    "    :positionMode = 0 " +
                    "    OR (" +
                    "        :positionMode = 1 " +
                    "        AND (" +
                    "            position IS NULL " +
                    "            OR TRIM(position) = ''" +
                    "        )" +
                    "    ) " +
                    "    OR (" +
                    "        :positionMode = 2 " +
                    "        AND position = :position COLLATE NOCASE" +
                    "    )" +
                    ") " +
                    "ORDER BY category COLLATE NOCASE ASC, " +
                    "code COLLATE NOCASE ASC"
    )
    LiveData<List<WarehouseItemEntity>> filter(
            String query,
            String category,
            String site,
            int positionMode,
            String position
    );

    @Query(
            "SELECT DISTINCT category " +
                    "FROM warehouse_items " +
                    "WHERE TRIM(category) <> '' " +
                    "ORDER BY category COLLATE NOCASE ASC"
    )
    LiveData<List<String>> observeCategories();

    @Query(
            "SELECT DISTINCT site " +
                    "FROM warehouse_items " +
                    "WHERE TRIM(site) <> '' " +
                    "ORDER BY site COLLATE NOCASE ASC"
    )
    LiveData<List<String>> observeSites();

    @Query(
            "SELECT DISTINCT position " +
                    "FROM warehouse_items " +
                    "WHERE position IS NOT NULL " +
                    "AND TRIM(position) <> '' " +
                    "ORDER BY position COLLATE NOCASE ASC"
    )
    LiveData<List<String>> observePositions();

    @Query(
            "SELECT COUNT(*) FROM warehouse_items " +
                    "WHERE position IS NULL " +
                    "OR TRIM(position) = ''"
    )
    LiveData<Integer> observeWithoutPositionCount();

    @Query(
            "SELECT * FROM warehouse_items " +
                    "WHERE id = :warehouseItemId " +
                    "LIMIT 1"
    )
    LiveData<WarehouseItemEntity> observeById(
            long warehouseItemId
    );

    @Query(
            "SELECT * FROM warehouse_items " +
                    "WHERE id = :warehouseItemId " +
                    "LIMIT 1"
    )
    WarehouseItemEntity findById(
            long warehouseItemId
    );

    @Query(
            "SELECT EXISTS(" +
                    "SELECT 1 FROM warehouse_items " +
                    "WHERE category = :category COLLATE NOCASE " +
                    "AND code = :code COLLATE NOCASE" +
                    ")"
    )
    boolean existsByCategoryAndCode(
            String category,
            String code
    );

    @Query(
            "SELECT EXISTS(" +
                    "SELECT 1 FROM warehouse_items " +
                    "WHERE category = :category COLLATE NOCASE " +
                    "AND code = :code COLLATE NOCASE " +
                    "AND id <> :excludedWarehouseItemId" +
                    ")"
    )
    boolean existsByCategoryAndCodeExcludingId(
            String category,
            String code,
            long excludedWarehouseItemId
    );

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(WarehouseItemEntity entity);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    List<Long> insertAllInternal(
            List<WarehouseItemEntity> entities
    );

    @Transaction
    default List<Long> insertAll(
            List<WarehouseItemEntity> entities
    ) {
        return insertAllInternal(entities);
    }

    @Update(onConflict = OnConflictStrategy.ABORT)
    int update(WarehouseItemEntity entity);

    @Query(
            "DELETE FROM warehouse_items " +
                    "WHERE id = :warehouseItemId"
    )
    int deleteById(long warehouseItemId);

    @Query(
            "DELETE FROM warehouse_items " +
                    "WHERE id IN (:warehouseItemIds)"
    )
    int deleteByIds(List<Long> warehouseItemIds);

    @Query("DELETE FROM warehouse_items")
    void deleteAllInternal();

    @Transaction
    default List<Long> replaceAll(
            List<WarehouseItemEntity> entities
    ) {
        deleteAllInternal();

        if (entities == null || entities.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return insertAllInternal(entities);
    }
}