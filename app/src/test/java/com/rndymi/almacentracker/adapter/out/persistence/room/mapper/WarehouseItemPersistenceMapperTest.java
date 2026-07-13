package com.rndymi.almacentracker.adapter.out.persistence.room.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.adapter.out.persistence.room.entity.WarehouseItemEntity;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

public class WarehouseItemPersistenceMapperTest {
    private final WarehouseItemPersistenceMapper mapper =
            new WarehouseItemPersistenceMapper();

    @Test
    public void mapsExistingPosition() {
        WarehouseItemEntity entity = new WarehouseItemEntity(
                1L,
                "MR",
                "1050",
                "A1",
                "Nivel 2",
                null,
                100L,
                100L
        );

        WarehouseItem item = mapper.toDomain(entity);

        assertTrue(item.hasPosition());
        assertEquals("Nivel 2", item.getPosition());
    }

    @Test
    public void convertsBlankPositionToNull() {
        WarehouseItemEntity entity = new WarehouseItemEntity(
                1L,
                "MD",
                "1050",
                "B3",
                "   ",
                null,
                100L,
                100L
        );

        WarehouseItem item = mapper.toDomain(entity);

        assertFalse(item.hasPosition());
        assertNull(item.getPosition());
    }
}