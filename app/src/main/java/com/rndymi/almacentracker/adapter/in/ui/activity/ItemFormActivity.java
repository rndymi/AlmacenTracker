package com.rndymi.almacentracker.adapter.in.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.AlmacenTrackerApplication;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemFormUiState;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemFormViewModel;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemFormViewModelFactory;
import com.rndymi.almacentracker.databinding.ActivityItemFormBinding;

public final class ItemFormActivity extends AppCompatActivity {

    private ActivityItemFormBinding binding;
    private WarehouseItemFormViewModel viewModel;
    private boolean rendering;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        binding = ActivityItemFormBinding.inflate(
                getLayoutInflater()
        );

        setContentView(binding.getRoot());

        configureToolbar();
        configureViewModel();
        configureInputListeners();
        configureActions();
        observeState();
    }

    private void configureToolbar() {
        setSupportActionBar(binding.toolbar);

        binding.toolbar.setNavigationOnClickListener(
                ignored -> finish()
        );
    }

    private void configureViewModel() {
        AlmacenTrackerApplication application =
                (AlmacenTrackerApplication)
                        getApplication();

        WarehouseItemFormViewModelFactory factory =
                application
                        .getAppContainer()
                        .provideWarehouseItemFormViewModelFactory();

        viewModel = new ViewModelProvider(
                this,
                factory
        ).get(WarehouseItemFormViewModel.class);
    }

    private void configureInputListeners() {
        binding.categoryEditText.addTextChangedListener(
                SimpleTextWatcher.afterTextChanged(
                        value -> {
                            if (!rendering) {
                                viewModel.onCategoryChanged(value);
                            }
                        }
                )
        );

        binding.codeEditText.addTextChangedListener(
                SimpleTextWatcher.afterTextChanged(
                        value -> {
                            if (!rendering) {
                                viewModel.onCodeChanged(value);
                            }
                        }
                )
        );

        binding.siteEditText.addTextChangedListener(
                SimpleTextWatcher.afterTextChanged(
                        value -> {
                            if (!rendering) {
                                viewModel.onSiteChanged(value);
                            }
                        }
                )
        );

        binding.positionEditText.addTextChangedListener(
                SimpleTextWatcher.afterTextChanged(
                        value -> {
                            if (!rendering) {
                                viewModel.onPositionChanged(value);
                            }
                        }
                )
        );

        binding.observationsEditText.addTextChangedListener(
                SimpleTextWatcher.afterTextChanged(
                        value -> {
                            if (!rendering) {
                                viewModel.onObservationsChanged(
                                        value
                                );
                            }
                        }
                )
        );
    }

    private void configureActions() {
        binding.cancelButton.setOnClickListener(
                ignored -> finish()
        );

        binding.saveButton.setOnClickListener(
                ignored -> viewModel.save()
        );
    }

    private void observeState() {
        viewModel.getUiState().observe(
                this,
                this::render
        );

        viewModel.getCreationSuccess().observe(
                this,
                event -> {
                    if (event == null) {
                        return;
                    }

                    Long createdItemId =
                            event.getContentIfNotHandled();

                    if (createdItemId == null) {
                        return;
                    }

                    Toast.makeText(
                            this,
                            R.string.warehouse_item_created,
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                }
        );
    }

    private void render(
            WarehouseItemFormUiState state
    ) {
        rendering = true;

        setTextIfDifferent(
                binding.categoryEditText.getText() != null
                        ? binding.categoryEditText
                          .getText()
                          .toString()
                        : "",
                state.getCategory(),
                value -> binding.categoryEditText
                        .setText(value)
        );

        setTextIfDifferent(
                binding.codeEditText.getText() != null
                        ? binding.codeEditText
                          .getText()
                          .toString()
                        : "",
                state.getCode(),
                value -> binding.codeEditText
                        .setText(value)
        );

        setTextIfDifferent(
                binding.siteEditText.getText() != null
                        ? binding.siteEditText
                          .getText()
                          .toString()
                        : "",
                state.getSite(),
                value -> binding.siteEditText
                        .setText(value)
        );

        setTextIfDifferent(
                binding.positionEditText.getText() != null
                        ? binding.positionEditText
                          .getText()
                          .toString()
                        : "",
                state.getPosition(),
                value -> binding.positionEditText
                        .setText(value)
        );

        setTextIfDifferent(
                binding.observationsEditText.getText() != null
                        ? binding.observationsEditText
                          .getText()
                          .toString()
                        : "",
                state.getObservations(),
                value -> binding.observationsEditText
                        .setText(value)
        );

        rendering = false;

        binding.categoryInputLayout.setError(
                state.getCategoryError()
        );

        binding.codeInputLayout.setError(
                state.getCodeError()
        );

        binding.siteInputLayout.setError(
                state.getSiteError()
        );

        boolean hasGeneralError =
                state.getGeneralError() != null;

        binding.generalErrorText.setText(
                state.getGeneralError()
        );

        binding.generalErrorText.setVisibility(
                hasGeneralError
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.savingProgress.setVisibility(
                state.isSaving()
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.saveButton.setEnabled(
                !state.isSaving()
        );

        binding.cancelButton.setEnabled(
                !state.isSaving()
        );

        setFieldsEnabled(!state.isSaving());
    }

    private void setFieldsEnabled(boolean enabled) {
        binding.categoryEditText.setEnabled(enabled);
        binding.codeEditText.setEnabled(enabled);
        binding.siteEditText.setEnabled(enabled);
        binding.positionEditText.setEnabled(enabled);
        binding.observationsEditText.setEnabled(enabled);
    }

    private void setTextIfDifferent(
            String current,
            String expected,
            TextSetter setter
    ) {
        if (!current.equals(expected)) {
            setter.set(expected);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private interface TextSetter {
        void set(String value);
    }
}