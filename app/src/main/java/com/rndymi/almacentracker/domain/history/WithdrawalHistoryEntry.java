package com.rndymi.almacentracker.domain.history;

import java.util.Objects;
import java.util.Collections;
import java.util.List;

public final class WithdrawalHistoryEntry {

    private final long id;
    private final long historyId;
    private final int orderIndex;
    private final String category;
    private final String code;
    private final Integer quantity;
    private final String unit;
    private final Long warehouseItemIdSnapshot;
    private final String siteSnapshot;
    private final String positionSnapshot;
    private final WithdrawalLocationStatus locationStatus;
    private final List<String> destinations;

    public WithdrawalHistoryEntry(
            long id,
            long historyId,
            int orderIndex,
            String category,
            String code,
            Integer quantity,
            String unit,
            Long warehouseItemIdSnapshot,
            String siteSnapshot,
            String positionSnapshot,
            WithdrawalLocationStatus locationStatus
    ) {
        this(
                id,
                historyId,
                orderIndex,
                category,
                code,
                quantity,
                unit,
                warehouseItemIdSnapshot,
                siteSnapshot,
                positionSnapshot,
                locationStatus,
                Collections.emptyList()
        );
    }

    public WithdrawalHistoryEntry(
            long id,
            long historyId,
            int orderIndex,
            String category,
            String code,
            Integer quantity,
            String unit,
            Long warehouseItemIdSnapshot,
            String siteSnapshot,
            String positionSnapshot,
            WithdrawalLocationStatus locationStatus,
            List<String> destinations
    ) {
        if (id < 0L) {
            throw new IllegalArgumentException(
                    "Entry id cannot be negative"
            );
        }

        if (historyId < 0L) {
            throw new IllegalArgumentException(
                    "History id cannot be negative"
            );
        }

        if (orderIndex < 0) {
            throw new IllegalArgumentException(
                    "Order index cannot be negative"
            );
        }

        this.category = requireText(
                category,
                "Category cannot be blank"
        );
        this.code = requireText(
                code,
                "Code cannot be blank"
        );

        if (quantity != null && quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be positive"
            );
        }

        this.locationStatus = Objects.requireNonNull(
                locationStatus,
                "Location status cannot be null"
        );
        this.destinations =
                WithdrawalDestinationCodec
                        .immutableCopy(destinations);

        String normalizedUnit = normalizeOptional(unit);
        String normalizedSite = normalizeOptional(siteSnapshot);
        String normalizedPosition =
                normalizeOptional(positionSnapshot);

        validateLocationSnapshot(
                locationStatus,
                warehouseItemIdSnapshot,
                normalizedSite,
                normalizedPosition
        );

        this.id = id;
        this.historyId = historyId;
        this.orderIndex = orderIndex;
        this.quantity = quantity;
        this.unit = normalizedUnit;
        this.warehouseItemIdSnapshot =
                warehouseItemIdSnapshot;
        this.siteSnapshot = normalizedSite;
        this.positionSnapshot = normalizedPosition;
    }

    public long getId() {
        return id;
    }

    public long getHistoryId() {
        return historyId;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public Long getWarehouseItemIdSnapshot() {
        return warehouseItemIdSnapshot;
    }

    public String getSiteSnapshot() {
        return siteSnapshot;
    }

    public String getPositionSnapshot() {
        return positionSnapshot;
    }

    public WithdrawalLocationStatus getLocationStatus() {
        return locationStatus;
    }

    public List<String> getDestinations() {
        return destinations;
    }

    public boolean hasQuantity() {
        return quantity != null;
    }

    public boolean hasUnit() {
        return unit != null;
    }

    public boolean hasPositionSnapshot() {
        return positionSnapshot != null;
    }

    private void validateLocationSnapshot(
            WithdrawalLocationStatus status,
            Long warehouseItemId,
            String site,
            String position
    ) {
        if (status == WithdrawalLocationStatus.FOUND) {
            if (warehouseItemId == null
                    || warehouseItemId <= 0L) {
                throw new IllegalArgumentException(
                        "Found entries require a valid warehouse item id"
                );
            }

            if (site == null) {
                throw new IllegalArgumentException(
                        "Found entries require a site snapshot"
                );
            }

            return;
        }

        if (warehouseItemId != null
                || site != null
                || position != null) {
            throw new IllegalArgumentException(
                    "Not found entries cannot contain a location snapshot"
            );
        }
    }

    private String requireText(
            String value,
            String message
    ) {
        Objects.requireNonNull(value, message);

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}
