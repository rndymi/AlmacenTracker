package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import static com.rndymi.almacentracker.testutil.LiveDataTestUtil.getOrAwaitValue;
import static org.junit.Assert.assertEquals;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemListUiState;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.SearchWarehouseItemsUseCase;
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
    public void exposesContentWhenItemsExist()
            throws Exception {

        TestSources sources = new TestSources();
        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        sources.allItems.setValue(
                WarehouseItemsResult.success(
                        Collections.singletonList(
                                createItem()
                        )
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
    public void exposesEmptyDatabaseWhenNoItemsExist()
            throws Exception {

        TestSources sources = new TestSources();
        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        sources.allItems.setValue(
                WarehouseItemsResult.success(
                        Collections.emptyList()
                )
        );

        WarehouseItemListUiState state =
                getOrAwaitValue(viewModel.getUiState());

        assertEquals(
                WarehouseItemListUiState.Status.EMPTY_DATABASE,
                state.getStatus()
        );
    }

    @Test
    public void exposesNoResultsWhenSearchDoesNotMatch()
            throws Exception {

        TestSources sources = new TestSources();
        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        sources.allItems.setValue(
                WarehouseItemsResult.success(
                        Collections.singletonList(
                                createItem()
                        )
                )
        );

        viewModel.setSearchQuery("ZZZ");

        sources.searchItems.setValue(
                WarehouseItemsResult.success(
                        Collections.emptyList()
                )
        );

        WarehouseItemListUiState state =
                getOrAwaitValue(viewModel.getUiState());

        assertEquals(
                WarehouseItemListUiState.Status.NO_RESULTS,
                state.getStatus()
        );

        assertEquals("ZZZ", state.getQuery());
    }

    @Test
    public void exposesSearchContentWhenMatchesExist()
            throws Exception {

        TestSources sources = new TestSources();
        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        WarehouseItem item = createItem();

        sources.allItems.setValue(
                WarehouseItemsResult.success(
                        Collections.singletonList(item)
                )
        );

        viewModel.setSearchQuery("105");

        sources.searchItems.setValue(
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

        assertEquals("105", state.getQuery());
        assertEquals(1, state.getItems().size());
    }

    @Test
    public void clearsSearchAndRestoresCompleteList()
            throws Exception {

        TestSources sources = new TestSources();
        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        WarehouseItem item = createItem();

        sources.allItems.setValue(
                WarehouseItemsResult.success(
                        Collections.singletonList(item)
                )
        );

        viewModel.setSearchQuery("ZZZ");

        sources.searchItems.setValue(
                WarehouseItemsResult.success(
                        Collections.emptyList()
                )
        );

        viewModel.clearSearch();

        WarehouseItemListUiState state =
                getOrAwaitValue(viewModel.getUiState());

        assertEquals(
                WarehouseItemListUiState.Status.CONTENT,
                state.getStatus()
        );

        assertEquals("", state.getQuery());
        assertEquals(1, state.getItems().size());
    }

    @Test
    public void trimsSearchQueryBeforeDelegating()
            throws Exception {

        TestSources sources = new TestSources();
        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        sources.allItems.setValue(
                WarehouseItemsResult.success(
                        Collections.singletonList(
                                createItem()
                        )
                )
        );

        viewModel.setSearchQuery("  A1  ");

        assertEquals("A1", sources.requestedQuery);
    }

    @Test
    public void exposesErrorWhenRepositoryFails()
            throws Exception {

        TestSources sources = new TestSources();
        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        sources.allItems.setValue(
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

    private WarehouseItem createItem() {
        return new WarehouseItem(
                1L,
                "MR",
                "1050",
                "A1",
                "Nivel 2",
                null,
                100L,
                100L
        );
    }

    private static final class TestSources {

        private final MutableLiveData<WarehouseItemsResult>
                allItems = new MutableLiveData<>();

        private final MutableLiveData<WarehouseItemsResult>
                searchItems = new MutableLiveData<>();

        private String requestedQuery;

        private WarehouseItemListViewModel createViewModel() {
            ObserveWarehouseItemsUseCase observeUseCase =
                    () -> allItems;

            SearchWarehouseItemsUseCase searchUseCase =
                    query -> {
                        requestedQuery = query;
                        return searchItems;
                    };

            return new WarehouseItemListViewModel(
                    observeUseCase,
                    searchUseCase
            );
        }
    }
}