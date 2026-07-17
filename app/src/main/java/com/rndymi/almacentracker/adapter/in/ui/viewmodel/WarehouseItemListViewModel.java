package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemListUiState;
import com.rndymi.almacentracker.application.port.in.FilterWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemFilterOptionsUseCase;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.PositionFilter;
import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptions;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;
import java.util.Objects;

public final class WarehouseItemListViewModel
        extends ViewModel {

    private static final String DEFAULT_LOAD_ERROR_MESSAGE =
            "No se pudo cargar la mercancía.";

    private static final String DEFAULT_FILTER_ERROR_MESSAGE =
            "No se pudieron aplicar los filtros.";

    private final MediatorLiveData<WarehouseItemListUiState>
            uiState = new MediatorLiveData<>();

    private final FilterWarehouseItemsUseCase
            filterWarehouseItemsUseCase;

    private final LiveData<WarehouseItemsResult>
            allItemsSource;

    private final LiveData<WarehouseItemFilterOptionsResult>
            filterOptionsSource;

    private LiveData<WarehouseItemsResult> filteredItemsSource;

    private WarehouseItemFilterCriteria criteria =
            WarehouseItemFilterCriteria.empty();

    private WarehouseItemFilterOptions filterOptions =
            WarehouseItemFilterOptions.empty();

    private boolean databaseStateKnown;
    private boolean databaseEmpty;
    private WarehouseItemsResult latestFilteredResult;

    public WarehouseItemListViewModel(
            ObserveWarehouseItemsUseCase observeWarehouseItemsUseCase,
            FilterWarehouseItemsUseCase filterWarehouseItemsUseCase,
            ObserveWarehouseItemFilterOptionsUseCase
                    observeFilterOptionsUseCase
    ) {
        Objects.requireNonNull(observeWarehouseItemsUseCase);

        this.filterWarehouseItemsUseCase =
                Objects.requireNonNull(
                        filterWarehouseItemsUseCase
                );

        Objects.requireNonNull(observeFilterOptionsUseCase);

        uiState.setValue(
                WarehouseItemListUiState.loading(
                        criteria,
                        filterOptions
                )
        );

        allItemsSource =
                observeWarehouseItemsUseCase
                        .observeWarehouseItems();

        filterOptionsSource =
                observeFilterOptionsUseCase
                        .observeFilterOptions();

        uiState.addSource(
                allItemsSource,
                this::handleAllItemsResult
        );

        uiState.addSource(
                filterOptionsSource,
                this::handleFilterOptionsResult
        );

        refreshFilteredSource();
    }

    public LiveData<WarehouseItemListUiState> getUiState() {
        return uiState;
    }

    public void setSearchQuery(String query) {
        updateCriteria(criteria.withQuery(query));
    }

    public void clearSearch() {
        setSearchQuery("");
    }

    public void setCategoryFilter(String category) {
        updateCriteria(
                criteria.withCategory(category)
        );
    }

    public void setSiteFilter(String site) {
        updateCriteria(
                criteria.withSite(site)
        );
    }

    public void setPositionFilter(
            PositionFilter positionFilter
    ) {
        updateCriteria(
                criteria.withPositionFilter(
                        positionFilter == null
                                ? PositionFilter.all()
                                : positionFilter
                )
        );
    }

    public void clearFilters() {
        updateCriteria(criteria.clearFilters());
    }

    private void updateCriteria(
            WarehouseItemFilterCriteria newCriteria
    ) {
        if (criteria.equals(newCriteria)) {
            return;
        }

        criteria = newCriteria;

        uiState.setValue(
                WarehouseItemListUiState.loading(
                        criteria,
                        filterOptions
                )
        );

        refreshFilteredSource();
    }

    private void refreshFilteredSource() {
        detachFilteredSource();

        filteredItemsSource =
                filterWarehouseItemsUseCase.filter(criteria);

        uiState.addSource(
                filteredItemsSource,
                this::handleFilteredItemsResult
        );
    }

    private void handleAllItemsResult(
            WarehouseItemsResult result
    ) {
        if (!(result instanceof WarehouseItemsResult.Success)) {
            databaseStateKnown = false;

            uiState.setValue(
                    WarehouseItemListUiState.error(
                            criteria,
                            filterOptions,
                            DEFAULT_LOAD_ERROR_MESSAGE
                    )
            );

            return;
        }

        List<WarehouseItem> items =
                ((WarehouseItemsResult.Success) result)
                        .getItems();

        databaseStateKnown = true;
        databaseEmpty = items.isEmpty();

        renderLatestResult();
    }

    private void handleFilteredItemsResult(
            WarehouseItemsResult result
    ) {
        latestFilteredResult = result;
        renderLatestResult();
    }

    private void handleFilterOptionsResult(
            WarehouseItemFilterOptionsResult result
    ) {
        if (result instanceof
                WarehouseItemFilterOptionsResult.Success) {

            filterOptions =
                    ((WarehouseItemFilterOptionsResult.Success)
                            result)
                            .getOptions();

            renderLatestResult();
            return;
        }

        uiState.setValue(
                WarehouseItemListUiState.error(
                        criteria,
                        filterOptions,
                        DEFAULT_FILTER_ERROR_MESSAGE
                )
        );
    }

    private void renderLatestResult() {
        if (!databaseStateKnown
                || latestFilteredResult == null) {

            uiState.setValue(
                    WarehouseItemListUiState.loading(
                            criteria,
                            filterOptions
                    )
            );

            return;
        }

        if (!(latestFilteredResult
                instanceof WarehouseItemsResult.Success)) {

            uiState.setValue(
                    WarehouseItemListUiState.error(
                            criteria,
                            filterOptions,
                            DEFAULT_FILTER_ERROR_MESSAGE
                    )
            );

            return;
        }

        if (databaseEmpty) {
            uiState.setValue(
                    WarehouseItemListUiState.emptyDatabase(
                            criteria,
                            filterOptions
                    )
            );

            return;
        }

        List<WarehouseItem> filteredItems =
                ((WarehouseItemsResult.Success)
                        latestFilteredResult)
                        .getItems();

        if (filteredItems.isEmpty()) {
            uiState.setValue(
                    WarehouseItemListUiState.noResults(
                            criteria,
                            filterOptions
                    )
            );

            return;
        }

        uiState.setValue(
                WarehouseItemListUiState.content(
                        filteredItems,
                        criteria,
                        filterOptions
                )
        );
    }

    private void detachFilteredSource() {
        if (filteredItemsSource == null) {
            return;
        }

        uiState.removeSource(filteredItemsSource);
        filteredItemsSource = null;
        latestFilteredResult = null;
    }
}