package com.rndymi.almacentracker.feature.data_management.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public final class DataManagementUiStateTest {

    @Test
    public void operationStateHasNoIncompatiblePayload() {
        DataManagementUiState state =
                DataManagementUiState.exporting();

        assertEquals(
                DataManagementUiState.Status.EXPORTING,
                state.getStatus()
        );
        assertNull(state.getMessage());
        assertEquals(0, state.getPendingRestoreCount());
    }

    @Test
    public void backupReadyAllowsEmptyValidatedBackup() {
        DataManagementUiState state =
                DataManagementUiState.backupReady(0);

        assertEquals(
                DataManagementUiState.Status.BACKUP_READY,
                state.getStatus()
        );
        assertEquals(0, state.getPendingRestoreCount());
    }

    @Test
    public void backupReadyRejectsNegativeCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DataManagementUiState.backupReady(-1)
        );
    }

    @Test
    public void emptyAndErrorRequireMessages() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DataManagementUiState.empty("   ")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> DataManagementUiState.error(null)
        );
    }
}
