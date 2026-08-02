package com.rndymi.almacentracker.feature.withdrawal_history.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.app.AlmacenTrackerApplication;
import com.rndymi.almacentracker.databinding.ActivityWithdrawalHistoryListBinding;

public final class WithdrawalHistoryListActivity
        extends AppCompatActivity {

    private ActivityWithdrawalHistoryListBinding
            binding;

    private WithdrawalHistoryListViewModel viewModel;

    private WithdrawalHistoryListAdapter adapter;

    private boolean firstResume = true;

    public static Intent createIntent(Context context) {
        return new Intent(
                context,
                WithdrawalHistoryListActivity.class
        );
    }

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        binding =
                ActivityWithdrawalHistoryListBinding
                        .inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        configureToolbar();
        configureRecyclerView();
        configureViewModel();
        configureActions();
        observeState();

        viewModel.load();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (firstResume) {
            firstResume = false;
            return;
        }

        viewModel.refresh();
    }

    private void configureToolbar() {
        binding.toolbar.setNavigationOnClickListener(
                ignored ->
                        getOnBackPressedDispatcher()
                                .onBackPressed()
        );
    }

    private void configureRecyclerView() {
        adapter =
                new WithdrawalHistoryListAdapter();

        binding.historyRecyclerView.setAdapter(adapter);
        binding.historyRecyclerView.setHasFixedSize(true);
    }

    private void configureViewModel() {
        AlmacenTrackerApplication application =
                (AlmacenTrackerApplication)
                        getApplication();

        WithdrawalHistoryListViewModelFactory factory =
                application
                        .getAppContainer()
                        .provideWithdrawalHistoryListViewModelFactory();

        viewModel =
                new ViewModelProvider(
                        this,
                        factory
                ).get(
                        WithdrawalHistoryListViewModel.class
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
            WithdrawalHistoryListUiState state
    ) {
        boolean hasContent = state.hasContent();
        boolean initialLoading =
                state.isLoading() && !hasContent;
        boolean fullError =
                state.hasError() && !hasContent;

        binding.progressIndicator.setVisibility(
                initialLoading
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.historyRecyclerView.setVisibility(
                hasContent
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.emptyContainer.setVisibility(
                state.isEmpty()
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.errorContainer.setVisibility(
                fullError
                        ? View.VISIBLE
                        : View.GONE
        );

        adapter.submitList(
                state.getSummaries()
        );

        if (state.hasError() && hasContent) {
            Snackbar.make(
                    binding.getRoot(),
                    R.string.withdrawal_history_refresh_error,
                    Snackbar.LENGTH_LONG
            ).setAction(
                    R.string.retry_action,
                    ignored -> viewModel.retry()
            ).show();
        }
    }
}
