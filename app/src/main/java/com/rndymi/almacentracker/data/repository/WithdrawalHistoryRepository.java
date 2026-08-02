package com.rndymi.almacentracker.data.repository;

import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySummary;

import java.util.List;

public interface WithdrawalHistoryRepository {

    void insert(
            WithdrawalHistoryRecord record,
            RepositoryCallback<Long> callback
    );

    void findById(
            long historyId,
            RepositoryCallback<WithdrawalHistoryRecord> callback
    );

    void findAllSummaries(
            RepositoryCallback<
                    List<WithdrawalHistorySummary>
                    > callback
    );

    void deleteById(
            long historyId,
            RepositoryCallback<Void> callback
    );
}