package com.rndymi.almacentracker.data.repository;

import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;

public interface WithdrawalHistoryRepository {

    void insert(
            WithdrawalHistoryRecord record,
            RepositoryCallback<Long> callback
    );

    void findById(
            long historyId,
            RepositoryCallback<WithdrawalHistoryRecord> callback
    );

    void deleteById(
            long historyId,
            RepositoryCallback<Void> callback
    );
}