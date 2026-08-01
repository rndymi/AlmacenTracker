package com.rndymi.almacentracker.data.local.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntity;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntryEntity;
import com.rndymi.almacentracker.data.local.room.relation.WithdrawalHistoryWithEntries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Dao
public interface WithdrawalHistoryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertHistory(
            WithdrawalHistoryEntity entity
    );

    @Insert(onConflict = OnConflictStrategy.ABORT)
    List<Long> insertEntries(
            List<WithdrawalHistoryEntryEntity> entities
    );

    @Transaction
    default long insertHistoryWithEntries(
            WithdrawalHistoryEntity history,
            List<WithdrawalHistoryEntryEntity> entries
    ) {
        long historyId = insertHistory(history);

        if (entries == null || entries.isEmpty()) {
            return historyId;
        }

        List<WithdrawalHistoryEntryEntity> linkedEntries =
                new ArrayList<>(entries.size());

        for (WithdrawalHistoryEntryEntity entry : entries) {
            linkedEntries.add(
                    entry.withHistoryId(historyId)
            );
        }

        insertEntries(linkedEntries);

        return historyId;
    }

    @Transaction
    @Query(
            "SELECT * FROM withdrawal_history " +
                    "WHERE id = :historyId " +
                    "LIMIT 1"
    )
    WithdrawalHistoryWithEntries findByIdWithEntries(
            long historyId
    );

    @Query(
            "SELECT * FROM withdrawal_history_entries " +
                    "WHERE history_id = :historyId " +
                    "ORDER BY order_index ASC"
    )
    List<WithdrawalHistoryEntryEntity>
    findEntriesByHistoryId(
            long historyId
    );

    @Query(
            "DELETE FROM withdrawal_history " +
                    "WHERE id = :historyId"
    )
    int deleteById(long historyId);

    @Query(
            "SELECT COUNT(*) FROM withdrawal_history"
    )
    int countHistories();

    @Query(
            "SELECT COUNT(*) " +
                    "FROM withdrawal_history_entries " +
                    "WHERE history_id = :historyId"
    )
    int countEntriesByHistoryId(long historyId);

    @Query(
            "SELECT COUNT(*) " +
                    "FROM withdrawal_history_entries"
    )
    int countAllEntries();

    default List<WithdrawalHistoryEntryEntity>
    findOrderedEntriesByHistoryId(
            long historyId
    ) {
        List<WithdrawalHistoryEntryEntity> entries =
                findEntriesByHistoryId(historyId);

        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }

        return entries;
    }
}