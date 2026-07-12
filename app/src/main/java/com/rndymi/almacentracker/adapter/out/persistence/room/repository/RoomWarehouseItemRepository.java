package com.rndymi.almacentracker.adapter.out.persistence.room.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.rndymi.almacentracker.adapter.out.persistence.room.dao.WarehouseItemDao;
import com.rndymi.almacentracker.adapter.out.persistence.room.mapper.WarehouseItemPersistenceMapper;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;

import java.util.Objects;

public final class RoomWarehouseItemRepository
        implements WarehouseItemRepository {
    private final WarehouseItemDao warehouseItemDao;
    private final WarehouseItemPersistenceMapper mapper;

    public RoomWarehouseItemRepository(
            WarehouseItemDao warehouseItemDao,
            WarehouseItemPersistenceMapper mapper
    ) {
        this.warehouseItemDao = Objects.requireNonNull(warehouseItemDao);
        this.mapper = Objects.requireNonNull(mapper);
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
                        return WarehouseItemsResult.error(exception);
                    }
                }
        );
    }
}