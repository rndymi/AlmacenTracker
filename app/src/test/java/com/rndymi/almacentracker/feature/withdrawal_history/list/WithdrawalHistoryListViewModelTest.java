package com.rndymi.almacentracker.feature.withdrawal_history.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WithdrawalHistoryRepository;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySummary;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public final class WithdrawalHistoryListViewModelTest {

    @Rule
    public final InstantTaskExecutorRule executorRule =
            new InstantTaskExecutorRule();

    private FakeWithdrawalHistoryRepository repository;
    private WithdrawalHistoryListViewModel viewModel;

    @Before
    public void setUp() {
        repository =
                new FakeWithdrawalHistoryRepository();

        viewModel =
                new WithdrawalHistoryListViewModel(
                        repository
                );
    }

    @Test
    public void load_emptyResult_publishesEmpty() {
        viewModel.load();

        repository.completeWith(
                Collections.emptyList()
        );

        assertEquals(
                WithdrawalHistoryListUiState.Status.EMPTY,
                viewModel.getUiState()
                        .getValue()
                        .getStatus()
        );
    }

    @Test
    public void load_withSummaries_publishesContent() {
        WithdrawalHistorySummary summary =
                createSummary(1L);

        viewModel.load();

        repository.completeWith(
                Collections.singletonList(summary)
        );

        WithdrawalHistoryListUiState state =
                viewModel.getUiState().getValue();

        assertEquals(
                WithdrawalHistoryListUiState.Status.CONTENT,
                state.getStatus()
        );

        assertEquals(
                Collections.singletonList(summary),
                state.getSummaries()
        );
    }

    @Test
    public void load_whileLoading_doesNotStartSecondQuery() {
        viewModel.load();
        viewModel.load();
        viewModel.refresh();

        assertEquals(
                1,
                repository.findAllInvocationCount
        );
    }

    @Test
    public void retry_afterError_startsNewQuery() {
        viewModel.load();
        repository.fail();

        viewModel.retry();

        assertEquals(
                2,
                repository.findAllInvocationCount
        );
    }

    @Test
    public void refreshError_preservesPreviousContent() {
        WithdrawalHistorySummary summary =
                createSummary(1L);

        viewModel.load();

        repository.completeWith(
                Collections.singletonList(summary)
        );

        viewModel.refresh();
        repository.fail();

        WithdrawalHistoryListUiState state =
                viewModel.getUiState().getValue();

        assertEquals(
                WithdrawalHistoryListUiState.Status.ERROR,
                state.getStatus()
        );

        assertTrue(state.hasContent());
        assertEquals(
                summary,
                state.getSummaries().get(0)
        );
    }

    private static WithdrawalHistorySummary
    createSummary(long id) {
        return new WithdrawalHistorySummary(
                id,
                "Lista",
                100L,
                100L,
                100L,
                2,
                1,
                1
        );
    }

    private static final class
    FakeWithdrawalHistoryRepository
            implements WithdrawalHistoryRepository {

        private RepositoryCallback<
                List<WithdrawalHistorySummary>
                > pendingCallback;

        private int findAllInvocationCount;

        @Override
        public void insert(
                WithdrawalHistoryRecord record,
                RepositoryCallback<Long> callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void findById(
                long historyId,
                RepositoryCallback<
                        WithdrawalHistoryRecord
                        > callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void findAllSummaries(
                RepositoryCallback<
                        List<WithdrawalHistorySummary>
                        > callback
        ) {
            findAllInvocationCount++;
            pendingCallback = callback;
        }

        @Override
        public void deleteById(
                long historyId,
                RepositoryCallback<Void> callback
        ) {
            throw new UnsupportedOperationException();
        }

        void completeWith(
                List<WithdrawalHistorySummary> values
        ) {
            RepositoryCallback<
                    List<WithdrawalHistorySummary>
                    > callback =
                    pendingCallback;

            pendingCallback = null;
            callback.onSuccess(values);
        }

        void fail() {
            RepositoryCallback<
                    List<WithdrawalHistorySummary>
                    > callback =
                    pendingCallback;

            pendingCallback = null;
            callback.onError(
                    new RuntimeException("Failure")
            );
        }
    }
}
