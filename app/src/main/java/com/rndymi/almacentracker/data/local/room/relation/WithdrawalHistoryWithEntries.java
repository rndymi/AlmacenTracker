package com.rndymi.almacentracker.data.local.room.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntity;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntryEntity;

import java.util.List;

public final class WithdrawalHistoryWithEntries {

    @Embedded
    public WithdrawalHistoryEntity history;

    @Relation(
            parentColumn = "id",
            entityColumn = "history_id",
            entity = WithdrawalHistoryEntryEntity.class
    )
    public List<WithdrawalHistoryEntryEntity> entries;
}