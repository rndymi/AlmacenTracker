package com.rndymi.almacentracker.feature.inventory.list;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.application.result.DeleteWarehouseItemsResult;

import org.junit.Test;

import java.util.Collections;
import java.util.Set;

public final class WarehouseItemSelectionUiStateTest {

    @Test
    public void emptyStateIsNotDeletingOrSelecting() {
        WarehouseItemSelectionUiState state =
                WarehouseItemSelectionUiState.empty();

        assertFalse(state.isSelectionMode());
        assertFalse(state.isDeleting());
    }

    @Test
    public void selectingRequiresSelectedIds() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WarehouseItemSelectionUiState.selecting(
                        Collections.emptySet()
                )
        );
    }

    @Test
    public void deletingRequiresSelectedIds() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WarehouseItemSelectionUiState.deleting(
                        Collections.emptySet()
                )
        );
    }

    @Test
    public void selectedIdsMustBePositive() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WarehouseItemSelectionUiState.selecting(
                        Collections.singleton(0L)
                )
        );
    }

    @Test
    public void deletingStateHasSelection() {
        WarehouseItemSelectionUiState state =
                WarehouseItemSelectionUiState.deleting(
                        Set.of(1L, 2L)
                );

        assertTrue(state.isSelectionMode());
        assertTrue(state.isDeleting());
    }

    @Test
    public void resultMayClearSelectionAfterSuccess() {
        WarehouseItemSelectionUiState state =
                WarehouseItemSelectionUiState.result(
                        Collections.emptySet(),
                        DeleteWarehouseItemsResult.success(2)
                );

        assertFalse(state.isSelectionMode());
        assertTrue(state.getResultEvent() != null);
    }
}
