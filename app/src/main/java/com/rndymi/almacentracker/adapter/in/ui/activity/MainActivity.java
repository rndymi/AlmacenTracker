package com.rndymi.almacentracker.adapter.in.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rndymi.almacentracker.AlmacenTrackerApplication;
import com.rndymi.almacentracker.adapter.in.ui.adapter.WarehouseItemAdapter;
import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemListUiState;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemListViewModel;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemListViewModelFactory;
import com.rndymi.almacentracker.databinding.ActivityMainBinding;

import android.content.Intent;

public final class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private WarehouseItemAdapter warehouseItemAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configureToolbar();
        configureRecyclerView();
        configureActions();
        observeUiState();
    }

    private void configureToolbar() {
        setSupportActionBar(binding.toolbar);
    }

    private void configureRecyclerView() {
        warehouseItemAdapter = new WarehouseItemAdapter();

        binding.warehouseRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.warehouseRecyclerView.setAdapter(
                warehouseItemAdapter
        );

        binding.warehouseRecyclerView.setHasFixedSize(true);
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
    }

    private void observeUiState() {
        AlmacenTrackerApplication application =
                (AlmacenTrackerApplication) getApplication();

        WarehouseItemListViewModelFactory factory =
                application
                        .getAppContainer()
                        .provideWarehouseItemListViewModelFactory();

        WarehouseItemListViewModel viewModel =
                new ViewModelProvider(this, factory)
                        .get(WarehouseItemListViewModel.class);

        viewModel.getUiState().observe(
                this,
                this::render
        );
    }

    private void render(WarehouseItemListUiState state) {
        hideAllStates();

        switch (state.getStatus()) {
            case LOADING:
                binding.loadingProgress.setVisibility(View.VISIBLE);
                break;

            case CONTENT:
                warehouseItemAdapter.submitList(state.getItems());
                binding.warehouseRecyclerView.setVisibility(
                        View.VISIBLE
                );
                break;

            case EMPTY:
                warehouseItemAdapter.submitList(null);
                binding.emptyState.setVisibility(View.VISIBLE);
                break;

            case ERROR:
                warehouseItemAdapter.submitList(null);
                binding.errorText.setText(state.getErrorMessage());
                binding.errorText.setVisibility(View.VISIBLE);
                Log.e(TAG, "Warehouse items could not be loaded");
                break;
        }
    }

    private void hideAllStates() {
        binding.loadingProgress.setVisibility(View.GONE);
        binding.warehouseRecyclerView.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.GONE);
        binding.errorText.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding.warehouseRecyclerView.setAdapter(null);
        binding = null;
    }
}