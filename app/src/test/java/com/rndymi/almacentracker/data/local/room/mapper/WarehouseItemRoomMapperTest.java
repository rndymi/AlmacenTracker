package com.rndymi.almacentracker.data.local.room.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.data.local.room.entity.WarehouseItemEntity;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class WarehouseItemRoomMapperTest {
    private final WarehouseItemRoomMapper mapper =
            new WarehouseItemRoomMapper();

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

    @Test
    public void mapsDomainItemToRoomEntity() {
        WarehouseItem item = new WarehouseItem(
                7L,
                "MR",
                "1050",
                "A1",
                "  Nivel 2  ",
                "   ",
                100L,
                200L
        );

        WarehouseItemEntity entity = mapper.toEntity(item);

        assertEquals(7L, entity.getId());
        assertEquals("MR", entity.getCategory());
        assertEquals("1050", entity.getCode());
        assertEquals("A1", entity.getSite());
        assertEquals("Nivel 2", entity.getPosition());
        assertNull(entity.getObservations());
        assertEquals(100L, entity.getCreatedAt());
        assertEquals(200L, entity.getUpdatedAt());
    }

    @Test
    public void mapsEntityListWithoutLeakingEntities() {
        WarehouseItemEntity entity =
                new WarehouseItemEntity(
                        1L,
                        "MR",
                        "1050",
                        "A1",
                        null,
                        null,
                        100L,
                        200L
                );

        List<WarehouseItem> items =
                mapper.toDomainList(
                        Collections.singletonList(entity)
                );

        assertEquals(1, items.size());
        assertEquals("MR", items.get(0).getCategory());
    }

    @Test
    public void nullAndEmptyListsMapToEmptyList() {
        assertTrue(mapper.toDomainList(null).isEmpty());
        assertTrue(
                mapper.toDomainList(
                        Collections.emptyList()
                ).isEmpty()
        );
    }

    @Test
    public void rejectsNullValuesAtMappingBoundary() {
        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toDomain(null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toEntity(null)
        );
    }
}
