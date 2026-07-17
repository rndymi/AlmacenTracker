package com.rndymi.almacentracker.adapter.in.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.AlmacenTrackerApplication;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemFormMode;
import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemFormUiState;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemFormViewModel;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemFormViewModelFactory;
import com.rndymi.almacentracker.databinding.ActivityItemFormBinding;

public final class ItemFormActivity
        extends AppCompatActivity {

    public static final String EXTRA_WAREHOUSE_ITEM_ID =
            "com.rndymi.almacentracker.extra.FORM_WAREHOUSE_ITEM_ID";

    private static final long CREATE_MODE_ITEM_ID = 0L;

    private ActivityItemFormBinding binding;
    private WarehouseItemFormViewModel viewModel;
    private boolean rendering;

    public static Intent createEditIntent(
            Context context,
            long warehouseItemId
    ) {
        Intent intent =
                new Intent(context, ItemFormActivity.class);

        intent.putExtra(
                EXTRA_WAREHOUSE_ITEM_ID,
                warehouseItemId
        );

        return intent;
    }

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
                ignored -> getOnBackPressedDispatcher()
                        .onBackPressed()
        );
    }

    private void configureViewModel() {
        AlmacenTrackerApplication application =
                (AlmacenTrackerApplication)
                        getApplication();

        WarehouseItemFormViewModelFactory factory =
                application
                        .getAppContainer()
                        .provideWarehouseItemFormViewModelFactory(
                                readWarehouseItemId()
                        );

        viewModel = new ViewModelProvider(
                this,
                factory
        ).get(WarehouseItemFormViewModel.class);
    }

    private long readWarehouseItemId() {
        return getIntent().getLongExtra(
                EXTRA_WAREHOUSE_ITEM_ID,
                CREATE_MODE_ITEM_ID
        );
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
                    if (event == null
                            || event.getContentIfNotHandled() == null) {
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

        viewModel.getUpdateSuccess().observe(
                this,
                event -> {
                    if (event == null
                            || event.getContentIfNotHandled() == null) {
                        return;
                    }

                    Toast.makeText(
                            this,
                            R.string.warehouse_item_updated,
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                }
        );
    }

    private void render(
            WarehouseItemFormUiState state
    ) {
        renderMode(state);
        renderContent(state);
        renderErrors(state);
        renderAvailability(state);
    }

    private void renderMode(
            WarehouseItemFormUiState state
    ) {
        boolean editMode =
                state.getMode()
                        == WarehouseItemFormMode.EDIT;

        binding.toolbar.setTitle(
                editMode
                        ? R.string.edit_warehouse_item_title
                        : R.string.register_warehouse_item_title
        );

        binding.saveButton.setText(
                editMode
                        ? R.string.save_changes_action
                        : R.string.save_action
        );
    }

    private void renderContent(
            WarehouseItemFormUiState state
    ) {
        binding.initialLoadingProgress.setVisibility(
                state.isLoading()
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.formContent.setVisibility(
                state.isLoading()
                        ? View.GONE
                        : View.VISIBLE
        );

        rendering = true;

        setTextIfDifferent(
                textOf(binding.categoryEditText),
                state.getCategory(),
                value -> binding.categoryEditText.setText(value)
        );

        setTextIfDifferent(
                textOf(binding.codeEditText),
                state.getCode(),
                value -> binding.codeEditText.setText(value)
        );

        setTextIfDifferent(
                textOf(binding.siteEditText),
                state.getSite(),
                value -> binding.siteEditText.setText(value)
        );

        setTextIfDifferent(
                textOf(binding.positionEditText),
                state.getPosition(),
                value -> binding.positionEditText.setText(value)
        );

        setTextIfDifferent(
                textOf(binding.observationsEditText),
                state.getObservations(),
                value -> binding.observationsEditText
                        .setText(value)
        );

        rendering = false;
    }

    private void renderErrors(
            WarehouseItemFormUiState state
    ) {
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
    }

    private void renderAvailability(
            WarehouseItemFormUiState state
    ) {
        binding.savingProgress.setVisibility(
                state.isSaving()
                        ? View.VISIBLE
                        : View.GONE
        );

        boolean editable = state.isEditable();

        binding.saveButton.setEnabled(editable);
        binding.cancelButton.setEnabled(!state.isSaving());

        setFieldsEnabled(editable);
    }

    private void setFieldsEnabled(boolean enabled) {
        binding.categoryEditText.setEnabled(enabled);
        binding.codeEditText.setEnabled(enabled);
        binding.siteEditText.setEnabled(enabled);
        binding.positionEditText.setEnabled(enabled);
        binding.observationsEditText.setEnabled(enabled);
    }

    private String textOf(
            android.widget.EditText editText
    ) {
        return editText.getText() == null
                ? ""
                : editText.getText().toString();
    }

    private void setTextIfDifferent(
            String current,
            String expected,
            TextSetter setter
    ) {
        String safeExpected =
                expected == null ? "" : expected;

        if (!current.equals(safeExpected)) {
            setter.set(safeExpected);
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