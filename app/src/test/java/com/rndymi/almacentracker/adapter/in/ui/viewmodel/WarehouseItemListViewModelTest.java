package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import static com.rndymi.almacentracker.testutil.LiveDataTestUtil.getOrAwaitValue;
import static org.junit.Assert.assertEquals;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemListUiState;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;

public class WarehouseItemListViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    @Test
    public void exposesContentWhenItemsExist() throws Exception {
        MutableLiveData<WarehouseItemsResult> source =
                new MutableLiveData<>();

        ObserveWarehouseItemsUseCase useCase = () -> source;

        WarehouseItemListViewModel viewModel =
                new WarehouseItemListViewModel(useCase);

        WarehouseItem item = new WarehouseItem(
                1L,
                "MR",
                "1050",
                "A1",
                "Nivel 2",
                null,
                100L,
                100L
        );

        source.setValue(
                WarehouseItemsResult.success(
                        Collections.singletonList(item)
                )
        );

        WarehouseItemListUiState state =
                getOrAwaitValue(viewModel.getUiState());

        assertEquals(
                WarehouseItemListUiState.Status.CONTENT,
                state.getStatus()
        );
        assertEquals(1, state.getItems().size());
    }

    @Test
    public void exposesEmptyWhenNoItemsExist() throws Exception {
        MutableLiveData<WarehouseItemsResult> source =
                new MutableLiveData<>();

        ObserveWarehouseItemsUseCase useCase = () -> source;

        WarehouseItemListViewModel viewModel =
                new WarehouseItemListViewModel(useCase);

        source.setValue(
                WarehouseItemsResult.success(
                        Collections.emptyList()
                )
        );

        WarehouseItemListUiState state =
                getOrAwaitValue(viewModel.getUiState());

        assertEquals(
                WarehouseItemListUiState.Status.EMPTY,
                state.getStatus()
        );
    }

    @Test
    public void exposesErrorWhenRepositoryFails() throws Exception {
        MutableLiveData<WarehouseItemsResult> source =
                new MutableLiveData<>();

        ObserveWarehouseItemsUseCase useCase = () -> source;

        WarehouseItemListViewModel viewModel =
                new WarehouseItemListViewModel(useCase);

        source.setValue(
                WarehouseItemsResult.error(
                        new IllegalStateException(
                                "Database failure"
                        )
                )
        );

        WarehouseItemListUiState state =
                getOrAwaitValue(viewModel.getUiState());

        assertEquals(
                WarehouseItemListUiState.Status.ERROR,
                state.getStatus()
        );
    }
}