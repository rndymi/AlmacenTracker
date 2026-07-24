package com.rndymi.almacentracker.feature.inventory.detail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

public final class WarehouseItemDetailUiStateTest {

    @Test
    public void notFoundCannotExposeContentOrActions() {
        WarehouseItemDetailUiState state =
                WarehouseItemDetailUiState.notFound();

        assertEquals(
                WarehouseItemDetailUiState.Status.NOT_FOUND,
                state.getStatus()
        );
        assertTrue(state.getWarehouseItem() == null);
        assertFalse(state.isDeleting());
    }

    @Test
    public void deletingRequiresContent() {
        assertThrows(
                NullPointerException.class,
                () -> WarehouseItemDetailUiState.deleting(null)
        );
    }

    @Test
    public void deletingStateContainsItem() {
        WarehouseItem item = item();
        WarehouseItemDetailUiState state =
                WarehouseItemDetailUiState.deleting(item);

        assertEquals(
                WarehouseItemDetailUiState.Status.CONTENT,
                state.getStatus()
        );
        assertEquals(item, state.getWarehouseItem());
        assertTrue(state.isDeleting());
    }

    @Test
    public void errorStatesRequireMessages() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WarehouseItemDetailUiState.error(" ")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> WarehouseItemDetailUiState.deleteError(
                        item(),
                        null
                )
        );
    }

    private WarehouseItem item() {
        return new WarehouseItem(
                1L,
                "MR",
                "1050",
                "A1",
                null,
                null,
                100L,
                100L
        );
    }
}
