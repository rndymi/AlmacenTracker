package com.rndymi.almacentracker.feature.withdrawal_history.common;

import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;

import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WithdrawalHistoryCreateInput {

    private final int orderIndex;
    private final String category;
    private final String code;
    private final Integer quantityProposal;
    private final String unitProposal;
    private final Long warehouseItemIdSnapshot;
    private final String siteSnapshot;
    private final String positionSnapshot;
    private final WithdrawalLocationStatus locationStatus;
    private final List<String> destinations;

    public WithdrawalHistoryCreateInput(
            int orderIndex,
            String category,
            String code,
            Integer quantityProposal,
            String unitProposal,
            Long warehouseItemIdSnapshot,
            String siteSnapshot,
            String positionSnapshot,
            WithdrawalLocationStatus locationStatus
    ) {
        this(
                orderIndex,
                category,
                code,
                quantityProposal,
                unitProposal,
                warehouseItemIdSnapshot,
                siteSnapshot,
                positionSnapshot,
                locationStatus,
                Collections.emptyList()
        );
    }

    public WithdrawalHistoryCreateInput(
            int orderIndex,
            String category,
            String code,
            Integer quantityProposal,
            String unitProposal,
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
        this.category = Objects.requireNonNull(
                category,
                "category"
        );
        this.code = Objects.requireNonNull(
                code,
                "code"
        );
        this.quantityProposal = quantityProposal;
        this.unitProposal = unitProposal;
        this.warehouseItemIdSnapshot =
                warehouseItemIdSnapshot;
        this.siteSnapshot = siteSnapshot;
        this.positionSnapshot = positionSnapshot;
        this.locationStatus =
                Objects.requireNonNull(
                        locationStatus,
                        "locationStatus"
                );
        this.destinations = immutableDestinations(
                destinations
        );
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

    public Integer getQuantityProposal() {
        return quantityProposal;
    }

    public String getUnitProposal() {
        return unitProposal;
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

    private static List<String> immutableDestinations(
            List<String> values
    ) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();

        for (String value : values) {
            if (value != null
                    && !value.trim().isEmpty()
                    && !result.contains(value.trim())) {
                result.add(value.trim());
            }
        }

        return result.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(result);
    }
}
