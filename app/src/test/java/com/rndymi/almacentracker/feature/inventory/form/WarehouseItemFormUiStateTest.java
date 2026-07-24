package com.rndymi.almacentracker.feature.inventory.form;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class WarehouseItemFormUiStateTest {

    @Test
    public void createModeIsEditable() {
        assertTrue(
                WarehouseItemFormUiState
                        .createMode()
                        .isEditable()
        );
    }

    @Test
    public void loadingAndSavingCannotCoexist() {
        assertThrows(
                IllegalArgumentException.class,
                () -> state(
                        WarehouseItemFormMode.EDIT,
                        1L,
                        true,
                        true,
                        false,
                        false
                )
        );
    }

    @Test
    public void createModeCannotContainExistingItemId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> state(
                        WarehouseItemFormMode.CREATE,
                        1L,
                        false,
                        false,
                        false,
                        false
                )
        );
    }

    @Test
    public void editModeRequiresPositiveId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> state(
                        WarehouseItemFormMode.EDIT,
                        0L,
                        false,
                        false,
                        false,
                        false
                )
        );
    }

    @Test
    public void invalidIdStateRequiresInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> state(
                        WarehouseItemFormMode.EDIT,
                        1L,
                        false,
                        false,
                        false,
                        true
                )
        );
    }

    @Test
    public void notFoundStateIsNotEditable() {
        WarehouseItemFormUiState state = state(
                WarehouseItemFormMode.EDIT,
                1L,
                false,
                false,
                true,
                false
        );

        assertTrue(state.isNotFound());
        assertFalse(state.isEditable());
    }

    private WarehouseItemFormUiState state(
            WarehouseItemFormMode mode,
            long itemId,
            boolean loading,
            boolean saving,
            boolean notFound,
            boolean invalidId
    ) {
        return new WarehouseItemFormUiState(
                mode,
                itemId,
                "",
                "",
                "",
                "",
                "",
                null,
                null,
                null,
                null,
                loading,
                saving,
                notFound,
                invalidId
        );
    }
}
