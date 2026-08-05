package com.rndymi.almacentracker.data.local.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "withdrawal_history")
public class WithdrawalHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    private final long id;

    @ColumnInfo(name = "title")
    private final String title;

    @ColumnInfo(name = "destination")
    private final String destination;

    @ColumnInfo(name = "registered_at")
    private final long registeredAt;

    @ColumnInfo(name = "created_at")
    private final long createdAt;

    @ColumnInfo(name = "updated_at")
    private final long updatedAt;

    @Ignore
    public WithdrawalHistoryEntity(
            long id,
            String title,
            long registeredAt,
            long createdAt,
            long updatedAt
    ) {
        this(id, title, null, registeredAt, createdAt, updatedAt);
    }

    public WithdrawalHistoryEntity(
            long id,
            String title,
            String destination,
            long registeredAt,
            long createdAt,
            long updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.destination = destination;
        this.registeredAt = registeredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getRegisteredAt() {
        return registeredAt;
    }

    public String getDestination() {
        return destination;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }
}
