package com.rndymi.almacentracker.feature.withdrawal_history.detail;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.app.AlmacenTrackerApplication;
import com.rndymi.almacentracker.databinding.ActivityWithdrawalHistoryDetailBinding;
import com.rndymi.almacentracker.domain.history.WithdrawalHistory;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;
import com.rndymi.almacentracker.feature.withdrawal_history.common.WithdrawalHistoryDetailIntentContract;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public final class WithdrawalHistoryDetailActivity
        extends AppCompatActivity {

    private ActivityWithdrawalHistoryDetailBinding
            binding;

    private WithdrawalHistoryDetailViewModel
            viewModel;

    private WithdrawalHistoryDetailAdapter
            adapter;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        binding =
                ActivityWithdrawalHistoryDetailBinding
                        .inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        configureToolbar();
        configureRecyclerView();
        configureViewModel();
        configureActions();
        observeState();

        long historyId =
                WithdrawalHistoryDetailIntentContract
                        .readHistoryId(getIntent());

        viewModel.load(historyId);
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
                new WithdrawalHistoryDetailAdapter();

        binding.historyEntriesRecyclerView
                .setAdapter(adapter);

        binding.historyEntriesRecyclerView
                .setHasFixedSize(true);
    }

    private void configureViewModel() {
        AlmacenTrackerApplication application =
                (AlmacenTrackerApplication)
                        getApplication();

        WithdrawalHistoryDetailViewModelFactory factory =
                application
                        .getAppContainer()
                        .provideWithdrawalHistoryDetailViewModelFactory();

        viewModel =
                new ViewModelProvider(
                        this,
                        factory
                ).get(
                        WithdrawalHistoryDetailViewModel.class
                );
    }

    private void configureActions() {
        binding.retryButton.setOnClickListener(
                ignored -> viewModel.retry()
        );

        binding.notFoundBackButton.setOnClickListener(
                ignored ->
                        getOnBackPressedDispatcher()
                                .onBackPressed()
        );

        binding.errorBackButton.setOnClickListener(
                ignored ->
                        getOnBackPressedDispatcher()
                                .onBackPressed()
        );
    }

    private void observeState() {
        viewModel.getUiState().observe(
                this,
                this::render
        );
    }

    private void render(
            WithdrawalHistoryDetailUiState state
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

        binding.contentContainer.setVisibility(
                hasContent
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.notFoundContainer.setVisibility(
                state.isNotFound()
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.errorContainer.setVisibility(
                fullError
                        ? View.VISIBLE
                        : View.GONE
        );

        if (hasContent) {
            renderRecord(state.getRecord());
        } else {
            adapter.submitList(null);
        }

        if (state.hasError() && hasContent) {
            Snackbar.make(
                    binding.getRoot(),
                    R.string
                            .withdrawal_history_detail_refresh_error,
                    Snackbar.LENGTH_LONG
            ).setAction(
                    R.string.retry_action,
                    ignored -> viewModel.retry()
            ).show();
        }
    }

    private void renderRecord(
            WithdrawalHistoryRecord record
    ) {
        WithdrawalHistory history =
                record.getHistory();

        String title =
                history.hasTitle()
                        ? history.getTitle()
                        : getString(
                        R.string
                        .withdrawal_history_untitled
                );

        binding.historyTitleText.setText(title);

        binding.historyDateText.setText(
                formatDate(
                        history.getRegisteredAt()
                )
        );

        int entryCount =
                record.getEntries().size();

        binding.historyReferenceCountText.setText(
                getResources().getQuantityString(
                        R.plurals
                                .withdrawal_history_reference_count,
                        entryCount,
                        entryCount
                )
        );

        adapter.submitList(
                record.getEntries()
        );
    }

    private String formatDate(long timestamp) {
        Locale locale =
                getResources()
                        .getConfiguration()
                        .getLocales()
                        .get(0);

        DateTimeFormatter formatter =
                DateTimeFormatter
                        .ofLocalizedDateTime(
                                FormatStyle.MEDIUM,
                                FormatStyle.SHORT
                        )
                        .withLocale(locale)
                        .withZone(
                                ZoneId.systemDefault()
                        );

        return formatter.format(
                Instant.ofEpochMilli(timestamp)
        );
    }
}
