package com.rndymi.almacentracker.feature.withdrawal_history.create;

import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraftValidator;
import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;
import com.rndymi.almacentracker.domain.history.WithdrawalDestinationCodec;

import java.util.Collections;
import java.util.List;

public final class WithdrawalHistoryDraftEntryUiModel
        implements WithdrawalHistoryDraftValidator.EditableEntry {

    private final long stableId;
    private final int orderIndex;
    private final String category;
    private final String code;
    private final String quantityText;
    private final String unitText;
    private final Long warehouseItemIdSnapshot;
    private final String siteSnapshot;
    private final String positionSnapshot;
    private final WithdrawalLocationStatus locationStatus;
    private final String quantityError;
    private final String unitError;
    private final List<String> destinations;

    public WithdrawalHistoryDraftEntryUiModel(
            long stableId,
            int orderIndex,
            String category,
            String code,
            String quantityText,
            String unitText,
            Long warehouseItemIdSnapshot,
            String siteSnapshot,
            String positionSnapshot,
            WithdrawalLocationStatus locationStatus,
            String quantityError,
            String unitError
    ) {
        this(
                stableId,
                orderIndex,
                category,
                code,
                quantityText,
                unitText,
                warehouseItemIdSnapshot,
                siteSnapshot,
                positionSnapshot,
                locationStatus,
                quantityError,
                unitError,
                Collections.emptyList()
        );
    }

    public WithdrawalHistoryDraftEntryUiModel(
            long stableId,
            int orderIndex,
            String category,
            String code,
            String quantityText,
            String unitText,
            Long warehouseItemIdSnapshot,
            String siteSnapshot,
            String positionSnapshot,
            WithdrawalLocationStatus locationStatus,
            String quantityError,
            String unitError,
            List<String> destinations
    ) {
        this.stableId = stableId;
        this.orderIndex = orderIndex;
        this.category = category;
        this.code = code;
        this.quantityText = quantityText;
        this.unitText = unitText;
        this.warehouseItemIdSnapshot =
                warehouseItemIdSnapshot;
        this.siteSnapshot = siteSnapshot;
        this.positionSnapshot = positionSnapshot;
        this.locationStatus = locationStatus;
        this.quantityError = quantityError;
        this.unitError = unitError;
        this.destinations =
                WithdrawalDestinationCodec
                        .immutableCopy(destinations);
    }

    @Override
    public long getStableId() {
        return stableId;
    }

    @Override
    public String getQuantityText() {
        return quantityText;
    }

    @Override
    public String getUnitText() {
        return unitText;
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

    public String getQuantityError() {
        return quantityError;
    }

    public String getUnitError() {
        return unitError;
    }

    public List<String> getDestinations() {
        return destinations;
    }

    public WithdrawalHistoryDraftEntryUiModel
    withQuantity(
            String value
    ) {
        return copy(
                value,
                unitText,
                null,
                unitError
        );
    }

    public WithdrawalHistoryDraftEntryUiModel
    withUnit(
            String value
    ) {
        return copy(
                quantityText,
                value,
                quantityError,
                null
        );
    }

    public WithdrawalHistoryDraftEntryUiModel
    withErrors(
            String newQuantityError,
            String newUnitError
    ) {
        return copy(
                quantityText,
                unitText,
                newQuantityError,
                newUnitError
        );
    }

    private WithdrawalHistoryDraftEntryUiModel copy(
            String newQuantity,
            String newUnit,
            String newQuantityError,
            String newUnitError
    ) {
        return new WithdrawalHistoryDraftEntryUiModel(
                stableId,
                orderIndex,
                category,
                code,
                newQuantity,
                newUnit,
                warehouseItemIdSnapshot,
                siteSnapshot,
                positionSnapshot,
                locationStatus,
                newQuantityError,
                newUnitError,
                destinations
        );
    }
}
