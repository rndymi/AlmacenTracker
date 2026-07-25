package com.rndymi.almacentracker.feature.inventory.list;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WarehouseItemCodeSearchResult {

    public enum Status {
        SINGLE_MATCH,
        MULTIPLE_MATCHES,
        NOT_FOUND,
        INVALID_CODE,
        ERROR
    }

    private final Status status;
    private final String scannedCode;
    private final List<WarehouseItem> matches;
    private final Throwable cause;

    private WarehouseItemCodeSearchResult(
            Status status,
            String scannedCode,
            List<WarehouseItem> matches,
            Throwable cause
    ) {
        this.status = Objects.requireNonNull(status);
        this.scannedCode =
                scannedCode == null ? "" : scannedCode;

        List<WarehouseItem> safeMatches =
                matches == null
                        ? Collections.emptyList()
                        : new ArrayList<>(matches);

        this.matches =
                Collections.unmodifiableList(safeMatches);

        this.cause = cause;
    }

    public static WarehouseItemCodeSearchResult singleMatch(
            String scannedCode,
            WarehouseItem match
    ) {
        return new WarehouseItemCodeSearchResult(
                Status.SINGLE_MATCH,
                scannedCode,
                Collections.singletonList(
                        Objects.requireNonNull(match)
                ),
                null
        );
    }

    public static WarehouseItemCodeSearchResult multipleMatches(
            String scannedCode,
            List<WarehouseItem> matches
    ) {
        Objects.requireNonNull(matches);

        if (matches.size() < 2) {
            throw new IllegalArgumentException(
                    "Multiple matches require at least two items"
            );
        }

        return new WarehouseItemCodeSearchResult(
                Status.MULTIPLE_MATCHES,
                scannedCode,
                matches,
                null
        );
    }

    public static WarehouseItemCodeSearchResult notFound(
            String scannedCode
    ) {
        return new WarehouseItemCodeSearchResult(
                Status.NOT_FOUND,
                scannedCode,
                Collections.emptyList(),
                null
        );
    }

    public static WarehouseItemCodeSearchResult invalidCode() {
        return new WarehouseItemCodeSearchResult(
                Status.INVALID_CODE,
                "",
                Collections.emptyList(),
                null
        );
    }

    public static WarehouseItemCodeSearchResult error(
            String scannedCode,
            Throwable cause
    ) {
        return new WarehouseItemCodeSearchResult(
                Status.ERROR,
                scannedCode,
                Collections.emptyList(),
                Objects.requireNonNull(cause)
        );
    }

    public Status getStatus() {
        return status;
    }

    public String getScannedCode() {
        return scannedCode;
    }

    public List<WarehouseItem> getMatches() {
        return matches;
    }

    public WarehouseItem getSingleMatch() {
        if (status != Status.SINGLE_MATCH
                || matches.size() != 1) {
            throw new IllegalStateException(
                    "The result does not contain a single match"
            );
        }

        return matches.get(0);
    }

    public Throwable getCause() {
        return cause;
    }
}