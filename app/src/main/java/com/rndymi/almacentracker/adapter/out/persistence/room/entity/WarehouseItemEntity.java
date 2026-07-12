package com.rndymi.almacentracker.adapter.out.persistence.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "warehouse_items",
        indices = {
                @Index(
                        value = {"category", "code"},
                        unique = true
                 )
        }
)
public class WarehouseItemEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "code")
    private String code;

    @ColumnInfo(name = "site")
    private String site;

    @ColumnInfo(name = "position")
    private String position;

    @ColumnInfo(name = "observations")
    private String observations;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public WarehouseItemEntity(
            long id,
            String category,
            String code,
            String site,
            String position,
            String observations,
            long createdAt,
            long updatedAt
    ) {
        this.id = id;
        this.category = category;
        this.code = code;
        this.site = site;
        this.position = position;
        this.observations = observations;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getSite() {
        return site;
    }

    public String getPosition() {
        return position;
    }

    public String getObservations() {
        return observations;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }
}
