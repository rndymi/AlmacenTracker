package com.rndymi.almacentracker.feature.reference_list.location;

import com.rndymi.almacentracker.domain.reference.WarehouseReferenceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReferenceListLocationUiState {

    public enum Status {
        IDLE,
        LOADING,
        CONTENT,
        INVALID_INPUT,
        ERROR
    }

    private final Status status;

    private final List<WarehouseReferenceLocation>
            locations;

    private final int foundCount;
    private final int notFoundCount;

    private ReferenceListLocationUiState(
            Status status,
            List<WarehouseReferenceLocation> locations,
            int foundCount,
            int notFoundCount
    ) {
        this.status = status;

        this.locations =
                Collections.unmodifiableList(
                        new ArrayList<>(locations)
                );

        this.foundCount = foundCount;
        this.notFoundCount = notFoundCount;
    }

    public static ReferenceListLocationUiState idle() {
        return new ReferenceListLocationUiState(
                Status.IDLE,
                Collections.emptyList(),
                0,
                0
        );
    }

    public static ReferenceListLocationUiState loading() {
        return new ReferenceListLocationUiState(
                Status.LOADING,
                Collections.emptyList(),
                0,
                0
        );
    }

    public static ReferenceListLocationUiState content(
            ReferenceListLocationResult result
    ) {
        return new ReferenceListLocationUiState(
                Status.CONTENT,
                result.getLocations(),
                result.getFoundCount(),
                result.getNotFoundCount()
        );
    }

    public static ReferenceListLocationUiState
    invalidInput() {
        return new ReferenceListLocationUiState(
                Status.INVALID_INPUT,
                Collections.emptyList(),
                0,
                0
        );
    }

    public static ReferenceListLocationUiState error() {
        return new ReferenceListLocationUiState(
                Status.ERROR,
                Collections.emptyList(),
                0,
                0
        );
    }

    public Status getStatus() {
        return status;
    }

    public List<WarehouseReferenceLocation>
    getLocations() {
        return locations;
    }

    public int getFoundCount() {
        return foundCount;
    }

    public int getNotFoundCount() {
        return notFoundCount;
    }

    public int getTotalCount() {
        return foundCount + notFoundCount;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean hasContent() {
        return status == Status.CONTENT;
    }

    public boolean canRetry() {
        return status == Status.ERROR;
    }
}