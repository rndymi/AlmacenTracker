package com.rndymi.almacentracker.feature.withdrawal_history.create;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.app.AlmacenTrackerApplication;
import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.databinding.ActivityWithdrawalHistoryCreateBinding;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraft;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraftEntry;
import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;
import com.rndymi.almacentracker.feature.inventory.common.SimpleTextWatcher;
import com.rndymi.almacentracker.feature.withdrawal_history.common.WithdrawalHistoryCreateInput;
import com.rndymi.almacentracker.feature.withdrawal_history.common.WithdrawalHistoryCreateIntentContract;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public final class WithdrawalHistoryCreateActivity
        extends AppCompatActivity {

    public static final String EXTRA_SAVED_HISTORY_ID =
            "com.rndymi.almacentracker.extra.SAVED_HISTORY_ID";
    private static final String EXTRA_INITIAL_TITLE =
            "com.rndymi.almacentracker.extra."
                    + "WITHDRAWAL_HISTORY_INITIAL_TITLE";

    private ActivityWithdrawalHistoryCreateBinding binding;
    private WithdrawalHistoryCreateViewModel viewModel;
    private WithdrawalHistoryCreateAdapter adapter;
    private boolean rendering;
    private boolean saveErrorShown;

    public static Intent createIntent(
            Context context,
            List<WithdrawalHistoryCreateInput> entries,
            @Nullable String initialTitle
    ) {
        Intent intent =
                new Intent(
                        context,
                        WithdrawalHistoryCreateActivity.class
                );

        WithdrawalHistoryCreateIntentContract
                .putEntries(
                        intent,
                        entries
                );

        if (initialTitle != null
                && !initialTitle.trim().isEmpty()) {
            intent.putExtra(
                    EXTRA_INITIAL_TITLE,
                    initialTitle.trim()
            );
        }

        return intent;
    }

    public static long getSavedHistoryId(
            @Nullable Intent data
    ) {
        if (data == null) {
            return 0L;
        }

        return data.getLongExtra(
                EXTRA_SAVED_HISTORY_ID,
                0L
        );
    }

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        binding =
                ActivityWithdrawalHistoryCreateBinding
                        .inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        configureToolbar();
        configureBackNavigation();
        configureList();
        configureViewModel();
        configureInputs();
        configureActions();
        observeState();

        viewModel.initialize(
                WithdrawalHistoryCreateIntentContract
                        .getEntries(getIntent()),
                getIntent().getStringExtra(
                        EXTRA_INITIAL_TITLE
                ),
                System.currentTimeMillis()
        );
    }

    private void configureToolbar() {
        binding.toolbar.setNavigationOnClickListener(
                ignored ->
                        getOnBackPressedDispatcher()
                                .onBackPressed()
        );
    }

    private void configureBackNavigation() {
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        WithdrawalHistoryCreateUiState state =
                                viewModel.getUiState()
                                        .getValue();

                        if (state != null
                                && state.isSaving()) {
                            return;
                        }

                        setEnabled(false);

                        getOnBackPressedDispatcher()
                                .onBackPressed();
                    }
                }
        );
    }

    private void configureList() {
        adapter =
                new WithdrawalHistoryCreateAdapter(
                        new WithdrawalHistoryCreateAdapter
                                .Listener() {
                            @Override
                            public void onQuantityChanged(
                                    long stableId,
                                    String value
                            ) {
                                viewModel.onQuantityChanged(
                                        stableId,
                                        value
                                );
                            }

                            @Override
                            public void onUnitChanged(
                                    long stableId,
                                    String value
                            ) {
                                viewModel.onUnitChanged(
                                        stableId,
                                        value
                                );
                            }
                        }
                );

        binding.entriesRecyclerView.setAdapter(
                adapter
        );
    }

    private void configureViewModel() {
        AlmacenTrackerApplication application =
                (AlmacenTrackerApplication)
                        getApplication();

        viewModel =
                new ViewModelProvider(
                        this,
                        application
                                .getAppContainer()
                                .provideWithdrawalHistoryCreateViewModelFactory()
                ).get(
                        WithdrawalHistoryCreateViewModel.class
                );
    }

    private void configureInputs() {
        binding.titleEditText.addTextChangedListener(
                SimpleTextWatcher.afterTextChanged(
                        value -> {
                            if (!rendering) {
                                viewModel.onTitleChanged(
                                        value
                                );
                            }
                        }
                )
        );
        binding.destinationEditText.addTextChangedListener(
                SimpleTextWatcher.afterTextChanged(
                        value -> {
                            if (!rendering) {
                                viewModel.onDestinationChanged(value);
                            }
                        }
                )
        );
    }

    private void configureActions() {
        binding.continueButton.setOnClickListener(
                ignored ->
                        viewModel.requestSaveConfirmation(
                                System.currentTimeMillis()
                        )
        );
    }

    private void observeState() {
        viewModel.getUiState().observe(
                this,
                this::render
        );

        viewModel.getConfirmationEvent().observe(
                this,
                this::consumeConfirmationEvent
        );

        viewModel.getSavedEvent().observe(
                this,
                this::consumeSavedEvent
        );
    }

    private void render(
            WithdrawalHistoryCreateUiState state
    ) {
        if (state == null) {
            return;
        }

        boolean showContent =
                !state.hasInitialInputError()
                        && state.getStatus()
                        != WithdrawalHistoryCreateUiState
                        .Status.INITIALIZING;

        binding.contentContainer.setVisibility(
                showContent
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.errorText.setVisibility(
                state.hasInitialInputError()
                        ? View.VISIBLE
                        : View.GONE
        );

        if (!showContent) {
            return;
        }

        boolean editable =
                state.isEditable();

        rendering = true;

        if (!binding.titleEditText
                .getText()
                .toString()
                .equals(state.getTitle())) {
            binding.titleEditText.setText(
                    state.getTitle()
            );
        }

        binding.titleInputLayout.setError(
                state.getTitleError()
        );

        if (!binding.destinationEditText
                .getText().toString()
                .equals(state.getDestination())) {
            binding.destinationEditText.setText(
                    state.getDestination()
            );
        }

        binding.dateText.setText(
                DateFormat
                        .getDateTimeInstance(
                                DateFormat.MEDIUM,
                                DateFormat.SHORT
                        )
                        .format(
                                new Date(
                                        state.getRegisteredAt()
                                )
                        )
        );

        binding.dateErrorText.setVisibility(
                state.getDateError() == null
                        ? View.GONE
                        : View.VISIBLE
        );

        binding.dateErrorText.setText(
                state.getDateError()
        );

        binding.referenceCountText.setText(
                getResources().getQuantityString(
                        R.plurals
                                .withdrawal_history_reference_count,
                        state.getEntries().size(),
                        state.getEntries().size()
                )
        );

        adapter.submitList(
                state.getEntries()
        );

        binding.titleEditText.setEnabled(
                editable
        );
        binding.destinationEditText.setEnabled(editable);

        binding.entriesRecyclerView.setEnabled(
                editable
        );

        binding.continueButton.setEnabled(
                state.canRequestSave()
        );

        binding.continueButton.setText(
                state.isSaving()
                        ? R.string.withdrawal_history_saving
                        : R.string.withdrawal_history_save_action
        );

        rendering = false;

        if (state.hasSaveError()
                && !saveErrorShown) {
            saveErrorShown = true;

            Toast.makeText(
                    this,
                    state.getSaveError(),
                    Toast.LENGTH_LONG
            ).show();
        }

        if (!state.hasSaveError()) {
            saveErrorShown = false;
        }
    }

    private void consumeConfirmationEvent(
            UiEvent<WithdrawalHistoryDraft> event
    ) {
        WithdrawalHistoryDraft draft =
                event == null
                        ? null
                        : event.getContentIfNotHandled();

        if (draft == null
                || isFinishing()
                || isDestroyed()) {
            return;
        }

        int foundCount = 0;

        for (
                WithdrawalHistoryDraftEntry entry
                : draft.getEntries()
        ) {
            if (entry.getLocationStatus()
                    == WithdrawalLocationStatus.FOUND) {
                foundCount++;
            }
        }

        int notFoundCount =
                draft.getEntries().size()
                        - foundCount;

        String message =
                getString(
                        R.string
                                .withdrawal_history_save_confirmation_message,
                        draft.getEntries().size(),
                        foundCount,
                        notFoundCount
                );

        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        R.string
                                .withdrawal_history_save_confirmation_title
                )
                .setMessage(message)
                .setNegativeButton(
                        android.R.string.cancel,
                        null
                )
                .setPositiveButton(
                        R.string.withdrawal_history_save_action,
                        (dialog, which) ->
                                viewModel.confirmSave(
                                        System.currentTimeMillis()
                                )
                )
                .show();
    }

    private void consumeSavedEvent(
            UiEvent<Long> event
    ) {
        Long generatedId =
                event == null
                        ? null
                        : event.getContentIfNotHandled();

        if (generatedId == null
                || generatedId <= 0L) {
            return;
        }

        Toast.makeText(
                this,
                R.string.withdrawal_history_saved,
                Toast.LENGTH_SHORT
        ).show();

        Intent result =
                new Intent()
                        .putExtra(
                                EXTRA_SAVED_HISTORY_ID,
                                generatedId
                        );

        setResult(
                RESULT_OK,
                result
        );

        finish();
    }
}
