package com.rndymi.almacentracker.feature.inventory.detail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.data.repository.WarehouseItemDetailResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.feature.inventory.common.WarehouseItemDeleteResult;
import com.rndymi.almacentracker.feature.inventory.common.WarehouseItemDeleteService;
import com.rndymi.almacentracker.testutil.LiveDataTestUtil;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;

import org.junit.Rule;
import org.junit.Test;

import java.util.function.Consumer;

public final class WarehouseItemDetailViewModelTest {

    @Rule
    public final InstantTaskExecutorRule executorRule =
            new InstantTaskExecutorRule();

    @Test
    public void constructor_exposesInvalidId_whenIdIsNotValid()
            throws InterruptedException {

        RecordingDetailRepository detailRepository =
                new RecordingDetailRepository();

        RecordingDeleteService deleteService =
                new RecordingDeleteService();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailRepository,
                        deleteService,
                        0L
                );

        WarehouseItemDetailUiState state =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getUiState()
                );

        assertEquals(
                WarehouseItemDetailUiState.Status.INVALID_ID,
                state.getStatus()
        );

        assertEquals(0, detailRepository.calls);
    }

    @Test
    public void result_exposesContent_whenItemExists()
            throws InterruptedException {

        RecordingDetailRepository detailRepository =
                new RecordingDetailRepository();

        RecordingDeleteService deleteService =
                new RecordingDeleteService();

        WarehouseItem warehouseItem =
                createWarehouseItem(3L);

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailRepository,
                        deleteService,
                        3L
                );

        detailRepository.result.setValue(
                WarehouseItemDetailResult.found(
                        warehouseItem
                )
        );

        WarehouseItemDetailUiState state =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getUiState()
                );

        assertEquals(
                WarehouseItemDetailUiState.Status.CONTENT,
                state.getStatus()
        );

        assertSame(
                warehouseItem,
                state.getWarehouseItem()
        );

        assertFalse(state.isDeleting());
    }

    @Test
    public void delete_entersDeletingAndUsesCurrentId()
            throws InterruptedException {

        RecordingDetailRepository detailRepository =
                new RecordingDetailRepository();

        RecordingDeleteService deleteService =
                new RecordingDeleteService();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailRepository,
                        deleteService,
                        7L
                );

        detailRepository.result.setValue(
                WarehouseItemDetailResult.found(
                        createWarehouseItem(7L)
                )
        );

        LiveDataTestUtil.getOrAwaitValue(
                viewModel.getUiState()
        );

        viewModel.deleteWarehouseItem();

        WarehouseItemDetailUiState state =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getUiState()
                );

        assertTrue(state.isDeleting());
        assertEquals(1, deleteService.calls);
        assertEquals(7L, deleteService.deletedId);
    }

    @Test
    public void delete_ignoresSecondCallWhileDeleting()
            throws InterruptedException {
        RecordingDetailRepository detailRepository =
                new RecordingDetailRepository();

        RecordingDeleteService deleteService =
                new RecordingDeleteService();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailRepository,
                        deleteService,
                        7L
                );

        detailRepository.result.setValue(
                WarehouseItemDetailResult.found(
                        createWarehouseItem(7L)
                )
        );

        LiveDataTestUtil.getOrAwaitValue(
                viewModel.getUiState()
        );

        viewModel.deleteWarehouseItem();
        viewModel.deleteWarehouseItem();

        assertEquals(1, deleteService.calls);
    }

    @Test
    public void delete_emitsSuccessOnlyOnce()
            throws InterruptedException {

        RecordingDetailRepository detailRepository =
                new RecordingDetailRepository();

        RecordingDeleteService deleteService =
                new RecordingDeleteService();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailRepository,
                        deleteService,
                        5L
                );

        detailRepository.result.setValue(
                WarehouseItemDetailResult.found(
                        createWarehouseItem(5L)
                )
        );

        LiveDataTestUtil.getOrAwaitValue(
                viewModel.getUiState()
        );

        viewModel.deleteWarehouseItem();

        deleteService.callback.accept(
                WarehouseItemDeleteResult.success(1)
        );

        UiEvent<Boolean> event =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getDeletionSuccess()
                );

        assertEquals(
                Boolean.TRUE,
                event.getContentIfNotHandled()
        );

        assertEquals(
                null,
                event.getContentIfNotHandled()
        );
    }

    @Test
    public void delete_exposesNotFound_whenNoRowWasDeleted()
            throws InterruptedException {

        RecordingDetailRepository detailRepository =
                new RecordingDetailRepository();

        RecordingDeleteService deleteService =
                new RecordingDeleteService();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailRepository,
                        deleteService,
                        8L
                );

        detailRepository.result.setValue(
                WarehouseItemDetailResult.found(
                        createWarehouseItem(8L)
                )
        );

        LiveDataTestUtil.getOrAwaitValue(
                viewModel.getUiState()
        );

        viewModel.deleteWarehouseItem();

        deleteService.callback.accept(
                WarehouseItemDeleteResult.notFound(1)
        );

        WarehouseItemDetailUiState state =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getUiState()
                );

        assertEquals(
                WarehouseItemDetailUiState.Status.NOT_FOUND,
                state.getStatus()
        );
    }

    @Test
    public void delete_exposesErrorAndAllowsRetry()
            throws InterruptedException {

        RecordingDetailRepository detailRepository =
                new RecordingDetailRepository();

        RecordingDeleteService deleteService =
                new RecordingDeleteService();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailRepository,
                        deleteService,
                        10L
                );

        detailRepository.result.setValue(
                WarehouseItemDetailResult.found(
                        createWarehouseItem(10L)
                )
        );

        LiveDataTestUtil.getOrAwaitValue(
                viewModel.getUiState()
        );

        viewModel.deleteWarehouseItem();

        deleteService.callback.accept(
                WarehouseItemDeleteResult.persistenceError(
                        1,
                        new IllegalStateException("Failure")
                )
        );

        WarehouseItemDetailUiState state =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getUiState()
                );

        assertEquals(
                WarehouseItemDetailUiState.Status.CONTENT,
                state.getStatus()
        );

        assertFalse(state.isDeleting());

        assertEquals(
                "No se pudo eliminar la mercancía.",
                state.getDeleteErrorMessage()
        );

        viewModel.deleteWarehouseItem();

        assertEquals(2, deleteService.calls);
    }

    @Test
    public void detailNotFound_isIgnoredDuringSuccessfulDeletion()
            throws InterruptedException {

        RecordingDetailRepository detailRepository =
                new RecordingDetailRepository();

        RecordingDeleteService deleteService =
                new RecordingDeleteService();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailRepository,
                        deleteService,
                        12L
                );

        detailRepository.result.setValue(
                WarehouseItemDetailResult.found(
                        createWarehouseItem(12L)
                )
        );

        LiveDataTestUtil.getOrAwaitValue(
                viewModel.getUiState()
        );

        viewModel.deleteWarehouseItem();

        detailRepository.result.setValue(
                WarehouseItemDetailResult.notFound()
        );

        WarehouseItemDetailUiState state =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getUiState()
                );

        assertEquals(
                WarehouseItemDetailUiState.Status.CONTENT,
                state.getStatus()
        );

        assertTrue(state.isDeleting());

        deleteService.callback.accept(
                WarehouseItemDeleteResult.success(1)
        );

        UiEvent<Boolean> event =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getDeletionSuccess()
                );

        assertEquals(
                Boolean.TRUE,
                event.peekContent()
        );
    }

    private WarehouseItem createWarehouseItem(long id) {
        return new WarehouseItem(
                id,
                "MD",
                "1050",
                "B3",
                null,
                null,
                1000L,
                1000L
        );
    }

    private static final class
    RecordingDetailRepository
            extends WarehouseItemRepositoryStub {

        private final MutableLiveData<WarehouseItemDetailResult>
                result = new MutableLiveData<>();

        private int calls;

        @Override
        public LiveData<WarehouseItemDetailResult>
        observeById(long warehouseItemId) {
            calls++;
            return result;
        }
    }

    private static final class
    RecordingDeleteService
            extends WarehouseItemDeleteService {

        private int calls;
        private long deletedId;

        private Consumer<WarehouseItemDeleteResult>
                callback;

        private RecordingDeleteService() {
            super(new WarehouseItemRepositoryStub());
        }

        @Override
        public void delete(
                long warehouseItemId,
                Consumer<WarehouseItemDeleteResult> callback
        ) {
            calls++;
            deletedId = warehouseItemId;
            this.callback = callback;
        }
    }
}
