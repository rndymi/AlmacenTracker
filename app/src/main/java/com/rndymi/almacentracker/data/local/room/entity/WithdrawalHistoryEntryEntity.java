package com.rndymi.almacentracker.data.local.room.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "withdrawal_history_entries",
        foreignKeys = {
                @ForeignKey(
                        entity = WithdrawalHistoryEntity.class,
                        parentColumns = "id",
                        childColumns = "history_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = {"history_id"}),
                @Index(
                        value = {
                                "history_id",
                                "order_index"
                        },
                        unique = true
                )
        }
)
public class WithdrawalHistoryEntryEntity {

    @PrimaryKey(autoGenerate = true)
    private final long id;

    @ColumnInfo(name = "history_id")
    private final long historyId;

    @ColumnInfo(name = "order_index")
    private final int orderIndex;

    @ColumnInfo(name = "category")
    @NonNull
    private final String category;

    @ColumnInfo(name = "code")
    @NonNull
    private final String code;

    @ColumnInfo(name = "quantity")
    private final Integer quantity;

    @ColumnInfo(name = "unit")
    private final String unit;

    @ColumnInfo(name = "warehouse_item_id_snapshot")
    private final Long warehouseItemIdSnapshot;

    @ColumnInfo(name = "site_snapshot")
    private final String siteSnapshot;

    @ColumnInfo(name = "position_snapshot")
    private final String positionSnapshot;

    @ColumnInfo(name = "location_status")
    @NonNull
    private final String locationStatus;

    public WithdrawalHistoryEntryEntity(
            long id,
            long historyId,
            int orderIndex,
            @NonNull String category,
            @NonNull String code,
            Integer quantity,
            String unit,
            Long warehouseItemIdSnapshot,
            String siteSnapshot,
            String positionSnapshot,
            @NonNull String locationStatus
    ) {
        this.id = id;
        this.historyId = historyId;
        this.orderIndex = orderIndex;
        this.category = category;
        this.code = code;
        this.quantity = quantity;
        this.unit = unit;
        this.warehouseItemIdSnapshot =
                warehouseItemIdSnapshot;
        this.siteSnapshot = siteSnapshot;
        this.positionSnapshot = positionSnapshot;
        this.locationStatus = locationStatus;
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

    @NonNull
    public String getCategory() {
        return category;
    }

    @NonNull
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

    @NonNull
    public String getLocationStatus() {
        return locationStatus;
    }

    public WithdrawalHistoryEntryEntity withHistoryId(
            long newHistoryId
    ) {
        return new WithdrawalHistoryEntryEntity(
                id,
                newHistoryId,
                orderIndex,
                category,
                code,
                quantity,
                unit,
                warehouseItemIdSnapshot,
                siteSnapshot,
                positionSnapshot,
                locationStatus
        );
    }
}
