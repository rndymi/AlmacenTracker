package com.rndymi.almacentracker.adapter.in.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.rndymi.almacentracker.AlmacenTrackerApplication;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.adapter.in.ui.adapter.WarehouseItemAdapter;
import com.rndymi.almacentracker.adapter.in.ui.state.NoResultsReason;
import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemListUiState;
import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemSelectionUiState;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemListViewModel;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemListViewModelFactory;
import com.rndymi.almacentracker.application.port.in.PositionFilter;
import com.rndymi.almacentracker.application.result.DeleteWarehouseItemsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptions;
import com.rndymi.almacentracker.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public final class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private WarehouseItemAdapter warehouseItemAdapter;
    private WarehouseItemListViewModel viewModel;

    private boolean renderingControls;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(
                getLayoutInflater()
        );

        setContentView(binding.getRoot());

        configureToolbar();
        configureRecyclerView();
        configureViewModel();
        configureActions();
        observeUiState();
    }

    private void configureToolbar() {
        setSupportActionBar(binding.toolbar);

        binding.selectionToolbar.inflateMenu(
                R.menu.menu_selection
        );

        binding.selectionToolbar.setNavigationOnClickListener(
                ignored -> viewModel.clearSelection()
        );

        binding.selectionToolbar.setOnMenuItemClickListener(
                item -> {
                    if (item.getItemId()
                            == R.id.action_delete_selection) {
                        showDeleteSelectionConfirmation();
                        return true;
                    }

                    return false;
                }
        );
    }

    private void configureRecyclerView() {
        warehouseItemAdapter =
                new WarehouseItemAdapter(
                        new WarehouseItemAdapter
                                .WarehouseItemInteractionListener() {
                            @Override
                            public void onWarehouseItemClick(
                                    long warehouseItemId
                            ) {
                                if (viewModel.hasSelection()) {
                                    viewModel.toggleSelection(
                                            warehouseItemId
                                    );
                                    return;
                                }

                                openWarehouseItemDetail(
                                        warehouseItemId
                                );
                            }

                            @Override
                            public void onWarehouseItemLongClick(
                                    long warehouseItemId
                            ) {
                                if (viewModel.hasSelection()) {
                                    viewModel.toggleSelection(
                                            warehouseItemId
                                    );
                                    return;
                                }

                                viewModel.startSelection(
                                        warehouseItemId
                                );
                            }
                        }
                );

        binding.warehouseRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.warehouseRecyclerView.setAdapter(
                warehouseItemAdapter
        );

        binding.warehouseRecyclerView.setHasFixedSize(true);
    }

    private void configureViewModel() {
        AlmacenTrackerApplication application =
                (AlmacenTrackerApplication)
                        getApplication();

        WarehouseItemListViewModelFactory factory =
                application
                        .getAppContainer()
                        .provideWarehouseItemListViewModelFactory();

        viewModel =
                new ViewModelProvider(this, factory)
                        .get(
                                WarehouseItemListViewModel.class
                        );
    }

    private void configureActions() {
        binding.addWarehouseItemFab.setOnClickListener(
                ignored -> openWarehouseItemForm()
        );

        binding.emptyStateRegisterButton.setOnClickListener(
                ignored -> openWarehouseItemForm()
        );

        binding.searchEditText.addTextChangedListener(
                SimpleTextWatcher.afterTextChanged(
                        query -> {
                            if (!renderingControls) {
                                viewModel.setSearchQuery(query);
                            }
                        }
                )
        );

        binding.categoryFilterDropdown
                .setOnItemClickListener(
                        (parent, view, position, id) -> {
                            if (renderingControls) {
                                return;
                            }

                            String selectedValue =
                                    parent.getItemAtPosition(
                                            position
                                    ).toString();

                            viewModel.setCategoryFilter(
                                    position == 0
                                            ? null
                                            : selectedValue
                            );
                        }
                );

        binding.siteFilterDropdown
                .setOnItemClickListener(
                        (parent, view, position, id) -> {
                            if (renderingControls) {
                                return;
                            }

                            String selectedValue =
                                    parent.getItemAtPosition(
                                            position
                                    ).toString();

                            viewModel.setSiteFilter(
                                    position == 0
                                            ? null
                                            : selectedValue
                            );
                        }
                );

        binding.positionFilterDropdown
                .setOnItemClickListener(
                        (parent, view, position, id) -> {
                            if (renderingControls) {
                                return;
                            }

                            String selectedValue =
                                    parent.getItemAtPosition(
                                            position
                                    ).toString();

                            if (position == 0) {
                                viewModel.setPositionFilter(
                                        PositionFilter.all()
                                );
                                return;
                            }

                            if (selectedValue.equals(
                                    getString(
                                            R.string
                                                    .without_position_option
                                    )
                            )) {
                                viewModel.setPositionFilter(
                                        PositionFilter
                                                .withoutPosition()
                                );
                                return;
                            }

                            viewModel.setPositionFilter(
                                    PositionFilter.exact(
                                            selectedValue
                                    )
                            );
                        }
                );

        binding.clearFiltersButton.setOnClickListener(
                ignored -> viewModel.clearFilters()
        );

        binding.clearNoResultsButton.setOnClickListener(
                ignored -> recoverFromNoResults()
        );
    }

    private void observeUiState() {
        viewModel.getUiState().observe(
                this,
                this::render
        );

        viewModel.getSelectionUiState().observe(
                this,
                this::renderSelection
        );

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (viewModel.hasSelection()) {
                            viewModel.clearSelection();
                            return;
                        }

                        setEnabled(false);
                        getOnBackPressedDispatcher()
                                .onBackPressed();
                    }
                }
        );
    }

    private void openWarehouseItemForm() {
        if (viewModel.hasSelection()) {
            return;
        }

        Intent intent = new Intent(
                this,
                ItemFormActivity.class
        );

        startActivity(intent);
    }

    private void recoverFromNoResults() {
        WarehouseItemListUiState state =
                viewModel.getUiState().getValue();

        if (state == null
                || state.getStatus()
                != WarehouseItemListUiState.Status.NO_RESULTS) {
            return;
        }

        switch (state.getNoResultsReason()) {
            case SEARCH:
                viewModel.clearSearch();
                break;

            case FILTERS:
                viewModel.clearFilters();
                break;

            case SEARCH_AND_FILTERS:
                viewModel.clearAllCriteria();
                break;
        }
    }

    private void openWarehouseItemDetail(
            long warehouseItemId
    ) {
        Intent intent =
                ItemDetailActivity.createIntent(
                        this,
                        warehouseItemId
                );

        startActivity(intent);
    }

    private void render(
            WarehouseItemListUiState state
    ) {
        renderCriteria(state);
        renderFilterOptions(state);
        renderActiveFilters(state);

        hideAllContentStates();

        switch (state.getStatus()) {
            case LOADING:
                binding.loadingProgress.setVisibility(
                        View.VISIBLE
                );
                break;

            case CONTENT:
                warehouseItemAdapter.submitList(
                        state.getItems()
                );

                binding.warehouseRecyclerView.setVisibility(
                        View.VISIBLE
                );
                break;

            case EMPTY_DATABASE:
                warehouseItemAdapter.submitList(null);

                binding.emptyState.setVisibility(
                        View.VISIBLE
                );
                break;

            case NO_RESULTS:
                warehouseItemAdapter.submitList(null);

                binding.noResultsText.setText(
                        buildNoResultsMessage(state)
                );

                binding.clearNoResultsButton.setText(
                        getNoResultsActionText(
                                state.getNoResultsReason()
                        )
                );

                binding.noResultsState.setVisibility(
                        View.VISIBLE
                );
                break;

            case ERROR:
                warehouseItemAdapter.submitList(null);

                binding.errorText.setText(
                        state.getErrorMessage()
                );

                binding.errorText.setVisibility(
                        View.VISIBLE
                );

                Log.e(
                        TAG,
                        "Warehouse items could not be filtered"
                );
                break;
        }
    }

    private void renderCriteria(
            WarehouseItemListUiState state
    ) {
        renderingControls = true;

        renderSearchQuery(state.getQuery());

        binding.categoryFilterDropdown.setText(
                state.getSelectedCategory() == null
                        ? getString(
                        R.string.all_categories_option
                )
                        : state.getSelectedCategory(),
                false
        );

        binding.siteFilterDropdown.setText(
                state.getSelectedSite() == null
                        ? getString(
                        R.string.all_sites_option
                )
                        : state.getSelectedSite(),
                false
        );

        binding.positionFilterDropdown.setText(
                getPositionFilterLabel(
                        state.getSelectedPositionFilter()
                ),
                false
        );

        renderingControls = false;
    }

    private void renderSearchQuery(String query) {
        String currentText =
                binding.searchEditText.getText() == null
                        ? ""
                        : binding.searchEditText
                        .getText()
                        .toString();

        if (currentText.equals(query)) {
            return;
        }

        binding.searchEditText.setText(query);
        binding.searchEditText.setSelection(query.length());
    }

    private void renderFilterOptions(
            WarehouseItemListUiState state
    ) {
        WarehouseItemFilterOptions options =
                state.getFilterOptions();

        binding.categoryFilterDropdown.setAdapter(
                createDropdownAdapter(
                        R.string.all_categories_option,
                        options.getCategories(),
                        false
                )
        );

        binding.siteFilterDropdown.setAdapter(
                createDropdownAdapter(
                        R.string.all_sites_option,
                        options.getSites(),
                        false
                )
        );

        binding.positionFilterDropdown.setAdapter(
                createDropdownAdapter(
                        R.string.all_positions_option,
                        options.getPositions(),
                        options.hasItemsWithoutPosition()
                )
        );
    }

    private ArrayAdapter<String> createDropdownAdapter(
            int generalOptionResource,
            List<String> values,
            boolean includeWithoutPosition
    ) {
        List<String> options = new ArrayList<>();

        options.add(getString(generalOptionResource));

        if (includeWithoutPosition) {
            options.add(
                    getString(
                            R.string.without_position_option
                    )
            );
        }

        options.addAll(values);

        return new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                options
        );
    }

    private void renderActiveFilters(
            WarehouseItemListUiState state
    ) {
        boolean hasActiveFilters =
                state.hasActiveFilters();

        binding.activeFiltersText.setVisibility(
                hasActiveFilters
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.clearFiltersButton.setVisibility(
                hasActiveFilters
                        ? View.VISIBLE
                        : View.GONE
        );

        if (hasActiveFilters) {
            binding.activeFiltersText.setText(
                    getString(
                            R.string.active_filters_format,
                            state.getActiveFilterCount()
                    )
            );
        }
    }

    private String getPositionFilterLabel(
            PositionFilter filter
    ) {
        switch (filter.getType()) {
            case WITHOUT_POSITION:
                return getString(
                        R.string.without_position_option
                );

            case EXACT_VALUE:
                return filter.getValue();

            case ALL:
            default:
                return getString(
                        R.string.all_positions_option
                );
        }
    }

    private String buildNoResultsMessage(
            WarehouseItemListUiState state
    ) {
        switch (state.getNoResultsReason()) {
            case SEARCH_AND_FILTERS:
                return getString(
                        R.string
                                .warehouse_search_filter_no_results,
                        state.getQuery()
                );

            case FILTERS:
                return getString(
                        R.string.warehouse_filter_no_results
                );

            case SEARCH:
            default:
                return getString(
                        R.string.warehouse_no_results,
                        state.getQuery()
                );
        }
    }

    private int getNoResultsActionText(
            NoResultsReason reason
    ) {
        switch (reason) {
            case FILTERS:
                return R.string.clear_filters_action;

            case SEARCH_AND_FILTERS:
                return R.string.clear_all_criteria_action;

            case SEARCH:
            default:
                return R.string.clear_search_action;
        }
    }

    private void hideAllContentStates() {
        binding.loadingProgress.setVisibility(View.GONE);
        binding.warehouseRecyclerView.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.GONE);
        binding.noResultsState.setVisibility(View.GONE);
        binding.errorText.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding.warehouseRecyclerView.setAdapter(null);
        binding = null;
    }

    private void showDeleteSelectionConfirmation() {
        WarehouseItemSelectionUiState selectionState =
                viewModel
                        .getSelectionUiState()
                        .getValue();

        if (selectionState == null
                || !selectionState.isSelectionMode()
                || selectionState.isDeleting()) {
            return;
        }

        int selectedCount =
                selectionState.getSelectedCount();

        String message =
                selectedCount == 1
                        ? getString(
                        R.string
                        .delete_one_selected_item_message
                )
                        : getString(
                        R.string
                        .delete_multiple_selected_items_message,
                        selectedCount
                );

        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        R.string
                                .delete_selected_items_title
                )
                .setMessage(message)
                .setNegativeButton(
                        R.string.cancel_action,
                        null
                )
                .setPositiveButton(
                        R.string.delete_action,
                        (dialog, which) ->
                                viewModel.deleteSelectedItems()
                )
                .show();
    }

    private void renderSelection(
            WarehouseItemSelectionUiState state
    ) {
        boolean selectionMode =
                state.isSelectionMode();

        binding.toolbar.setVisibility(
                selectionMode
                        ? View.GONE
                        : View.VISIBLE
        );

        binding.selectionToolbar.setVisibility(
                selectionMode
                        ? View.VISIBLE
                        : View.GONE
        );

        warehouseItemAdapter.setSelectedIds(
                state.getSelectedIds()
        );

        if (selectionMode) {
            binding.selectionToolbar.setTitle(
                    state.getSelectedCount() == 1
                            ? getString(
                            R.string
                            .selected_item_count
                    )
                            : getString(
                            R.string
                            .selected_items_count,
                            state.getSelectedCount()
                    )
            );
        }

        boolean controlsEnabled =
                !selectionMode
                        && !state.isDeleting();

        setQueryControlsEnabled(
                controlsEnabled
        );

        binding.addWarehouseItemFab.setVisibility(
                selectionMode
                        ? View.GONE
                        : View.VISIBLE
        );

        binding.selectionDeleteProgress.setVisibility(
                state.isDeleting()
                        ? View.VISIBLE
                        : View.GONE
        );

        if (selectionMode) {
            binding.selectionToolbar
                    .getMenu()
                    .findItem(
                            R.id.action_delete_selection
                    )
                    .setEnabled(
                            !state.isDeleting()
                    );
        }

        consumeSelectionResult(state);
    }

    private void setQueryControlsEnabled(
            boolean enabled
    ) {
        binding.searchInputLayout.setEnabled(enabled);
        binding.searchEditText.setEnabled(enabled);

        binding.categoryFilterLayout.setEnabled(enabled);
        binding.categoryFilterDropdown.setEnabled(enabled);

        binding.siteFilterLayout.setEnabled(enabled);
        binding.siteFilterDropdown.setEnabled(enabled);

        binding.positionFilterLayout.setEnabled(enabled);
        binding.positionFilterDropdown.setEnabled(enabled);

        binding.clearFiltersButton.setEnabled(enabled);
        binding.emptyStateRegisterButton.setEnabled(enabled);
        binding.clearNoResultsButton.setEnabled(enabled);
    }

    private void consumeSelectionResult(
            WarehouseItemSelectionUiState state
    ) {
        if (state.getResultEvent() == null) {
            return;
        }

        DeleteWarehouseItemsResult result =
                state.getResultEvent()
                        .getContentIfNotHandled();

        if (result == null) {
            return;
        }

        switch (result.getStatus()) {
            case SUCCESS:
                showMessage(
                        getString(
                                R.string.warehouse_items_deleted,
                                result.getDeletedCount()
                        )
                );
                break;

            case PARTIAL_SUCCESS:
                showMessage(
                        getString(
                                R.string
                                        .warehouse_items_partial_deleted,
                                result.getDeletedCount(),
                                result.getRequestedCount()
                        )
                );
                break;

            case NOT_FOUND:
                showMessage(
                        getString(
                                R.string
                                        .warehouse_items_not_found
                        )
                );
                break;

            case EMPTY_SELECTION:
                break;

            case INVALID_IDS:
                showMessage(
                        getString(
                                R.string
                                        .warehouse_items_invalid_selection
                        )
                );
                break;

            case PERSISTENCE_ERROR:
                Log.e(
                        TAG,
                        "Multiple warehouse item deletion failed",
                        result.getCause()
                );

                showMessage(
                        getString(
                                R.string
                                        .warehouse_items_delete_error
                        )
                );
                break;
        }
    }

    private void showMessage(String message) {
        android.widget.Toast.makeText(
                this,
                message,
                android.widget.Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_data_management) {
            startActivity(
                    new Intent(this, DataManagementActivity.class)
            );
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}