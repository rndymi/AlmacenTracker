package com.rndymi.almacentracker.feature.withdrawal_history.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.app.AlmacenTrackerApplication;
import com.rndymi.almacentracker.databinding.ActivityWithdrawalHistoryListBinding;
import com.rndymi.almacentracker.feature.withdrawal_history.common.WithdrawalHistoryDetailIntentContract;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public final class WithdrawalHistoryListActivity
        extends AppCompatActivity {

    private ActivityWithdrawalHistoryListBinding
            binding;

    private WithdrawalHistoryListViewModel viewModel;

    private WithdrawalHistoryListAdapter adapter;

    private boolean firstResume = true;
    private boolean renderingState;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofLocalizedDate(
                    FormatStyle.MEDIUM
            );

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
                new WithdrawalHistoryListAdapter(
                        this::openHistoryDetail
                );

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
        binding.searchEditText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence text,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence text,
                            int start,
                            int before,
                            int count
                    ) {
                    }

                    @Override
                    public void afterTextChanged(
                            Editable editable
                    ) {
                        if (renderingState) {
                            return;
                        }

                        String value =
                                editable == null
                                        ? ""
                                        : editable.toString();

                        viewModel.updateQuery(value);
                    }
                }
        );

        binding.searchEditText.setOnEditorActionListener(
                (textView, actionId, event) -> {
                    if (actionId
                            != EditorInfo.IME_ACTION_SEARCH) {
                        return false;
                    }

                    executeSearch();
                    return true;
                }
        );

        binding.searchButton.setOnClickListener(
                ignored -> executeSearch()
        );

        binding.fromDateButton.setOnClickListener(
                ignored -> openFromDatePicker()
        );

        binding.toDateButton.setOnClickListener(
                ignored -> openToDatePicker()
        );

        binding.clearFromDateButton.setOnClickListener(
                ignored -> viewModel.clearFromDate()
        );

        binding.clearToDateButton.setOnClickListener(
                ignored -> viewModel.clearToDate()
        );

        binding.clearFiltersButton.setOnClickListener(
                ignored -> viewModel.clearCriteria()
        );

        binding.clearNoResultsButton.setOnClickListener(
                ignored -> viewModel.clearCriteria()
        );

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
        renderingState = true;

        try {
            renderCriteria(state);
            renderContent(state);
        } finally {
            renderingState = false;
        }
    }

    private void renderCriteria(
            WithdrawalHistoryListUiState state
    ) {
        String currentText =
                binding.searchEditText
                        .getText() == null
                        ? ""
                        : binding.searchEditText
                        .getText()
                        .toString();

        if (!currentText.equals(state.getQuery())) {
            binding.searchEditText.setText(
                    state.getQuery()
            );

            binding.searchEditText.setSelection(
                    state.getQuery().length()
            );
        }

        LocalDate fromDate = state.getFromDate();
        LocalDate toDate = state.getToDate();

        binding.fromDateButton.setText(
                fromDate == null
                        ? getString(
                        R.string
                        .withdrawal_history_from_any_date
                )
                        : getString(
                        R.string
                        .withdrawal_history_from_date,
                        dateFormatter.format(fromDate)
                )
        );

        binding.toDateButton.setText(
                toDate == null
                        ? getString(
                        R.string
                        .withdrawal_history_to_any_date
                )
                        : getString(
                        R.string
                        .withdrawal_history_to_date,
                        dateFormatter.format(toDate)
                )
        );

        binding.clearFromDateButton.setVisibility(
                fromDate == null
                        ? View.GONE
                        : View.VISIBLE
        );

        binding.clearToDateButton.setVisibility(
                toDate == null
                        ? View.GONE
                        : View.VISIBLE
        );

        binding.clearFiltersButton.setVisibility(
                state.hasActiveCriteria()
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.filterErrorText.setVisibility(
                state.hasInvalidDateInterval()
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void renderContent(
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

        binding.noResultsContainer.setVisibility(
                state.hasNoResults()
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
                    R.string.withdrawal_history_search_error,
                    Snackbar.LENGTH_LONG
            ).setAction(
                    R.string.retry_action,
                    ignored -> viewModel.retry()
            ).show();
        }
    }

    private void executeSearch() {
        hideKeyboard();

        String query =
                binding.searchEditText
                        .getText() == null
                        ? ""
                        : binding.searchEditText
                        .getText()
                        .toString();

        viewModel.updateQuery(query);
        viewModel.search();
    }

    private void openFromDatePicker() {
        WithdrawalHistoryListUiState state =
                viewModel.getUiState().getValue();

        LocalDate selected =
                state == null
                        ? null
                        : state.getFromDate();

        MaterialDatePicker.Builder<Long> builder =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText(
                                R.string
                                        .withdrawal_history_select_from_date
                        );

        if (selected != null) {
            builder.setSelection(
                    toPickerMillis(selected)
            );
        }

        MaterialDatePicker<Long> picker =
                builder.build();

        picker.addOnPositiveButtonClickListener(
                selection -> {
                    if (selection == null) {
                        return;
                    }

                    viewModel.updateFromDate(
                            fromPickerMillis(selection)
                    );
                }
        );

        picker.show(
                getSupportFragmentManager(),
                "withdrawal-history-from-date"
        );
    }

    private void openToDatePicker() {
        WithdrawalHistoryListUiState state =
                viewModel.getUiState().getValue();

        LocalDate selected =
                state == null
                        ? null
                        : state.getToDate();

        MaterialDatePicker.Builder<Long> builder =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText(
                                R.string
                                        .withdrawal_history_select_to_date
                        );

        if (selected != null) {
            builder.setSelection(
                    toPickerMillis(selected)
            );
        }

        MaterialDatePicker<Long> picker =
                builder.build();

        picker.addOnPositiveButtonClickListener(
                selection -> {
                    if (selection == null) {
                        return;
                    }

                    viewModel.updateToDate(
                            fromPickerMillis(selection)
                    );
                }
        );

        picker.show(
                getSupportFragmentManager(),
                "withdrawal-history-to-date"
        );
    }

    private static long toPickerMillis(
            LocalDate date
    ) {
        return date
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli();
    }

    private static LocalDate fromPickerMillis(
            long millis
    ) {
        return Instant
                .ofEpochMilli(millis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
    }

    private void hideKeyboard() {
        View focusedView = getCurrentFocus();

        if (focusedView == null) {
            return;
        }

        InputMethodManager manager =
                (InputMethodManager)
                        getSystemService(
                                INPUT_METHOD_SERVICE
                        );

        if (manager != null) {
            manager.hideSoftInputFromWindow(
                    focusedView.getWindowToken(),
                    0
            );
        }
    }

    private void openHistoryDetail(long historyId) {
        startActivity(
                WithdrawalHistoryDetailIntentContract
                        .createIntent(
                                this,
                                historyId
                        )
        );
    }
}
