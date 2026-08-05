package com.rndymi.almacentracker.feature.withdrawal_history.detail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.app.AlmacenTrackerApplication;
import com.rndymi.almacentracker.core.common.event.UiEvent;
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

    private MenuItem deleteMenuItem;

    private WithdrawalHistoryDetailUiState
            currentState;

    private final OnBackPressedCallback
            backPressedCallback =
            new OnBackPressedCallback(true) {

                @Override
                public void handleOnBackPressed() {
                    if (currentState != null
                            && currentState.isDeleting()) {
                        return;
                    }

                    setEnabled(false);

                    getOnBackPressedDispatcher()
                            .onBackPressed();

                    setEnabled(true);
                }
            };

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        binding =
                ActivityWithdrawalHistoryDetailBinding
                        .inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        backPressedCallback
                );

        configureToolbar();
        configureRecyclerView();
        configureViewModel();
        configureActions();
        observeState();
        observeDeleteSuccess();

        long historyId =
                WithdrawalHistoryDetailIntentContract
                        .readHistoryId(getIntent());

        viewModel.load(historyId);
    }

    private void configureToolbar() {
        binding.toolbar.inflateMenu(
                R.menu.menu_withdrawal_history_detail
        );

        deleteMenuItem =
                binding.toolbar.getMenu()
                        .findItem(
                                R.id.actionDeleteHistory
                        );

        binding.toolbar.setNavigationOnClickListener(
                ignored ->
                        getOnBackPressedDispatcher()
                                .onBackPressed()
        );

        binding.toolbar.setOnMenuItemClickListener(
                item -> {
                    if (item.getItemId()
                            != R.id.actionDeleteHistory) {
                        return false;
                    }

                    showDeleteConfirmation();
                    return true;
                }
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
                ignored -> viewModel.retryLoad()
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

    private void observeDeleteSuccess() {
        viewModel.getDeleteSuccessEvent().observe(
                this,
                this::consumeDeleteSuccess
        );
    }

    private void consumeDeleteSuccess(
            UiEvent<Long> event
    ) {
        if (event == null) {
            return;
        }

        Long deletedHistoryId =
                event.getContentIfNotHandled();

        if (deletedHistoryId == null
                || deletedHistoryId <= 0L) {
            return;
        }

        Intent result =
                WithdrawalHistoryDetailIntentContract
                        .createDeleteResult(
                                deletedHistoryId
                        );

        setResult(
                Activity.RESULT_OK,
                result
        );

        finish();
    }

    private void render(
            WithdrawalHistoryDetailUiState state
    ) {
        currentState = state;

        boolean hasContent = state.hasContent();

        boolean initialLoading =
                state.isLoading() && !hasContent;

        boolean fullLoadError =
                state.hasLoadError() && !hasContent;

        binding.progressIndicator.setVisibility(
                initialLoading || state.isDeleting()
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.progressIndicator
                .setContentDescription(
                        getString(
                                state.isDeleting()
                                        ? R.string
                                          .withdrawal_history_deleting
                                        : R.string
                                          .withdrawal_history_detail_loading
                        )
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
                fullLoadError
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.toolbar.setNavigationIcon(
                state.isDeleting()
                        ? null
                        : getDrawable(
                        R.drawable.ic_arrow_back
                )
        );

        if (deleteMenuItem != null) {
            deleteMenuItem.setVisible(
                    hasContent
            );

            deleteMenuItem.setEnabled(
                    state.canDelete()
            );
        }

        if (hasContent) {
            renderRecord(state.getRecord());
        } else {
            adapter.submitList(null);
        }

        if (state.hasLoadError() && hasContent) {
            Snackbar.make(
                    binding.getRoot(),
                    R.string
                            .withdrawal_history_detail_refresh_error,
                    Snackbar.LENGTH_LONG
            ).setAction(
                    R.string.retry_action,
                    ignored -> viewModel.retryLoad()
            ).show();
        }

        if (state.hasDeleteError()) {
            Snackbar.make(
                    binding.getRoot(),
                    R.string
                            .withdrawal_history_delete_error,
                    Snackbar.LENGTH_LONG
            ).setAction(
                    R.string.retry_action,
                    ignored -> viewModel.retryDelete()
            ).show();
        }
    }

    private void showDeleteConfirmation() {
        WithdrawalHistoryDetailUiState state =
                currentState;

        if (state == null
                || !state.canDelete()
                || state.getRecord() == null) {
            return;
        }

        WithdrawalHistory history =
                state.getRecord()
                        .getHistory();

        String dialogTitle =
                history.hasTitle()
                        ? getString(
                        R.string
                        .withdrawal_history_delete_named_title,
                        history.getTitle()
                )
                        : getString(
                        R.string
                        .withdrawal_history_delete_title
                );

        new MaterialAlertDialogBuilder(this)
                .setTitle(dialogTitle)
                .setMessage(
                        R.string
                                .withdrawal_history_delete_message
                )
                .setNegativeButton(
                        R.string.cancel_action,
                        null
                )
                .setPositiveButton(
                        R.string.delete_action,
                        (dialog, which) ->
                                viewModel.deleteHistory()
                )
                .show();
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

        binding.historyDestinationText.setVisibility(
                history.hasDestination()
                        ? View.VISIBLE
                        : View.GONE
        );
        if (history.hasDestination()) {
            binding.historyDestinationText.setText(
                    getString(
                            R.string.withdrawal_history_destination,
                            history.getDestination()
                    )
            );
        }

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
