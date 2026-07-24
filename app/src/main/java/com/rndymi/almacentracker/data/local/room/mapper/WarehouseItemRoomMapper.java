package com.rndymi.almacentracker.data.local.room.mapper;

import com.rndymi.almacentracker.data.local.room.entity.WarehouseItemEntity;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.domain.rule.WarehouseItemNormalizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WarehouseItemRoomMapper {

    private static final WarehouseItemNormalizer NORMALIZER =
            new WarehouseItemNormalizer();

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
                NORMALIZER.normalizeOptional(
                        entity.getPosition()
                ),
                NORMALIZER.normalizeOptional(
                        entity.getObservations()
                ),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public WarehouseItemEntity toEntity(WarehouseItem item) {
        if (item == null) {
            throw new IllegalArgumentException(
                    "WarehouseItem cannot be null"
            );
        }
        return new WarehouseItemEntity(
                item.getId(),
                item.getCategory(),
                item.getCode(),
                item.getSite(),
                NORMALIZER.normalizeOptional(
                        item.getPosition()
                ),
                NORMALIZER.normalizeOptional(
                        item.getObservations()
                ),
                item.getCreatedAt(),
                item.getUpdatedAt()
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
}
