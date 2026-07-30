package com.rndymi.almacentracker.feature.inventory.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.databinding.DialogDeleteConfirmationBinding;

import java.util.Objects;

public final class DeleteConfirmationDialog {

    private DeleteConfirmationDialog() {
    }

    public static void show(
            Context context,
            Runnable onConfirmed
    ) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(onConfirmed);

        DialogDeleteConfirmationBinding binding =
                DialogDeleteConfirmationBinding.inflate(
                        LayoutInflater.from(context)
                );

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(context)
                        .setTitle(
                                R.string
                                        .delete_verification_title
                        )
                        .setView(binding.getRoot())
                        .setNegativeButton(
                                R.string.cancel_action,
                                null
                        )
                        .setPositiveButton(
                                R.string.delete_action,
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                ignored -> {
                    Button deleteButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    deleteButton.setEnabled(false);
                    deleteButton.setOnClickListener(
                            button -> {
                                onConfirmed.run();
                                dialog.dismiss();
                            }
                    );

                    binding.confirmationEditText
                            .addTextChangedListener(
                                    SimpleTextWatcher
                                            .afterTextChanged(
                                                    value ->
                                                            renderInputState(
                                                                    context,
                                                                    binding,
                                                                    deleteButton,
                                                                    value
                                                            )
                                            )
                            );
                }
        );

        dialog.show();
    }

    private static void renderInputState(
            Context context,
            DialogDeleteConfirmationBinding binding,
            Button deleteButton,
            String value
    ) {
        String requiredKeyword =
                context.getString(
                        R.string
                                .delete_verification_keyword
                );

        boolean valid =
                requiredKeyword.equals(value);

        deleteButton.setEnabled(valid);

        binding.confirmationInputLayout.setError(
                value.isEmpty() || valid
                        ? null
                        : context.getString(
                                R.string
                                        .delete_verification_error
                        )
        );
    }
}
