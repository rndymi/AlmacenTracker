package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemListUiState;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.SearchWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;
import java.util.Objects;

public final class WarehouseItemListViewModel extends ViewModel {

    private static final String DEFAULT_LOAD_ERROR_MESSAGE =
            "No se pudo cargar la mercancía.";

    private static final String DEFAULT_SEARCH_ERROR_MESSAGE =
            "No se pudo realizar la búsqueda.";

    private final MediatorLiveData<WarehouseItemListUiState> uiState =
            new MediatorLiveData<>();

    private final SearchWarehouseItemsUseCase
            searchWarehouseItemsUseCase;

    private final LiveData<WarehouseItemsResult> allItemsSource;

    private LiveData<WarehouseItemsResult> searchSource;

    private String searchQuery = "";
    private boolean databaseStateKnown;
    private boolean databaseEmpty;

    public WarehouseItemListViewModel(
            ObserveWarehouseItemsUseCase observeWarehouseItemsUseCase,
            SearchWarehouseItemsUseCase searchWarehouseItemsUseCase
    ) {
        Objects.requireNonNull(observeWarehouseItemsUseCase);

        this.searchWarehouseItemsUseCase =
                Objects.requireNonNull(
                        searchWarehouseItemsUseCase
                );

        uiState.setValue(
                WarehouseItemListUiState.loading(searchQuery)
        );

        allItemsSource =
                observeWarehouseItemsUseCase
                        .observeWarehouseItems();

        uiState.addSource(
                allItemsSource,
                this::handleAllItemsResult
        );
    }

    public LiveData<WarehouseItemListUiState> getUiState() {
        return uiState;
    }

    public void setSearchQuery(String query) {
        String normalizedQuery = query == null
                ? ""
                : query.trim();

        if (normalizedQuery.equals(searchQuery)) {
            return;
        }

        searchQuery = normalizedQuery;
        detachSearchSource();

        uiState.setValue(
                WarehouseItemListUiState.loading(searchQuery)
        );

        if (searchQuery.isEmpty()) {
            WarehouseItemsResult currentResult =
                    allItemsSource.getValue();

            if (currentResult != null) {
                handleAllItemsResult(currentResult);
            }

            return;
        }

        searchSource =
                searchWarehouseItemsUseCase.search(searchQuery);

        uiState.addSource(
                searchSource,
                this::handleSearchResult
        );
    }

    public void clearSearch() {
        setSearchQuery("");
    }

    private void handleAllItemsResult(
            WarehouseItemsResult result
    ) {
        if (!(result instanceof WarehouseItemsResult.Success)) {
            databaseStateKnown = false;

            uiState.setValue(
                    WarehouseItemListUiState.error(
                            searchQuery,
                            searchQuery.isEmpty()
                                    ? DEFAULT_LOAD_ERROR_MESSAGE
                                    : DEFAULT_SEARCH_ERROR_MESSAGE
                    )
            );

            return;
        }

        List<WarehouseItem> items =
                ((WarehouseItemsResult.Success) result)
                        .getItems();

        databaseStateKnown = true;
        databaseEmpty = items.isEmpty();

        if (!searchQuery.isEmpty()) {
            WarehouseItemsResult currentSearchResult =
                    searchSource == null
                            ? null
                            : searchSource.getValue();

            if (currentSearchResult != null) {
                handleSearchResult(currentSearchResult);
            }

            return;
        }

        if (databaseEmpty) {
            uiState.setValue(
                    WarehouseItemListUiState.emptyDatabase()
            );
        } else {
            uiState.setValue(
                    WarehouseItemListUiState.content(
                            items,
                            searchQuery
                    )
            );
        }
    }

    private void handleSearchResult(
            WarehouseItemsResult result
    ) {
        if (!(result instanceof WarehouseItemsResult.Success)) {
            uiState.setValue(
                    WarehouseItemListUiState.error(
                            searchQuery,
                            DEFAULT_SEARCH_ERROR_MESSAGE
                    )
            );

            return;
        }

        if (!databaseStateKnown) {
            uiState.setValue(
                    WarehouseItemListUiState.loading(
                            searchQuery
                    )
            );

            return;
        }

        List<WarehouseItem> items =
                ((WarehouseItemsResult.Success) result)
                        .getItems();

        if (!items.isEmpty()) {
            uiState.setValue(
                    WarehouseItemListUiState.content(
                            items,
                            searchQuery
                    )
            );

            return;
        }

        if (databaseEmpty) {
            uiState.setValue(
                    WarehouseItemListUiState.emptyDatabase()
            );
        } else {
            uiState.setValue(
                    WarehouseItemListUiState.noResults(
                            searchQuery
                    )
            );
        }
    }

    private void detachSearchSource() {
        if (searchSource == null) {
            return;
        }

        uiState.removeSource(searchSource);
        searchSource = null;
    }
}