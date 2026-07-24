package com.rndymi.almacentracker.feature.inventory.list;

import static com.rndymi.almacentracker.testutil.LiveDataTestUtil.getOrAwaitValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.data.repository.PositionFilter;
import com.rndymi.almacentracker.data.repository.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.data.repository.WarehouseItemFilterOptions;
import com.rndymi.almacentracker.data.repository.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.data.repository.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.feature.inventory.common.WarehouseItemDeleteService;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;

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

        sources.filteredItems.setValue(
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

        sources.filteredItems.setValue(
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

        sources.filteredItems.setValue(
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

        sources.filteredItems.setValue(
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

        sources.filteredItems.setValue(
                WarehouseItemsResult.success(
                        Collections.emptyList()
                )
        );

        viewModel.clearSearch();

        sources.filteredItems.setValue(
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

        assertEquals("A1", sources.requestedCriteria.getQuery());
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

    @Test
    public void emptyDatabaseHasPriorityOverActiveCriteria()
            throws Exception {

        TestSources sources = new TestSources();
        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        viewModel.setSearchQuery("ZZZ");
        viewModel.setCategoryFilter("MR");

        sources.allItems.setValue(
                WarehouseItemsResult.success(
                        Collections.emptyList()
                )
        );

        sources.filteredItems.setValue(
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
    public void clearSearchPreservesFilters() {
        TestSources sources = new TestSources();

        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        viewModel.setSearchQuery("105");
        viewModel.setCategoryFilter("MR");

        viewModel.clearSearch();

        assertEquals(
                "",
                sources.requestedCriteria.getQuery()
        );

        assertEquals(
                "MR",
                sources.requestedCriteria.getCategory()
        );
    }

    @Test
    public void clearAllCriteriaRemovesSearchAndFilters() {
        TestSources sources = new TestSources();

        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        viewModel.setSearchQuery("105");
        viewModel.setCategoryFilter("MR");
        viewModel.setSiteFilter("A1");
        viewModel.setPositionFilter(
                PositionFilter.exact("Nivel 2")
        );

        viewModel.clearAllCriteria();

        assertEquals(
                "",
                sources.requestedCriteria.getQuery()
        );

        assertNull(
                sources.requestedCriteria.getCategory()
        );

        assertNull(
                sources.requestedCriteria.getSite()
        );

        assertEquals(
                PositionFilter.Type.ALL,
                sources.requestedCriteria
                        .getPositionFilter()
                        .getType()
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
                filteredItems = new MutableLiveData<>();

        private final MutableLiveData<WarehouseItemFilterOptionsResult>
                filterOptions = new MutableLiveData<>();

        private WarehouseItemFilterCriteria requestedCriteria;

        private WarehouseItemListViewModel createViewModel() {
            WarehouseItemRepositoryStub repository =
                    new WarehouseItemRepositoryStub() {
                        @Override
                        public MutableLiveData<WarehouseItemsResult>
                        observeAll() {
                            return allItems;
                        }

                        @Override
                        public MutableLiveData<WarehouseItemsResult>
                        filter(
                                WarehouseItemFilterCriteria criteria
                        ) {
                            requestedCriteria = criteria;
                            return filteredItems;
                        }

                        @Override
                        public MutableLiveData
                        <WarehouseItemFilterOptionsResult>
                        observeFilterOptions() {
                            return filterOptions;
                        }
                    };

            WarehouseItemDeleteService deleteService =
                    new WarehouseItemDeleteService(
                            new WarehouseItemRepositoryStub()
                    );

            WarehouseItemListViewModel viewModel =
                    new WarehouseItemListViewModel(
                            repository,
                            deleteService
                    );

            viewModel.getUiState().observeForever(
                    ignored -> {
                    }
            );

            filterOptions.setValue(
                    WarehouseItemFilterOptionsResult.success(
                            WarehouseItemFilterOptions.empty()
                    )
            );

            return viewModel;
        }
    }

    @Test
    public void appliesCategoryFilter() {
        TestSources sources = new TestSources();

        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        viewModel.setCategoryFilter("MR");

        assertEquals(
                "MR",
                sources.requestedCriteria.getCategory()
        );
    }

    @Test
    public void appliesSiteFilter() {
        TestSources sources = new TestSources();

        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        viewModel.setSiteFilter("A1");

        assertEquals(
                "A1",
                sources.requestedCriteria.getSite()
        );
    }

    @Test
    public void appliesWithoutPositionFilter() {
        TestSources sources = new TestSources();

        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        viewModel.setPositionFilter(
                PositionFilter.withoutPosition()
        );

        assertEquals(
                PositionFilter.Type.WITHOUT_POSITION,
                sources.requestedCriteria
                        .getPositionFilter()
                        .getType()
        );
    }

    @Test
    public void combinesSearchAndFilters() {
        TestSources sources = new TestSources();

        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        viewModel.setSearchQuery("105");
        viewModel.setCategoryFilter("MR");
        viewModel.setSiteFilter("A1");

        assertEquals(
                "105",
                sources.requestedCriteria.getQuery()
        );

        assertEquals(
                "MR",
                sources.requestedCriteria.getCategory()
        );

        assertEquals(
                "A1",
                sources.requestedCriteria.getSite()
        );
    }

    @Test
    public void clearFiltersPreservesSearchQuery() {
        TestSources sources = new TestSources();

        WarehouseItemListViewModel viewModel =
                sources.createViewModel();

        viewModel.setSearchQuery("105");
        viewModel.setCategoryFilter("MR");
        viewModel.setSiteFilter("A1");

        viewModel.clearFilters();

        assertEquals(
                "105",
                sources.requestedCriteria.getQuery()
        );

        assertNull(
                sources.requestedCriteria.getCategory()
        );

        assertNull(
                sources.requestedCriteria.getSite()
        );

        assertEquals(
                PositionFilter.Type.ALL,
                sources.requestedCriteria
                        .getPositionFilter()
                        .getType()
        );
    }
}
