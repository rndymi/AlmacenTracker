package com.rndymi.almacentracker.feature.inventory.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.data.repository.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.data.repository.WarehouseItemFilterOptions;
import com.rndymi.almacentracker.data.repository.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.data.repository.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.domain.rule.WarehouseItemNormalizer;
import com.rndymi.almacentracker.feature.inventory.common.WarehouseItemDeleteResult;
import com.rndymi.almacentracker.feature.inventory.common.WarehouseItemDeleteService;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;

import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class WarehouseItemSelectionViewModelTest {

    @Rule
    public final InstantTaskExecutorRule
            instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    @Test
    public void longPressSelectionAndToggleAreStable() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemListViewModel viewModel =
                dependencies.createContentViewModel();

        viewModel.startSelection(1L);
        viewModel.toggleSelection(2L);

        WarehouseItemSelectionUiState selected =
                viewModel.getSelectionUiState().getValue();

        assertTrue(viewModel.hasSelection());
        assertEquals(
                new LinkedHashSet<>(
                        Arrays.asList(1L, 2L)
                ),
                selected.getSelectedIds()
        );

        viewModel.toggleSelection(1L);

        assertEquals(
                new LinkedHashSet<>(
                        Arrays.asList(2L)
                ),
                viewModel.getSelectionUiState()
                        .getValue()
                        .getSelectedIds()
        );
    }

    @Test
    public void clearSelectionCancelsSelectionMode() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemListViewModel viewModel =
                dependencies.createContentViewModel();

        viewModel.startSelection(1L);
        viewModel.clearSelection();

        assertFalse(viewModel.hasSelection());
        assertTrue(
                viewModel.getSelectionUiState()
                        .getValue()
                        .getSelectedIds()
                        .isEmpty()
        );
    }

    @Test
    public void invisibleOrInvalidIdsCannotBeSelected() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemListViewModel viewModel =
                dependencies.createContentViewModel();

        viewModel.startSelection(-1L);
        viewModel.startSelection(999L);

        assertFalse(viewModel.hasSelection());
    }

    @Test
    public void selectionSurvivesConfigurationStateReuse() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemListViewModel retainedViewModel =
                dependencies.createContentViewModel();

        retainedViewModel.startSelection(1L);

        WarehouseItemListViewModel afterRotation =
                retainedViewModel;

        assertTrue(afterRotation.hasSelection());
        assertEquals(
                new LinkedHashSet<>(
                        Arrays.asList(1L)
                ),
                afterRotation.getSelectionUiState()
                        .getValue()
                        .getSelectedIds()
        );
    }

    @Test
    public void multipleDeleteRunsOnceAndClearsSelection() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemListViewModel viewModel =
                dependencies.createContentViewModel();

        viewModel.startSelection(1L);
        viewModel.toggleSelection(2L);
        viewModel.deleteSelectedItems();
        viewModel.deleteSelectedItems();

        assertEquals(1, dependencies.deleteService.callCount);
        assertEquals(
                new LinkedHashSet<>(
                        Arrays.asList(1L, 2L)
                ),
                dependencies.deleteService.requestedIds
        );

        dependencies.deleteService.complete(
                WarehouseItemDeleteResult.success(2)
        );

        assertFalse(viewModel.hasSelection());
        assertTrue(
                viewModel.getSelectionUiState()
                        .getValue()
                        .getSelectedIds()
                        .isEmpty()
        );
    }

    @Test
    public void deletionErrorKeepsSelectionForRetry() {
        TestDependencies dependencies =
                new TestDependencies();
        WarehouseItemListViewModel viewModel =
                dependencies.createContentViewModel();

        viewModel.startSelection(1L);
        viewModel.deleteSelectedItems();
        dependencies.deleteService.complete(
                WarehouseItemDeleteResult.persistenceError(
                        1,
                        new IllegalStateException("delete")
                )
        );

        assertTrue(viewModel.hasSelection());
        assertEquals(
                new LinkedHashSet<>(
                        Arrays.asList(1L)
                ),
                viewModel.getSelectionUiState()
                        .getValue()
                        .getSelectedIds()
        );
    }

    private static final class TestDependencies {

        private final MutableLiveData<WarehouseItemsResult>
                allItems = new MutableLiveData<>();
        private final MutableLiveData<WarehouseItemsResult>
                filteredItems = new MutableLiveData<>();
        private final MutableLiveData<WarehouseItemFilterOptionsResult>
                filterOptions = new MutableLiveData<>();
        private final RecordingDeleteService
                deleteService =
                new RecordingDeleteService();

        private WarehouseItemListViewModel
        createContentViewModel() {
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
                                WarehouseItemFilterCriteria ignored
                        ) {
                            return filteredItems;
                        }

                        @Override
                        public MutableLiveData
                        <WarehouseItemFilterOptionsResult>
                        observeFilterOptions() {
                            return filterOptions;
                        }
                    };

            WarehouseItemListViewModel viewModel =
                    new WarehouseItemListViewModel(
                            repository,
                            deleteService,
                            new WarehouseItemCodeSearchService(
                                    repository,
                                    new WarehouseItemNormalizer()
                            )
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

            WarehouseItemsResult content =
                    WarehouseItemsResult.success(
                            Arrays.asList(
                                    item(1L, "100"),
                                    item(2L, "200")
                            )
                    );

            allItems.setValue(content);
            filteredItems.setValue(content);

            return viewModel;
        }

        private WarehouseItem item(long id, String code) {
            return new WarehouseItem(
                    id,
                    "MR",
                    code,
                    "A1",
                    null,
                    null,
                    100L,
                    100L
            );
        }
    }

    private static final class RecordingDeleteService
            extends WarehouseItemDeleteService {

        private int callCount;
        private Set<Long> requestedIds;
        private Consumer<WarehouseItemDeleteResult> callback;

        private RecordingDeleteService() {
            super(new WarehouseItemRepositoryStub());
        }

        @Override
        public void delete(
                Set<Long> warehouseItemIds,
                Consumer<WarehouseItemDeleteResult> callback
        ) {
            callCount++;
            requestedIds =
                    new LinkedHashSet<>(warehouseItemIds);
            this.callback = callback;
        }

        private void complete(
                WarehouseItemDeleteResult result
        ) {
            callback.accept(result);
        }
    }
}
