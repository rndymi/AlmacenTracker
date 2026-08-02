package com.rndymi.almacentracker.feature.withdrawal_history.common;

import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;

import java.util.Objects;

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
}
