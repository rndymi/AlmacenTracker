package com.rndymi.almacentracker.feature.withdrawal_history.detail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WithdrawalHistoryRepository;
import com.rndymi.almacentracker.domain.history.WithdrawalHistory;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryEntry;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySearchCriteria;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySummary;
import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;

import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class WithdrawalHistoryDetailViewModelTest {

    @Rule
    public final InstantTaskExecutorRule
            instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    @Test
    public void loadValidIdPublishesContent() {
        WithdrawalHistoryRecord record =
                createRecord();

        FakeRepository repository =
                new FakeRepository();

        repository.record = record;

        WithdrawalHistoryDetailViewModel viewModel =
                new WithdrawalHistoryDetailViewModel(
                        repository
                );

        AtomicReference<
                WithdrawalHistoryDetailUiState
                > observed =
                observe(viewModel.getUiState());

        viewModel.load(7L);

        WithdrawalHistoryDetailUiState state =
                observed.get();

        assertEquals(
                WithdrawalHistoryDetailUiState.Status.CONTENT,
                state.getStatus()
        );
        assertSame(
                record,
                state.getRecord()
        );
        assertEquals(
                1,
                repository.findByIdCalls
        );
        assertEquals(
                7L,
                repository.lastHistoryId
        );
    }

    @Test
    public void loadInvalidIdPublishesNotFoundWithoutRepositoryCall() {
        FakeRepository repository =
                new FakeRepository();

        WithdrawalHistoryDetailViewModel viewModel =
                new WithdrawalHistoryDetailViewModel(
                        repository
                );

        AtomicReference<
                WithdrawalHistoryDetailUiState
                > observed =
                observe(viewModel.getUiState());

        viewModel.load(0L);

        assertTrue(
                observed.get().isNotFound()
        );
        assertEquals(
                0,
                repository.findByIdCalls
        );
    }

    @Test
    public void repositoryNotFoundPublishesNotFound() {
        FakeRepository repository =
                new FakeRepository();

        repository.notFound = true;

        WithdrawalHistoryDetailViewModel viewModel =
                new WithdrawalHistoryDetailViewModel(
                        repository
                );

        AtomicReference<
                WithdrawalHistoryDetailUiState
                > observed =
                observe(viewModel.getUiState());

        viewModel.load(8L);

        assertTrue(
                observed.get().isNotFound()
        );
    }

    @Test
    public void repositoryErrorPublishesError() {
        FakeRepository repository =
                new FakeRepository();

        repository.error =
                new IllegalStateException(
                        "Database error"
                );

        WithdrawalHistoryDetailViewModel viewModel =
                new WithdrawalHistoryDetailViewModel(
                        repository
                );

        AtomicReference<
                WithdrawalHistoryDetailUiState
                > observed =
                observe(viewModel.getUiState());

        viewModel.load(9L);

        assertTrue(
                observed.get().hasLoadError()
        );
        assertFalse(
                observed.get().hasContent()
        );
    }

    @Test
    public void secondLoadDoesNotDuplicateRequest() {
        FakeRepository repository =
                new FakeRepository();

        repository.record = createRecord();

        WithdrawalHistoryDetailViewModel viewModel =
                new WithdrawalHistoryDetailViewModel(
                        repository
                );

        observe(viewModel.getUiState());

        viewModel.load(7L);
        viewModel.load(7L);

        assertEquals(
                1,
                repository.findByIdCalls
        );
    }

    @Test
    public void retryUsesStoredHistoryId() {
        FakeRepository repository =
                new FakeRepository();

        repository.error =
                new IllegalStateException();

        WithdrawalHistoryDetailViewModel viewModel =
                new WithdrawalHistoryDetailViewModel(
                        repository
                );

        AtomicReference<
                WithdrawalHistoryDetailUiState
                > observed =
                observe(viewModel.getUiState());

        viewModel.load(7L);

        assertTrue(
                observed.get().hasLoadError()
        );

        repository.error = null;
        repository.record = createRecord();

        viewModel.retryLoad();

        assertEquals(
                2,
                repository.findByIdCalls
        );
        assertEquals(
                7L,
                repository.lastHistoryId
        );
        assertTrue(
                observed.get().hasContent()
        );
    }

    @Test
    public void contentPreservesEntryOrder() {
        FakeRepository repository =
                new FakeRepository();

        repository.record = createRecord();

        WithdrawalHistoryDetailViewModel viewModel =
                new WithdrawalHistoryDetailViewModel(
                        repository
                );

        AtomicReference<
                WithdrawalHistoryDetailUiState
                > observed =
                observe(viewModel.getUiState());

        viewModel.load(7L);

        WithdrawalHistoryRecord record =
                observed.get().getRecord();

        assertNotNull(record);
        assertEquals(
                0,
                record.getEntries()
                        .get(0)
                        .getOrderIndex()
        );
    }

    @Test
    public void deleteHistoryPublishesDeletingAndSuccessEvent() {
        FakeRepository repository =
                new FakeRepository();

        repository.record = createRecord();

        WithdrawalHistoryDetailViewModel viewModel =
                new WithdrawalHistoryDetailViewModel(
                        repository
                );

        AtomicReference<
                WithdrawalHistoryDetailUiState
                > observedState =
                observe(viewModel.getUiState());

        AtomicReference<UiEvent<Long>>
                observedEvent =
                new AtomicReference<>();

        viewModel.getDeleteSuccessEvent()
                .observeForever(
                        observedEvent::set
                );

        viewModel.load(7L);
        viewModel.deleteHistory();

        assertEquals(
                1,
                repository.deleteByIdCalls
        );

        assertEquals(
                7L,
                repository.lastDeletedHistoryId
        );

        assertNotNull(
                observedEvent.get()
        );

        assertEquals(
                Long.valueOf(7L),
                observedEvent.get()
                        .getContentIfNotHandled()
        );

        assertNotNull(
                observedState.get()
                        .getRecord()
        );
    }

    @Test
    public void deleteHistoryNotFoundPublishesNotFound() {
        FakeRepository repository =
                new FakeRepository();

        repository.record = createRecord();

        WithdrawalHistoryDetailViewModel viewModel =
                new WithdrawalHistoryDetailViewModel(
                        repository
                );

        AtomicReference<
                WithdrawalHistoryDetailUiState
                > observed =
                observe(viewModel.getUiState());

        viewModel.load(7L);

        repository.deleteNotFound = true;

        viewModel.deleteHistory();

        assertTrue(
                observed.get().isNotFound()
        );
    }

    @Test
    public void deleteErrorPreservesContentAndAllowsRetry() {
        FakeRepository repository =
                new FakeRepository();

        repository.record = createRecord();
        repository.deleteError =
                new IllegalStateException(
                        "Delete failed"
                );

        WithdrawalHistoryDetailViewModel viewModel =
                new WithdrawalHistoryDetailViewModel(
                        repository
                );

        AtomicReference<
                WithdrawalHistoryDetailUiState
                > observed =
                observe(viewModel.getUiState());

        viewModel.load(7L);
        viewModel.deleteHistory();

        assertTrue(
                observed.get().hasDeleteError()
        );

        assertNotNull(
                observed.get().getRecord()
        );

        repository.deleteError = null;

        viewModel.retryDelete();

        assertEquals(
                2,
                repository.deleteByIdCalls
        );
    }

    @Test
    public void deleteHistoryIgnoresRequestWithoutLoadedRecord() {
        FakeRepository repository =
                new FakeRepository();

        WithdrawalHistoryDetailViewModel viewModel =
                new WithdrawalHistoryDetailViewModel(
                        repository
                );

        viewModel.deleteHistory();

        assertEquals(
                0,
                repository.deleteByIdCalls
        );
    }

    private AtomicReference<
            WithdrawalHistoryDetailUiState
            > observe(
            LiveData<WithdrawalHistoryDetailUiState>
                    liveData
    ) {
        AtomicReference<
                WithdrawalHistoryDetailUiState
                > observed =
                new AtomicReference<>();

        liveData.observeForever(
                observed::set
        );

        return observed;
    }

    private WithdrawalHistoryRecord createRecord() {
        WithdrawalHistory history =
                new WithdrawalHistory(
                        7L,
                        "Reposición",
                        1_700_000_000_000L,
                        1_700_000_000_100L,
                        1_700_000_000_100L
                );

        WithdrawalHistoryEntry entry =
                new WithdrawalHistoryEntry(
                        11L,
                        7L,
                        0,
                        "MR",
                        "001210A",
                        4,
                        "CAJAS",
                        25L,
                        "A1",
                        "2",
                        WithdrawalLocationStatus.FOUND
                );

        return new WithdrawalHistoryRecord(
                history,
                Collections.singletonList(entry)
        );
    }

    private static final class FakeRepository
            implements WithdrawalHistoryRepository {

        private WithdrawalHistoryRecord record;
        private Throwable error;
        private boolean notFound;
        private int findByIdCalls;
        private long lastHistoryId;
        private Throwable deleteError;
        private boolean deleteNotFound;
        private int deleteByIdCalls;
        private long lastDeletedHistoryId;

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
            findByIdCalls++;
            lastHistoryId = historyId;

            if (error != null) {
                callback.onError(error);
                return;
            }

            if (notFound) {
                callback.onNotFound();
                return;
            }

            callback.onSuccess(record);
        }

        @Override
        public void findAllSummaries(
                RepositoryCallback<
                        List<WithdrawalHistorySummary>
                        > callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void searchSummaries(
                WithdrawalHistorySearchCriteria criteria,
                RepositoryCallback<
                        List<WithdrawalHistorySummary>
                        > callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteById(
                long historyId,
                RepositoryCallback<Void> callback
        ) {
            deleteByIdCalls++;
            lastDeletedHistoryId = historyId;

            if (deleteError != null) {
                callback.onError(deleteError);
                return;
            }

            if (deleteNotFound) {
                callback.onNotFound();
                return;
            }

            callback.onSuccess(null);
        }
    }
}
