package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemDetailUiState;
import com.rndymi.almacentracker.application.port.in.GetWarehouseItemDetailUseCase;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.testutil.LiveDataTestUtil;

import org.junit.Rule;
import org.junit.Test;

public final class WarehouseItemDetailViewModelTest {

    @Rule
    public final InstantTaskExecutorRule executorRule =
            new InstantTaskExecutorRule();

    @Test
    public void constructor_exposesInvalidId_whenIdIsNotValid()
            throws InterruptedException {

        FakeGetWarehouseItemDetailUseCase useCase =
                new FakeGetWarehouseItemDetailUseCase();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        useCase,
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

        assertEquals(0, useCase.calls);
    }

    @Test
    public void result_exposesContent_whenItemExists()
            throws InterruptedException {

        FakeGetWarehouseItemDetailUseCase useCase =
                new FakeGetWarehouseItemDetailUseCase();

        WarehouseItem warehouseItem = new WarehouseItem(
                3L,
                "MD",
                "1050",
                "B3",
                null,
                null,
                1000L,
                1000L
        );

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        useCase,
                        3L
                );

        useCase.result.setValue(
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
    }

    @Test
    public void result_exposesNotFound_whenItemDoesNotExist()
            throws InterruptedException {

        FakeGetWarehouseItemDetailUseCase useCase =
                new FakeGetWarehouseItemDetailUseCase();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        useCase,
                        99L
                );

        useCase.result.setValue(
                WarehouseItemDetailResult.notFound()
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
    public void result_exposesError_whenRepositoryFails()
            throws InterruptedException {

        FakeGetWarehouseItemDetailUseCase useCase =
                new FakeGetWarehouseItemDetailUseCase();

        WarehouseItemDetailViewModel viewModel =
                new WarehouseItemDetailViewModel(
                        useCase,
                        4L
                );

        useCase.result.setValue(
                WarehouseItemDetailResult.error(
                        new IllegalStateException("Failure")
                )
        );

        WarehouseItemDetailUiState state =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getUiState()
                );

        assertEquals(
                WarehouseItemDetailUiState.Status.ERROR,
                state.getStatus()
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
}