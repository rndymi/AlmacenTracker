package com.rndymi.almacentracker.feature.reference_list.location;

import com.rndymi.almacentracker.domain.reference.WarehouseReferenceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ReferenceListLocationResult {

    public enum Status {
        SUCCESS,
        INVALID_INPUT,
        ERROR
    }

    private final Status status;

    private final List<WarehouseReferenceLocation>
            locations;

    private final int foundCount;
    private final int notFoundCount;
    private final Throwable cause;

    private ReferenceListLocationResult(
            Status status,
            List<WarehouseReferenceLocation> locations,
            int foundCount,
            int notFoundCount,
            Throwable cause
    ) {
        this.status = Objects.requireNonNull(
                status,
                "status"
        );

        this.locations =
                Collections.unmodifiableList(
                        new ArrayList<>(locations)
                );

        this.foundCount = foundCount;
        this.notFoundCount = notFoundCount;
        this.cause = cause;
    }

    public static ReferenceListLocationResult success(
            List<WarehouseReferenceLocation> locations
    ) {
        List<WarehouseReferenceLocation> safeLocations =
                locations == null
                        ? Collections.emptyList()
                        : locations;

        int foundCount = 0;
        int notFoundCount = 0;

        for (
                WarehouseReferenceLocation location
                : safeLocations
        ) {
            if (location == null) {
                continue;
            }

            if (location.isFound()) {
                foundCount++;
            } else {
                notFoundCount++;
            }
        }

        return new ReferenceListLocationResult(
                Status.SUCCESS,
                safeLocations,
                foundCount,
                notFoundCount,
                null
        );
    }

    public static ReferenceListLocationResult
    invalidInput() {
        return new ReferenceListLocationResult(
                Status.INVALID_INPUT,
                Collections.emptyList(),
                0,
                0,
                null
        );
    }

    public static ReferenceListLocationResult error(
            Throwable cause
    ) {
        return new ReferenceListLocationResult(
                Status.ERROR,
                Collections.emptyList(),
                0,
                0,
                Objects.requireNonNull(
                        cause,
                        "cause"
                )
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
        return locations.size();
    }

    public Throwable getCause() {
        return cause;
    }
}