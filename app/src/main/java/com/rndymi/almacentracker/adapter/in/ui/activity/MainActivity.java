package com.rndymi.almacentracker.adapter.in.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rndymi.almacentracker.AlmacenTrackerApplication;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.adapter.in.ui.adapter.WarehouseItemAdapter;
import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemListUiState;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemListViewModel;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemListViewModelFactory;
import com.rndymi.almacentracker.databinding.ActivityMainBinding;

public final class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private WarehouseItemAdapter warehouseItemAdapter;
    private WarehouseItemListViewModel viewModel;
    private boolean renderingQuery;

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
    }

    private void configureRecyclerView() {
        warehouseItemAdapter =
                new WarehouseItemAdapter(
                        this::openWarehouseItemDetail
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
                ignored -> {
                    Intent intent = new Intent(
                            this,
                            ItemFormActivity.class
                    );

                    startActivity(intent);
                }
        );

        binding.searchEditText.addTextChangedListener(
                SimpleTextWatcher.afterTextChanged(
                        query -> {
                            if (!renderingQuery) {
                                viewModel.setSearchQuery(query);
                            }
                        }
                )
        );

        binding.clearSearchButton.setOnClickListener(
                ignored -> viewModel.clearSearch()
        );
    }

    private void observeUiState() {
        viewModel.getUiState().observe(
                this,
                this::render
        );
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
        renderSearchQuery(state.getQuery());
        hideAllStates();

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
                        getString(
                                R.string.warehouse_no_results,
                                state.getQuery()
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
                        "Warehouse items could not be loaded"
                );
                break;
        }
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

        renderingQuery = true;

        binding.searchEditText.setText(query);
        binding.searchEditText.setSelection(
                query.length()
        );

        renderingQuery = false;
    }

    private void hideAllStates() {
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
}