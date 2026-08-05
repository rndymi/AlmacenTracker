package com.rndymi.almacentracker.domain.history;

import java.util.Locale;
import java.util.Objects;
import java.util.Collections;
import java.util.List;

public final class WithdrawalHistoryDraftEntry {

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

    public WithdrawalHistoryDraftEntry(
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

    public WithdrawalHistoryDraftEntry(
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
        if (orderIndex < 0) {
            throw new IllegalArgumentException(
                    "orderIndex must not be negative"
            );
        }

        this.orderIndex = orderIndex;
        this.category = normalizeRequired(
                category,
                "category"
        );
        this.code = normalizeRequired(
                code,
                "code"
        );

        if (quantity != null && quantity <= 0) {
            throw new IllegalArgumentException(
                    "quantity must be positive"
            );
        }

        this.quantity = quantity;
        this.unit = normalizeOptionalUnit(unit);
        this.locationStatus =
                Objects.requireNonNull(
                        locationStatus,
                        "locationStatus"
                );
        this.destinations =
                WithdrawalDestinationCodec
                        .immutableCopy(destinations);

        if (locationStatus
                == WithdrawalLocationStatus.FOUND) {
            if (warehouseItemIdSnapshot == null
                    || warehouseItemIdSnapshot <= 0L) {
                throw new IllegalArgumentException(
                        "FOUND requires a warehouse item id"
                );
            }

            if (siteSnapshot == null
                    || siteSnapshot.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "FOUND requires a site snapshot"
                );
            }

            this.warehouseItemIdSnapshot =
                    warehouseItemIdSnapshot;
            this.siteSnapshot =
                    siteSnapshot.trim();
            this.positionSnapshot =
                    normalizeOptional(
                            positionSnapshot
                    );
        } else {
            this.warehouseItemIdSnapshot = null;
            this.siteSnapshot = null;
            this.positionSnapshot = null;
        }
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

    private static String normalizeRequired(
            String value,
            String fieldName
    ) {
        if (value == null
                || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be empty"
            );
        }

        return value
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private static String normalizeOptionalUnit(
            String value
    ) {
        String normalized =
                normalizeOptional(value);

        return normalized == null
                ? null
                : normalized
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }

    private static String normalizeOptional(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}
