package com.rndymi.almacentracker.adapter.in.ui.state;

import com.rndymi.almacentracker.application.result.DeleteWarehouseItemsResult;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class WarehouseItemSelectionUiState {

    private final Set<Long> selectedIds;
    private final boolean deleting;
    private final UiEvent<DeleteWarehouseItemsResult> resultEvent;

    private WarehouseItemSelectionUiState(
            Set<Long> selectedIds,
            boolean deleting,
            UiEvent<DeleteWarehouseItemsResult> resultEvent
    ) {
        this.selectedIds =
                Collections.unmodifiableSet(
                        new LinkedHashSet<>(selectedIds)
                );

        this.deleting = deleting;
        this.resultEvent = resultEvent;
    }

    public static WarehouseItemSelectionUiState empty() {
        return new WarehouseItemSelectionUiState(
                Collections.emptySet(),
                false,
                null
        );
    }

    public static WarehouseItemSelectionUiState selecting(
            Set<Long> selectedIds
    ) {
        return new WarehouseItemSelectionUiState(
                selectedIds,
                false,
                null
        );
    }

    public static WarehouseItemSelectionUiState deleting(
            Set<Long> selectedIds
    ) {
        return new WarehouseItemSelectionUiState(
                selectedIds,
                true,
                null
        );
    }

    public static WarehouseItemSelectionUiState result(
            Set<Long> selectedIds,
            DeleteWarehouseItemsResult result
    ) {
        return new WarehouseItemSelectionUiState(
                selectedIds,
                false,
                new UiEvent<>(result)
        );
    }

    public Set<Long> getSelectedIds() {
        return selectedIds;
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    public boolean isSelectionMode() {
        return !selectedIds.isEmpty();
    }

    public boolean isDeleting() {
        return deleting;
    }

    public UiEvent<DeleteWarehouseItemsResult> getResultEvent() {
        return resultEvent;
    }
}