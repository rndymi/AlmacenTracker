package com.rndymi.almacentracker.feature.reference_list.review;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog
        .MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.app
        .AlmacenTrackerApplication;
import com.rndymi.almacentracker.databinding
        .ActivityReferenceListReviewBinding;
import com.rndymi.almacentracker.databinding
        .DialogReferenceEditorBinding;
import com.rndymi.almacentracker.domain.reference
        .WarehouseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReferenceListReviewActivity
        extends AppCompatActivity {

    private static final String
            EXTRA_RECOGNIZED_LINES =
            "com.rndymi.almacentracker.extra."
                    + "RECOGNIZED_LINES";

    private static final String
            EXTRA_CONFIRMED_REFERENCES =
            "com.rndymi.almacentracker.extra."
                    + "CONFIRMED_REFERENCES";

    private static final String
            CONTRACT_SEPARATOR =
            "\u001F";

    private ActivityReferenceListReviewBinding
            binding;

    private ReferenceListReviewViewModel
            viewModel;

    private ReferenceListReviewAdapter
            adapter;

    public static Intent createIntent(
            Context context,
            List<String> recognizedLines
    ) {
        Intent intent =
                new Intent(
                        context,
                        ReferenceListReviewActivity.class
                );

        ArrayList<String> copiedLines =
                recognizedLines == null
                        ? new ArrayList<>()
                        : new ArrayList<>(
                        recognizedLines
                );

        intent.putStringArrayListExtra(
                EXTRA_RECOGNIZED_LINES,
                copiedLines
        );

        return intent;
    }

    public static List<WarehouseReference>
    getConfirmedReferences(
            @Nullable Intent data
    ) {
        if (data == null) {
            return Collections.emptyList();
        }

        ArrayList<String> encodedReferences =
                data.getStringArrayListExtra(
                        EXTRA_CONFIRMED_REFERENCES
                );

        if (encodedReferences == null) {
            return Collections.emptyList();
        }

        List<WarehouseReference> references =
                new ArrayList<>();

        for (
                String encodedReference
                : encodedReferences
        ) {
            if (encodedReference == null) {
                continue;
            }

            String[] parts =
                    encodedReference.split(
                            CONTRACT_SEPARATOR,
                            -1
                    );

            if (parts.length != 2
                    || parts[0].isEmpty()
                    || parts[1].isEmpty()) {
                continue;
            }

            references.add(
                    new WarehouseReference(
                            parts[0],
                            parts[1]
                    )
            );
        }

        return Collections.unmodifiableList(
                references
        );
    }

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        binding =
                ActivityReferenceListReviewBinding
                        .inflate(
                                getLayoutInflater()
                        );

        setContentView(
                binding.getRoot()
        );

        configureViewModel();
        configureList();
        configureActions();
        observeState();

        viewModel.applyInitialLines(
                getIntent()
                        .getStringArrayListExtra(
                                EXTRA_RECOGNIZED_LINES
                        )
        );
    }

    private void configureViewModel() {
        AlmacenTrackerApplication application =
                (AlmacenTrackerApplication)
                        getApplication();

        ReferenceListReviewViewModelFactory factory =
                application
                        .getAppContainer()
                        .provideReferenceListReviewViewModelFactory();

        viewModel =
                new ViewModelProvider(
                        this,
                        factory
                ).get(
                        ReferenceListReviewViewModel.class
                );
    }

    private void configureList() {
        adapter =
                new ReferenceListReviewAdapter(
                        new ReferenceListReviewAdapter
                                .Listener() {

                            @Override
                            public void onEdit(
                                    ReferenceProposal proposal
                            ) {
                                showReferenceEditor(
                                        proposal
                                );
                            }

                            @Override
                            public void onDelete(
                                    ReferenceProposal proposal
                            ) {
                                viewModel.deleteReference(
                                        proposal.getId()
                                );

                                Snackbar.make(
                                        binding.getRoot(),
                                        R.string
                                                .reference_list_review_deleted,
                                        Snackbar.LENGTH_SHORT
                                ).show();
                            }
                        }
                );

        binding.referencesRecyclerView
                .setAdapter(adapter);
    }

    private void configureActions() {
        binding.toolbar
                .setNavigationOnClickListener(
                        ignored -> finish()
                );

        binding.addReferenceButton
                .setOnClickListener(
                        ignored ->
                                showReferenceEditor(
                                        null
                                )
                );

        binding.confirmButton
                .setOnClickListener(
                        ignored ->
                                viewModel.confirm()
                );
    }

    private void observeState() {
        viewModel.getUiState().observe(
                this,
                this::render
        );

        viewModel
                .getDuplicateConsolidatedEvent()
                .observe(
                        this,
                        event -> {
                            Integer duplicateCount =
                                    event
                                            .getContentIfNotHandled();

                            if (duplicateCount == null) {
                                return;
                            }

                            Snackbar.make(
                                    binding.getRoot(),
                                    getResources()
                                            .getQuantityString(
                                                    R.plurals
                                                            .reference_list_review_duplicates,
                                                    duplicateCount,
                                                    duplicateCount
                                            ),
                                    Snackbar.LENGTH_LONG
                            ).show();
                        }
                );

        viewModel
                .getConfirmationEvent()
                .observe(
                        this,
                        event -> {
                            List<WarehouseReference>
                                    references =
                                    event
                                            .getContentIfNotHandled();

                            if (references == null) {
                                return;
                            }

                            finishWithResult(
                                    references
                            );
                        }
                );
    }

    private void render(
            ReferenceListReviewUiState state
    ) {
        adapter.submitList(
                state.getProposals()
        );

        binding.emptyContainer.setVisibility(
                state.isEmpty()
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.referencesRecyclerView
                .setVisibility(
                        state.isEmpty()
                                ? View.GONE
                                : View.VISIBLE
                );

        binding.confirmButton.setEnabled(
                state.canConfirm()
        );

        binding.addReferenceButton.setEnabled(
                !state.isConfirming()
        );

        binding.countText.setText(
                getResources()
                        .getQuantityString(
                                R.plurals
                                        .reference_list_review_count,
                                state.getReferenceCount(),
                                state.getReferenceCount()
                        )
        );
    }

    private void showReferenceEditor(
            @Nullable ReferenceProposal proposal
    ) {
        DialogReferenceEditorBinding
                dialogBinding =
                DialogReferenceEditorBinding
                        .inflate(
                                LayoutInflater.from(
                                        this
                                )
                        );

        if (proposal != null) {
            dialogBinding.categoryEditText
                    .setText(
                            proposal
                                    .getReference()
                                    .getCategory()
                    );

            dialogBinding.codeEditText
                    .setText(
                            proposal
                                    .getReference()
                                    .getCode()
                    );
        }

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(
                        this
                )
                        .setTitle(
                                proposal == null
                                        ? R.string
                                          .reference_list_review_add_title
                                        : R.string
                                          .reference_list_review_edit_title
                        )
                        .setView(
                                dialogBinding.getRoot()
                        )
                        .setNegativeButton(
                                R.string.cancel_action,
                                null
                        )
                        .setPositiveButton(
                                R.string.save_action,
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                ignored ->
                        dialog.getButton(
                                AlertDialog
                                        .BUTTON_POSITIVE
                        ).setOnClickListener(
                                button ->
                                        saveEditorValues(
                                                dialog,
                                                dialogBinding,
                                                proposal
                                        )
                        )
        );

        dialog.show();
    }

    private void saveEditorValues(
            AlertDialog dialog,
            DialogReferenceEditorBinding
                    dialogBinding,
            @Nullable ReferenceProposal proposal
    ) {
        String category =
                textOf(
                        dialogBinding
                                .categoryEditText
                                .getText()
                );

        String code =
                textOf(
                        dialogBinding
                                .codeEditText
                                .getText()
                );

        ReferenceInputResult result;

        if (proposal == null) {
            result =
                    viewModel.addReference(
                            category,
                            code
                    );
        } else {
            result =
                    viewModel.editReference(
                            proposal.getId(),
                            category,
                            code
                    );
        }

        if (result.getStatus()
                == ReferenceInputResult
                .Status.SUCCESS) {
            dialog.dismiss();
            return;
        }

        if (result.getStatus()
                == ReferenceInputResult
                .Status.DUPLICATE) {
            Snackbar.make(
                    binding.getRoot(),
                    R.string
                            .reference_list_review_duplicate,
                    Snackbar.LENGTH_LONG
            ).show();

            return;
        }

        if (result.getStatus()
                == ReferenceInputResult
                .Status.INVALID) {
            dialogBinding
                    .categoryInputLayout
                    .setError(
                            result.isCategoryValid()
                                    ? null
                                    : getString(
                                    R.string
                                    .reference_list_review_category_error
                            )
                    );

            dialogBinding
                    .codeInputLayout
                    .setError(
                            result.isCodeValid()
                                    ? null
                                    : getString(
                                    R.string
                                    .reference_list_review_code_error
                            )
                    );
        }
    }

    private String textOf(
            @Nullable CharSequence value
    ) {
        return value == null
                ? ""
                : value.toString();
    }

    private void finishWithResult(
            List<WarehouseReference> references
    ) {
        ArrayList<String> encodedReferences =
                new ArrayList<>();

        for (
                WarehouseReference reference
                : references
        ) {
            encodedReferences.add(
                    reference.getCategory()
                            + CONTRACT_SEPARATOR
                            + reference.getCode()
            );
        }

        Intent result =
                new Intent()
                        .putStringArrayListExtra(
                                EXTRA_CONFIRMED_REFERENCES,
                                encodedReferences
                        );

        setResult(
                RESULT_OK,
                result
        );

        finish();
    }
}