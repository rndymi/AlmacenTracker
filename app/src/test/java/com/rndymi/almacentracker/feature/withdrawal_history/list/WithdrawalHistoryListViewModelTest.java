package com.rndymi.almacentracker.feature.withdrawal_history.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WithdrawalHistoryRepository;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySearchCriteria;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySummary;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WithdrawalHistoryListViewModelTest {

    @Rule
    public final InstantTaskExecutorRule executorRule =
            new InstantTaskExecutorRule();

    private FakeWithdrawalHistoryRepository repository;
    private WithdrawalHistoryListViewModel viewModel;
    private static final ZoneId TEST_ZONE =
            ZoneId.of("Europe/Madrid");

    @Before
    public void setUp() {
        repository =
                new FakeWithdrawalHistoryRepository();

        viewModel =
                new WithdrawalHistoryListViewModel(
                        repository,
                        TEST_ZONE
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

    @Test
    public void initialLoadUsesEmptyCriteria() {
        viewModel.load();

        assertEquals(
                WithdrawalHistorySearchCriteria.empty(),
                repository.lastSearchCriteria
        );
    }

    @Test
    public void updateQueryDoesNotSearchUntilSearchAction() {
        viewModel.updateQuery("MR");

        assertEquals(0, repository.searchCalls);

        viewModel.search();

        assertEquals(1, repository.searchCalls);
        assertEquals(
                "MR",
                repository.lastSearchCriteria.getQuery()
        );
    }

    @Test
    public void emptyFilteredResultProducesNoResults() {
        viewModel.updateQuery("NO-EXISTE");
        viewModel.search();

        repository.completeSearch(
                Collections.emptyList()
        );

        WithdrawalHistoryListUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        assertTrue(state.hasNoResults());
        assertFalse(state.isEmpty());
    }

    @Test
    public void emptyUnfilteredResultProducesEmpty() {
        viewModel.load();

        repository.completeSearch(
                Collections.emptyList()
        );

        WithdrawalHistoryListUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        assertTrue(state.isEmpty());
        assertFalse(state.hasNoResults());
    }

    @Test
    public void invalidDateIntervalDoesNotQueryRepository() {
        viewModel.updateFromDate(
                LocalDate.of(2026, 8, 3)
        );

        int callsAfterFromDate =
                repository.searchCalls;

        viewModel.updateToDate(
                LocalDate.of(2026, 8, 2)
        );

        assertEquals(
                callsAfterFromDate,
                repository.searchCalls
        );

        WithdrawalHistoryListUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        assertTrue(
                state.hasInvalidDateInterval()
        );
    }

    @Test
    public void sameVisibleDayUsesExclusiveNextDay() {
        LocalDate date =
                LocalDate.of(2026, 8, 2);

        viewModel.updateFromDate(date);
        viewModel.updateToDate(date);

        WithdrawalHistorySearchCriteria criteria =
                repository.lastSearchCriteria;

        assertEquals(
                date.atStartOfDay(TEST_ZONE)
                        .toInstant()
                        .toEpochMilli(),
                criteria
                        .getRegisteredFromInclusive()
                        .longValue()
        );

        assertEquals(
                date.plusDays(1L)
                        .atStartOfDay(TEST_ZONE)
                        .toInstant()
                        .toEpochMilli(),
                criteria
                        .getRegisteredToExclusive()
                        .longValue()
        );
    }

    @Test
    public void staleResultIsIgnored() {
        viewModel.updateQuery("MR");
        viewModel.search();

        RepositoryCallback<
                List<WithdrawalHistorySummary>
                > firstCallback =
                repository.callbacks.get(0);

        viewModel.updateQuery("MI");
        viewModel.search();

        RepositoryCallback<
                List<WithdrawalHistorySummary>
                > secondCallback =
                repository.callbacks.get(1);

        secondCallback.onSuccess(
                Collections.singletonList(
                        createSummary(2L)
                )
        );

        firstCallback.onSuccess(
                Collections.singletonList(
                        createSummary(1L)
                )
        );

        WithdrawalHistoryListUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        assertEquals(
                2L,
                state.getSummaries().get(0).getId()
        );
    }

    @Test
    public void refreshKeepsCurrentCriteria() {
        viewModel.updateQuery("MR");
        viewModel.search();

        repository.completeLatestSearch(
                Collections.emptyList()
        );

        viewModel.refresh();

        assertEquals(
                "MR",
                repository.lastSearchCriteria.getQuery()
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
        private int searchCalls;
        private WithdrawalHistorySearchCriteria lastSearchCriteria;

        private final List<RepositoryCallback<
                List<WithdrawalHistorySummary>
                >> callbacks = new ArrayList<>();

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
        public void searchSummaries(
                WithdrawalHistorySearchCriteria criteria,
                RepositoryCallback<
                        List<WithdrawalHistorySummary>
                        > callback
        ) {
            findAllInvocationCount++;
            searchCalls++;
            lastSearchCriteria = criteria;
            pendingCallback = callback;
            callbacks.add(callback);
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

        void completeSearch(
                List<WithdrawalHistorySummary> values
        ) {
            completeWith(values);
        }

        void completeLatestSearch(
                List<WithdrawalHistorySummary> values
        ) {
            RepositoryCallback<
                    List<WithdrawalHistorySummary>
                    > callback = callbacks.get(
                    callbacks.size() - 1
            );

            if (callback == pendingCallback) {
                pendingCallback = null;
            }

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
