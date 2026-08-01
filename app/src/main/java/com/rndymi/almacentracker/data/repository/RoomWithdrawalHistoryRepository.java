package com.rndymi.almacentracker.data.repository;

import com.rndymi.almacentracker.data.local.room.dao.WithdrawalHistoryDao;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntity;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntryEntity;
import com.rndymi.almacentracker.data.local.room.mapper.WithdrawalHistoryRoomMapper;
import com.rndymi.almacentracker.data.local.room.relation.WithdrawalHistoryWithEntries;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

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