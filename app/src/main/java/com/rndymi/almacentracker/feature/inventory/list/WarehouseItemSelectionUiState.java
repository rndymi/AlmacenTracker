package com.rndymi.almacentracker.feature.inventory.list;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.feature.inventory.common.WarehouseItemDeleteResult;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class WarehouseItemSelectionUiState {

    private final Set<Long> selectedIds;
    private final boolean deleting;
    private final UiEvent<WarehouseItemDeleteResult> resultEvent;

    private WarehouseItemSelectionUiState(
            Set<Long> selectedIds,
            boolean deleting,
            UiEvent<WarehouseItemDeleteResult> resultEvent
    ) {
        Objects.requireNonNull(selectedIds);

        for (Long selectedId : selectedIds) {
            if (selectedId == null || selectedId <= 0L) {
                throw new IllegalArgumentException(
                        "Selected IDs must be positive"
                );
            }
        }

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
        requireSelection(selectedIds, "Selecting");

        return new WarehouseItemSelectionUiState(
                selectedIds,
                false,
                null
        );
    }

    public static WarehouseItemSelectionUiState deleting(
            Set<Long> selectedIds
    ) {
        requireSelection(selectedIds, "Deleting");

        return new WarehouseItemSelectionUiState(
                selectedIds,
                true,
                null
        );
    }

    public static WarehouseItemSelectionUiState result(
            Set<Long> selectedIds,
            WarehouseItemDeleteResult result
    ) {
        Objects.requireNonNull(result);

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

    public UiEvent<WarehouseItemDeleteResult> getResultEvent() {
        return resultEvent;
    }

    private static void requireSelection(
            Set<Long> selectedIds,
            String stateName
    ) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            throw new IllegalArgumentException(
                    stateName + " requires a selection"
            );
        }
    }
}
