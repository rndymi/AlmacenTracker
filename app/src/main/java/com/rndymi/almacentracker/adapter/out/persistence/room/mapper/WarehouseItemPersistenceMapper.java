package com.rndymi.almacentracker.adapter.out.persistence.room.mapper;

import com.rndymi.almacentracker.adapter.out.persistence.room.entity.WarehouseItemEntity;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WarehouseItemPersistenceMapper {
    public WarehouseItem toDomain(WarehouseItemEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException(
                    "WarehouseItemEntity cannot be null"
            );
        }

        return new WarehouseItem(
                entity.getId(),
                entity.getCategory(),
                entity.getCode(),
                entity.getSite(),
                normalizeOptional(entity.getPosition()),
                normalizeOptional(entity.getObservations()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public List<WarehouseItem> toDomainList(
            List<WarehouseItemEntity> entities
    ) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        List<WarehouseItem> items = new ArrayList<>(entities.size());

        for (WarehouseItemEntity entity : entities) {
            items.add(toDomain(entity));
        }

        return items;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}