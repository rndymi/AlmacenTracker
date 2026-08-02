package com.rndymi.almacentracker.data.local.room.projection;

import androidx.room.ColumnInfo;

public final class WithdrawalHistorySummaryRow {

    private final long id;
    private final String title;

    @ColumnInfo(name = "registered_at")
    private final long registeredAt;

    @ColumnInfo(name = "created_at")
    private final long createdAt;

    @ColumnInfo(name = "updated_at")
    private final long updatedAt;

    @ColumnInfo(name = "entry_count")
    private final int entryCount;

    @ColumnInfo(name = "found_count")
    private final int foundCount;

    @ColumnInfo(name = "not_found_count")
    private final int notFoundCount;

    public WithdrawalHistorySummaryRow(
            long id,
            String title,
            long registeredAt,
            long createdAt,
            long updatedAt,
            int entryCount,
            int foundCount,
            int notFoundCount
    ) {
        this.id = id;
        this.title = title;
        this.registeredAt = registeredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.entryCount = entryCount;
        this.foundCount = foundCount;
        this.notFoundCount = notFoundCount;
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

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public int getFoundCount() {
        return foundCount;
    }

    public int getNotFoundCount() {
        return notFoundCount;
    }
}
