package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.adapter.in.ui.state.UiEvent;
import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemDetailUiState;
import com.rndymi.almacentracker.application.port.in.DeleteWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.in.GetWarehouseItemDetailUseCase;
import com.rndymi.almacentracker.application.result.DeleteWarehouseItemResult;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.testutil.LiveDataTestUtil;

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

        FakeGetWarehouseItemDetailUseCase detailUseCase =
                new FakeGetWarehouseItemDetailUseCase();

        FakeDeleteWarehouseItemUseCase deleteUseCase =
                new FakeDeleteWarehouseItemUseCase();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailUseCase,
                        deleteUseCase,
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

        assertEquals(0, detailUseCase.calls);
    }

    @Test
    public void result_exposesContent_whenItemExists()
            throws InterruptedException {

        FakeGetWarehouseItemDetailUseCase detailUseCase =
                new FakeGetWarehouseItemDetailUseCase();

        FakeDeleteWarehouseItemUseCase deleteUseCase =
                new FakeDeleteWarehouseItemUseCase();

        WarehouseItem warehouseItem =
                createWarehouseItem(3L);

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailUseCase,
                        deleteUseCase,
                        3L
                );

        detailUseCase.result.setValue(
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

        FakeGetWarehouseItemDetailUseCase detailUseCase =
                new FakeGetWarehouseItemDetailUseCase();

        FakeDeleteWarehouseItemUseCase deleteUseCase =
                new FakeDeleteWarehouseItemUseCase();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailUseCase,
                        deleteUseCase,
                        7L
                );

        detailUseCase.result.setValue(
                WarehouseItemDetailResult.found(
                        createWarehouseItem(7L)
                )
        );

        viewModel.deleteWarehouseItem();

        WarehouseItemDetailUiState state =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getUiState()
                );

        assertTrue(state.isDeleting());
        assertEquals(1, deleteUseCase.calls);
        assertEquals(7L, deleteUseCase.deletedId);
    }

    @Test
    public void delete_ignoresSecondCallWhileDeleting() {
        FakeGetWarehouseItemDetailUseCase detailUseCase =
                new FakeGetWarehouseItemDetailUseCase();

        FakeDeleteWarehouseItemUseCase deleteUseCase =
                new FakeDeleteWarehouseItemUseCase();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailUseCase,
                        deleteUseCase,
                        7L
                );

        detailUseCase.result.setValue(
                WarehouseItemDetailResult.found(
                        createWarehouseItem(7L)
                )
        );

        viewModel.deleteWarehouseItem();
        viewModel.deleteWarehouseItem();

        assertEquals(1, deleteUseCase.calls);
    }

    @Test
    public void delete_emitsSuccessOnlyOnce()
            throws InterruptedException {

        FakeGetWarehouseItemDetailUseCase detailUseCase =
                new FakeGetWarehouseItemDetailUseCase();

        FakeDeleteWarehouseItemUseCase deleteUseCase =
                new FakeDeleteWarehouseItemUseCase();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailUseCase,
                        deleteUseCase,
                        5L
                );

        detailUseCase.result.setValue(
                WarehouseItemDetailResult.found(
                        createWarehouseItem(5L)
                )
        );

        viewModel.deleteWarehouseItem();

        deleteUseCase.callback.accept(
                DeleteWarehouseItemResult.success()
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

        FakeGetWarehouseItemDetailUseCase detailUseCase =
                new FakeGetWarehouseItemDetailUseCase();

        FakeDeleteWarehouseItemUseCase deleteUseCase =
                new FakeDeleteWarehouseItemUseCase();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailUseCase,
                        deleteUseCase,
                        8L
                );

        detailUseCase.result.setValue(
                WarehouseItemDetailResult.found(
                        createWarehouseItem(8L)
                )
        );

        viewModel.deleteWarehouseItem();

        deleteUseCase.callback.accept(
                DeleteWarehouseItemResult.notFound()
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

        FakeGetWarehouseItemDetailUseCase detailUseCase =
                new FakeGetWarehouseItemDetailUseCase();

        FakeDeleteWarehouseItemUseCase deleteUseCase =
                new FakeDeleteWarehouseItemUseCase();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailUseCase,
                        deleteUseCase,
                        10L
                );

        detailUseCase.result.setValue(
                WarehouseItemDetailResult.found(
                        createWarehouseItem(10L)
                )
        );

        viewModel.deleteWarehouseItem();

        deleteUseCase.callback.accept(
                DeleteWarehouseItemResult.persistenceError(
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

        assertEquals(2, deleteUseCase.calls);
    }

    @Test
    public void detailNotFound_isIgnoredDuringSuccessfulDeletion()
            throws InterruptedException {

        FakeGetWarehouseItemDetailUseCase detailUseCase =
                new FakeGetWarehouseItemDetailUseCase();

        FakeDeleteWarehouseItemUseCase deleteUseCase =
                new FakeDeleteWarehouseItemUseCase();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        detailUseCase,
                        deleteUseCase,
                        12L
                );

        detailUseCase.result.setValue(
                WarehouseItemDetailResult.found(
                        createWarehouseItem(12L)
                )
        );

        viewModel.deleteWarehouseItem();

        detailUseCase.result.setValue(
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

        deleteUseCase.callback.accept(
                DeleteWarehouseItemResult.success()
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
    FakeGetWarehouseItemDetailUseCase
            implements GetWarehouseItemDetailUseCase {

        private final MutableLiveData<WarehouseItemDetailResult>
                result = new MutableLiveData<>();

        private int calls;

        @Override
        public LiveData<WarehouseItemDetailResult>
        observeWarehouseItemDetail(long warehouseItemId) {
            calls++;
            return result;
        }
    }

    private static final class
    FakeDeleteWarehouseItemUseCase
            implements DeleteWarehouseItemUseCase {

        private int calls;
        private long deletedId;

        private Consumer<DeleteWarehouseItemResult>
                callback;

        @Override
        public void deleteWarehouseItem(
                long warehouseItemId,
                Consumer<DeleteWarehouseItemResult> callback
        ) {
            calls++;
            deletedId = warehouseItemId;
            this.callback = callback;
        }
    }
}