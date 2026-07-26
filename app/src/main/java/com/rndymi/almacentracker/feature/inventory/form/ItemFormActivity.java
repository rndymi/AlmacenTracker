package com.rndymi.almacentracker.feature.inventory.form;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.rndymi.almacentracker.app.AlmacenTrackerApplication;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rndymi.almacentracker.databinding.ActivityItemFormBinding;
import com.rndymi.almacentracker.feature.inventory.common.SimpleTextWatcher;
import com.rndymi.almacentracker.feature.scanner.ScannerActivity;
import com.rndymi.almacentracker.R;

public final class ItemFormActivity
        extends AppCompatActivity {

    public static final String EXTRA_WAREHOUSE_ITEM_ID =
            "com.rndymi.almacentracker.extra.FORM_WAREHOUSE_ITEM_ID";
    public static final String EXTRA_INITIAL_CODE =
            "com.rndymi.almacentracker.extra.FORM_INITIAL_CODE";

    private static final long CREATE_MODE_ITEM_ID = 0L;

    private ActivityItemFormBinding binding;
    private WarehouseItemFormViewModel viewModel;
    private ActivityResultLauncher<Intent> scannerActivityLauncher;
    private boolean rendering;

    public static Intent createIntent(
            Context context,
            @Nullable String initialCode
    ) {
        Intent intent =
                new Intent(context, ItemFormActivity.class);

        if (initialCode != null) {
            intent.putExtra(
                    EXTRA_INITIAL_CODE,
                    initialCode
            );
        }

        return intent;
    }

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

        configureScannerResult();
        configureToolbar();
        configureViewModel();
        configureInputListeners();
        configureActions();
        observeState();
        applyInitialCode();
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

    private void configureScannerResult() {
        scannerActivityLauncher =
                registerForActivityResult(
                        new ActivityResultContracts
                                .StartActivityForResult(),
                        result -> {
                            if (result.getResultCode()
                                    != RESULT_OK) {
                                return;
                            }

                            String scannedCode =
                                    ScannerActivity.getScannedValue(
                                            result.getData()
                                    );

                            if (scannedCode == null
                                    || scannedCode.trim().isEmpty()) {
                                Toast.makeText(
                                        this,
                                        R.string.scanned_code_apply_error,
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            viewModel.applyScannedCode(
                                    scannedCode
                            );
                        }
                );
    }

    private void configureActions() {
        binding.cancelButton.setOnClickListener(
                ignored -> finish()
        );

        binding.saveButton.setOnClickListener(
                ignored -> viewModel.save()
        );

        binding.codeInputLayout.setEndIconOnClickListener(
                ignored -> requestCodeScan()
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

        binding.codeInputLayout.setEndIconVisible(
                !editMode
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

        binding.codeInputLayout.setEndIconActivated(
                editable
                        && state.getMode()
                        == WarehouseItemFormMode.CREATE
        );

        binding.codeInputLayout.setEndIconCheckable(false);

        setFieldsEnabled(editable);
    }

    private void setFieldsEnabled(boolean enabled) {
        binding.categoryEditText.setEnabled(enabled);
        binding.codeEditText.setEnabled(enabled);
        binding.siteEditText.setEnabled(enabled);
        binding.positionEditText.setEnabled(enabled);
        binding.observationsEditText.setEnabled(enabled);

        binding.codeInputLayout.setEndIconOnClickListener(
                enabled
                        ? ignored -> requestCodeScan()
                        : null
        );
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

    private void applyInitialCode() {
        if (readWarehouseItemId()
                != CREATE_MODE_ITEM_ID) {
            return;
        }

        viewModel.applyInitialCode(
                getIntent().getStringExtra(
                        EXTRA_INITIAL_CODE
                )
        );
    }

    private void requestCodeScan() {
        WarehouseItemFormUiState state =
                viewModel.getUiState().getValue();

        if (state == null
                || state.getMode()
                != WarehouseItemFormMode.CREATE
                || !state.isEditable()) {
            return;
        }

        if (state.getCode().trim().isEmpty()) {
            openScanner();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        R.string.replace_form_code_title
                )
                .setMessage(
                        R.string.replace_form_code_message
                )
                .setNegativeButton(
                        R.string.cancel_action,
                        null
                )
                .setPositiveButton(
                        R.string.replace_form_code_action,
                        (dialog, which) -> openScanner()
                )
                .show();
    }

    private void openScanner() {
        scannerActivityLauncher.launch(
                ScannerActivity.createIntent(this)
        );
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
