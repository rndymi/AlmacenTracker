package com.rndymi.almacentracker.feature.reference_list.location;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.app.AlmacenTrackerApplication;
import com.rndymi.almacentracker.databinding.ActivityReferenceListLocationBinding;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceLocation;
import com.rndymi.almacentracker.feature.inventory.detail.ItemDetailActivity;
import com.rndymi.almacentracker.feature.reference_list.common.WarehouseReferenceIntentContract;

import java.util.List;

public final class ReferenceListLocationActivity
        extends AppCompatActivity {

    private ActivityReferenceListLocationBinding
            binding;

    private ReferenceListLocationViewModel
            viewModel;

    private ReferenceListLocationAdapter adapter;

    public static Intent createIntent(
            Context context,
            List<WarehouseReference> references
    ) {
        Intent intent =
                new Intent(
                        context,
                        ReferenceListLocationActivity.class
                );

        WarehouseReferenceIntentContract
                .putReferences(
                        intent,
                        references
                );

        return intent;
    }

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        binding =
                ActivityReferenceListLocationBinding
                        .inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        configureToolbar();
        configureList();
        configureViewModel();
        configureActions();
        observeState();

        viewModel.applyInitialReferences(
                WarehouseReferenceIntentContract
                        .getReferences(getIntent())
        );
    }

    private void configureToolbar() {
        binding.toolbar.setNavigationOnClickListener(
                ignored ->
                        getOnBackPressedDispatcher()
                                .onBackPressed()
        );
    }

    private void configureList() {
        adapter =
                new ReferenceListLocationAdapter(
                        this::openDetail
                );

        binding.locationsRecyclerView
                .setAdapter(adapter);
    }

    private void configureViewModel() {
        AlmacenTrackerApplication application =
                (AlmacenTrackerApplication)
                        getApplication();

        ReferenceListLocationViewModelFactory factory =
                application
                        .getAppContainer()
                        .provideReferenceListLocationViewModelFactory();

        viewModel =
                new ViewModelProvider(
                        this,
                        factory
                ).get(
                        ReferenceListLocationViewModel.class
                );
    }

    private void configureActions() {
        binding.retryButton.setOnClickListener(
                ignored -> viewModel.retry()
        );
    }

    private void observeState() {
        viewModel.getUiState().observe(
                this,
                this::render
        );
    }

    private void render(
            ReferenceListLocationUiState state
    ) {
        boolean loading = state.isLoading();
        boolean content = state.hasContent();

        binding.progressIndicator.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.summaryText.setVisibility(
                content
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.locationsRecyclerView.setVisibility(
                content
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.messageContainer.setVisibility(
                state.getStatus()
                        == ReferenceListLocationUiState
                        .Status.INVALID_INPUT
                        || state.getStatus()
                        == ReferenceListLocationUiState
                        .Status.ERROR
                        ? View.VISIBLE
                        : View.GONE
        );

        if (content) {
            adapter.submitList(
                    state.getLocations()
            );

            binding.summaryText.setText(
                    getString(
                            R.string
                                    .reference_list_location_summary,
                            state.getTotalCount(),
                            state.getFoundCount(),
                            state.getNotFoundCount()
                    )
            );

            return;
        }

        adapter.submitList(null);

        if (state.getStatus()
                == ReferenceListLocationUiState
                .Status.INVALID_INPUT) {
            binding.messageText.setText(
                    R.string
                            .reference_list_location_invalid_input
            );

            binding.retryButton.setVisibility(
                    View.GONE
            );

            return;
        }

        if (state.getStatus()
                == ReferenceListLocationUiState
                .Status.ERROR) {
            binding.messageText.setText(
                    R.string
                            .reference_list_location_error
            );

            binding.retryButton.setVisibility(
                    View.VISIBLE
            );
        }
    }

    private void openDetail(
            WarehouseReferenceLocation location
    ) {
        if (!location.isFound()
                || location.getWarehouseItemId() <= 0L) {
            return;
        }

        startActivity(
                ItemDetailActivity.createIntent(
                        this,
                        location.getWarehouseItemId()
                )
        );
    }
}