package com.rndymi.almacentracker.adapter.out.persistence.room.repository;

import android.database.sqlite.SQLiteConstraintException;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.rndymi.almacentracker.adapter.out.persistence.room.dao.WarehouseItemDao;
import com.rndymi.almacentracker.adapter.out.persistence.room.entity.WarehouseItemEntity;
import com.rndymi.almacentracker.adapter.out.persistence.room.mapper.WarehouseItemPersistenceMapper;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Objects;
import java.util.concurrent.Executor;

public final class RoomWarehouseItemRepository
        implements WarehouseItemRepository {
    private final WarehouseItemDao warehouseItemDao;
    private final WarehouseItemPersistenceMapper mapper;
    private final Executor executor;

    public RoomWarehouseItemRepository(
            WarehouseItemDao warehouseItemDao,
            WarehouseItemPersistenceMapper mapper,
            Executor executor
    ) {
        this.warehouseItemDao = Objects.requireNonNull(
                warehouseItemDao
        );
        this.mapper = Objects.requireNonNull(mapper);
        this.executor = Objects.requireNonNull(executor);
    }

    @Override
    public LiveData<WarehouseItemsResult> observeAll() {
        return Transformations.map(
                warehouseItemDao.observeAll(),
                entities -> {
                    try {
                        return WarehouseItemsResult.success(
                                mapper.toDomainList(entities)
                        );
                    } catch (RuntimeException exception) {
                        return WarehouseItemsResult.error(
                                exception
                        );
                    }
                }
        );
    }

    @Override
    public LiveData<WarehouseItemsResult> search(String query) {
        Objects.requireNonNull(query);

        return Transformations.map(
                warehouseItemDao.search(query),
                entities -> {
                    try {
                        return WarehouseItemsResult.success(
                                mapper.toDomainList(entities)
                        );
                    } catch (RuntimeException exception) {
                        return WarehouseItemsResult.error(
                                exception
                        );
                    }
                }
        );
    }

    @Override
    public LiveData<WarehouseItemDetailResult> observeById(
            long warehouseItemId
    ) {
        return Transformations.map(
                warehouseItemDao.observeById(warehouseItemId),
                entity -> {
                    if (entity == null) {
                        return WarehouseItemDetailResult.notFound();
                    }

                    try {
                        return WarehouseItemDetailResult.found(
                                mapper.toDomain(entity)
                        );
                    } catch (RuntimeException exception) {
                        return WarehouseItemDetailResult.error(
                                exception
                        );
                    }
                }
        );
    }

    @Override
    public void insert(
            WarehouseItem warehouseItem,
            WarehouseItemInsertCallback callback
    ) {
        Objects.requireNonNull(warehouseItem);
        Objects.requireNonNull(callback);

        executor.execute(() -> {
            try {
                WarehouseItemEntity entity =
                        mapper.toEntity(warehouseItem);

                long generatedId =
                        warehouseItemDao.insert(entity);

                callback.onSuccess(generatedId);
            } catch (SQLiteConstraintException exception) {
                callback.onDuplicate();
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }
}