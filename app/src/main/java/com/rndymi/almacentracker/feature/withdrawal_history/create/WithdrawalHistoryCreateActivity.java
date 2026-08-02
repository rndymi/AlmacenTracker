package com.rndymi.almacentracker.feature.withdrawal_history.create;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.databinding.ActivityWithdrawalHistoryCreateBinding;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraft;
import com.rndymi.almacentracker.feature.inventory.common.SimpleTextWatcher;
import com.rndymi.almacentracker.feature.withdrawal_history.common.WithdrawalHistoryCreateInput;
import com.rndymi.almacentracker.feature.withdrawal_history.common.WithdrawalHistoryCreateIntentContract;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public final class WithdrawalHistoryCreateActivity
        extends AppCompatActivity {

    private ActivityWithdrawalHistoryCreateBinding binding;
    private WithdrawalHistoryCreateViewModel viewModel;
    private WithdrawalHistoryCreateAdapter adapter;
    private boolean rendering;

    public static Intent createIntent(
            Context context,
            List<WithdrawalHistoryCreateInput> entries
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

        return intent;
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
        configureList();
        configureViewModel();
        configureInputs();
        configureActions();
        observeState();

        viewModel.initialize(
                WithdrawalHistoryCreateIntentContract
                        .getEntries(getIntent()),
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
        viewModel =
                new ViewModelProvider(
                        this,
                        new WithdrawalHistoryCreateViewModelFactory()
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
    }

    private void configureActions() {
        binding.continueButton.setOnClickListener(
                ignored ->
                        viewModel.continueToConfirmation(
                                System.currentTimeMillis()
                        )
        );
    }

    private void observeState() {
        viewModel.getUiState().observe(
                this,
                this::render
        );

        viewModel.getContinueEvent().observe(
                this,
                this::consumeContinueEvent
        );
    }

    private void render(
            WithdrawalHistoryCreateUiState state
    ) {
        boolean ready =
                state.getStatus()
                        == WithdrawalHistoryCreateUiState
                        .Status.READY
                        || state.getStatus()
                        == WithdrawalHistoryCreateUiState
                        .Status.INVALID_INPUT;

        binding.contentContainer.setVisibility(
                ready ? View.VISIBLE : View.GONE
        );

        binding.errorText.setVisibility(
                state.getStatus()
                        == WithdrawalHistoryCreateUiState
                        .Status.ERROR
                        ? View.VISIBLE
                        : View.GONE
        );

        if (!ready) {
            return;
        }

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

        binding.continueButton.setEnabled(
                state.canContinue()
        );

        rendering = false;
    }

    private void consumeContinueEvent(
            UiEvent<WithdrawalHistoryDraft> event
    ) {
        WithdrawalHistoryDraft draft =
                event == null
                        ? null
                        : event.getContentIfNotHandled();

        if (draft == null) {
            return;
        }

        Toast.makeText(
                this,
                getString(
                        R.string
                                .withdrawal_history_draft_ready,
                        draft.getEntries().size()
                ),
                Toast.LENGTH_LONG
        ).show();
    }
}
