package com.rndymi.almacentracker.data.repository;

import com.rndymi.almacentracker.data.local.room.dao.WithdrawalHistoryDao;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntity;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntryEntity;
import com.rndymi.almacentracker.data.local.room.mapper.WithdrawalHistoryRoomMapper;
import com.rndymi.almacentracker.data.local.room.projection.WithdrawalHistorySummaryRow;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySearchCriteria;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySummary;

import com.rndymi.almacentracker.data.local.room.relation.WithdrawalHistoryWithEntries;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.List;
import java.util.Objects;

public final class RoomWithdrawalHistoryRepository
        implements WithdrawalHistoryRepository {

    private final WithdrawalHistoryDao dao;
    private final WithdrawalHistoryRoomMapper mapper;
    private final Executor executor;

    public RoomWithdrawalHistoryRepository(
            WithdrawalHistoryDao dao,
            WithdrawalHistoryRoomMapper mapper,
            Executor executor
    ) {
        this.dao = Objects.requireNonNull(dao);
        this.mapper = Objects.requireNonNull(mapper);
        this.executor = Objects.requireNonNull(executor);
    }

    @Override
    public void insert(
            WithdrawalHistoryRecord record,
            RepositoryCallback<Long> callback
    ) {
        Objects.requireNonNull(record);
        Objects.requireNonNull(callback);

        WithdrawalHistoryRecord recordCopy =
                new WithdrawalHistoryRecord(
                        record.getHistory(),
                        new ArrayList<>(record.getEntries())
                );

        executor.execute(() -> {
            try {
                WithdrawalHistoryEntity historyEntity =
                        mapper.toEntity(
                                recordCopy.getHistory()
                        );

                List<WithdrawalHistoryEntryEntity>
                        entryEntities =
                        mapper.toEntryEntities(
                                recordCopy.getEntries()
                        );

                long generatedId =
                        dao.insertHistoryWithEntries(
                                historyEntity,
                                entryEntities
                        );

                callback.onSuccess(generatedId);
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void findById(
            long historyId,
            RepositoryCallback<WithdrawalHistoryRecord> callback
    ) {
        Objects.requireNonNull(callback);

        executor.execute(() -> {
            try {
                WithdrawalHistoryWithEntries relation =
                        dao.findByIdWithEntries(historyId);

                if (relation == null
                        || relation.history == null) {
                    callback.onNotFound();
                    return;
                }

                relation.entries =
                        dao.findOrderedEntriesByHistoryId(
                                historyId
                        );

                callback.onSuccess(
                        mapper.toDomain(relation)
                );
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void findAllSummaries(
            RepositoryCallback<
                    List<WithdrawalHistorySummary>
                    > callback
    ) {
        Objects.requireNonNull(callback);

        executor.execute(() -> {
            try {
                List<WithdrawalHistorySummaryRow> rows =
                        dao.findAllSummaries();

                List<WithdrawalHistorySummary> summaries;

                if (rows == null || rows.isEmpty()) {
                    summaries = Collections.emptyList();
                } else {
                    summaries =
                            mapper.toSummaryDomains(rows);
                }

                callback.onSuccess(
                        Collections.unmodifiableList(
                                new ArrayList<>(summaries)
                        )
                );
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void searchSummaries(
            WithdrawalHistorySearchCriteria criteria,
            RepositoryCallback<
                    List<WithdrawalHistorySummary>
                    > callback
    ) {
        Objects.requireNonNull(
                criteria,
                "criteria"
        );

        Objects.requireNonNull(
                callback,
                "callback"
        );

        executor.execute(() -> {
            try {
                List<WithdrawalHistorySummaryRow> rows =
                        dao.searchSummaries(
                                criteria.hasQuery()
                                        ? 1
                                        : 0,
                                criteria.createLikePattern(),
                                criteria.getRegisteredFromInclusive(),
                                criteria.getRegisteredToExclusive()
                        );

                List<WithdrawalHistorySummary> summaries;

                if (rows == null || rows.isEmpty()) {
                    summaries = Collections.emptyList();
                } else {
                    summaries =
                            mapper.toSummaryDomains(rows);
                }

                callback.onSuccess(
                        Collections.unmodifiableList(
                                new ArrayList<>(summaries)
                        )
                );
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void deleteById(
            long historyId,
            RepositoryCallback<Void> callback
    ) {
        Objects.requireNonNull(callback);

        executor.execute(() -> {
            try {
                int deletedRows =
                        dao.deleteById(historyId);

                if (deletedRows == 0) {
                    callback.onNotFound();
                    return;
                }

                callback.onSuccess(null);
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }
}