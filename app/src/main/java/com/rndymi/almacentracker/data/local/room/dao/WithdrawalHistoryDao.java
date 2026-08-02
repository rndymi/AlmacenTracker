package com.rndymi.almacentracker.data.local.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntity;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntryEntity;
import com.rndymi.almacentracker.data.local.room.projection.WithdrawalHistorySummaryRow;
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
            "SELECT " +
                    "history.id AS id, " +
                    "history.title AS title, " +
                    "history.registered_at AS registered_at, " +
                    "history.created_at AS created_at, " +
                    "history.updated_at AS updated_at, " +
                    "COUNT(entry.id) AS entry_count, " +
                    "COALESCE(SUM(CASE " +
                    "WHEN entry.location_status = 'FOUND' " +
                    "THEN 1 ELSE 0 END), 0) AS found_count, " +
                    "COALESCE(SUM(CASE " +
                    "WHEN entry.location_status = 'NOT_FOUND' " +
                    "THEN 1 ELSE 0 END), 0) AS not_found_count " +
                    "FROM withdrawal_history AS history " +
                    "LEFT JOIN withdrawal_history_entries AS entry " +
                    "ON entry.history_id = history.id " +
                    "GROUP BY " +
                    "history.id, " +
                    "history.title, " +
                    "history.registered_at, " +
                    "history.created_at, " +
                    "history.updated_at " +
                    "ORDER BY " +
                    "history.registered_at DESC, " +
                    "history.id DESC"
    )
    List<WithdrawalHistorySummaryRow> findAllSummaries();

    @Query(
            "SELECT " +
                    "history.id AS id, " +
                    "history.title AS title, " +
                    "history.registered_at AS registered_at, " +
                    "history.created_at AS created_at, " +
                    "history.updated_at AS updated_at, " +
                    "COUNT(entry.id) AS entry_count, " +
                    "COALESCE(SUM(CASE " +
                    "WHEN entry.location_status = 'FOUND' " +
                    "THEN 1 ELSE 0 END), 0) AS found_count, " +
                    "COALESCE(SUM(CASE " +
                    "WHEN entry.location_status = 'NOT_FOUND' " +
                    "THEN 1 ELSE 0 END), 0) AS not_found_count " +
                    "FROM withdrawal_history AS history " +
                    "LEFT JOIN withdrawal_history_entries AS entry " +
                    "ON entry.history_id = history.id " +
                    "WHERE (" +
                    ":hasQuery = 0 " +
                    "OR history.title LIKE :queryPattern " +
                    "ESCAPE '\\' COLLATE NOCASE " +
                    "OR EXISTS (" +
                    "SELECT 1 " +
                    "FROM withdrawal_history_entries " +
                    "AS matching_entry " +
                    "WHERE matching_entry.history_id = history.id " +
                    "AND (" +
                    "matching_entry.category LIKE :queryPattern " +
                    "ESCAPE '\\' COLLATE NOCASE " +
                    "OR matching_entry.code LIKE :queryPattern " +
                    "ESCAPE '\\' COLLATE NOCASE" +
                    ")" +
                    ")" +
                    ") " +
                    "AND (" +
                    ":registeredFromInclusive IS NULL " +
                    "OR history.registered_at " +
                    ">= :registeredFromInclusive" +
                    ") " +
                    "AND (" +
                    ":registeredToExclusive IS NULL " +
                    "OR history.registered_at " +
                    "< :registeredToExclusive" +
                    ") " +
                    "GROUP BY " +
                    "history.id, " +
                    "history.title, " +
                    "history.registered_at, " +
                    "history.created_at, " +
                    "history.updated_at " +
                    "ORDER BY " +
                    "history.registered_at DESC, " +
                    "history.id DESC"
    )
    List<WithdrawalHistorySummaryRow> searchSummaries(
            int hasQuery,
            String queryPattern,
            Long registeredFromInclusive,
            Long registeredToExclusive
    );

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