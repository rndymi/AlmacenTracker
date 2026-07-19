package com.rndymi.almacentracker.adapter.in.ui.activity;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.rndymi.almacentracker.AlmacenTrackerApplication;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.adapter.in.ui.state.DataManagementUiState;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.DataManagementViewModel;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.DataManagementViewModelFactory;
import com.rndymi.almacentracker.application.result.ImportWarehouseItemIssue;
import com.rndymi.almacentracker.application.result.ImportWarehouseItemsResult;
import com.rndymi.almacentracker.application.result.ShareableCsvFile;
import com.rndymi.almacentracker.databinding.ActivityDataManagementBinding;

import java.util.List;

public final class DataManagementActivity
        extends AppCompatActivity {

    private static final int MAX_VISIBLE_IMPORT_ISSUES = 100;
    private ActivityDataManagementBinding binding;
    private DataManagementViewModel viewModel;

    private final ActivityResultLauncher<String>
            createCsvDocumentLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument(
                            "text/csv"
                    ),
                    this::handleDestinationResult
            );

    private final ActivityResultLauncher<String>
            createBackupDocumentLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument(
                            "text/csv"
                    ),
                    this::handleBackupDestinationResult
            );

    private final ActivityResultLauncher<String[]>
            openCsvDocumentLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    this::handleImportSourceResult
            );

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        binding = ActivityDataManagementBinding.inflate(
                getLayoutInflater()
        );

        setContentView(binding.getRoot());

        configureToolbar();
        configureViewModel();
        configureActions();
        observeViewModel();
    }

    private void configureToolbar() {
        binding.toolbar.setNavigationOnClickListener(
                ignored -> finish()
        );
    }

    private void configureViewModel() {
        AlmacenTrackerApplication application =
                (AlmacenTrackerApplication) getApplication();

        DataManagementViewModelFactory factory =
                application
                        .getAppContainer()
                        .provideDataManagementViewModelFactory();

        viewModel = new ViewModelProvider(
                this,
                factory
        ).get(DataManagementViewModel.class);
    }

    private void configureActions() {
        binding.exportCsvButton.setOnClickListener(
                ignored ->
                        viewModel.requestExportDestination()
        );

        binding.shareCsvButton.setOnClickListener(
                ignored ->
                        viewModel.shareWarehouseItems()
        );

        binding.retryExportButton.setOnClickListener(
                ignored -> viewModel.retry()
        );

        binding.importCsvButton.setOnClickListener(
                ignored -> viewModel.requestImportSource()
        );

        binding.createBackupButton.setOnClickListener(
                ignored ->
                        viewModel.requestBackupDestination()
        );
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(
                this,
                this::renderState
        );

        viewModel.getDestinationRequest().observe(
                this,
                event -> {
                    String suggestedFileName =
                            event.getContentIfNotHandled();

                    if (suggestedFileName != null) {
                        createCsvDocumentLauncher.launch(
                                suggestedFileName
                        );
                    }
                }
        );

        viewModel.getExportSuccess().observe(
                this,
                event -> {
                    Integer exportedCount =
                            event.getContentIfNotHandled();

                    if (exportedCount != null) {
                        Toast.makeText(
                                this,
                                getResources().getQuantityString(
                                        R.plurals
                                                .export_csv_success,
                                        exportedCount,
                                        exportedCount
                                ),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

        viewModel.getShareFileReady().observe(
                this,
                event -> {
                    ShareableCsvFile shareableFile =
                            event.getContentIfNotHandled();

                    if (shareableFile != null) {
                        openShareChooser(shareableFile);
                    }
                }
        );

        viewModel.getSourceRequest().observe(
                this,
                event -> {
                    Boolean shouldOpen =
                            event.getContentIfNotHandled();

                    if (Boolean.TRUE.equals(shouldOpen)) {
                        openCsvDocumentLauncher.launch(
                                new String[]{
                                        "text/csv",
                                        "text/comma-separated-values",
                                        "application/csv",
                                        "text/plain"
                                }
                        );
                    }
                }
        );

        viewModel.getImportCompleted().observe(
                this,
                event -> {
                    ImportWarehouseItemsResult result =
                            event.getContentIfNotHandled();

                    if (result != null) {
                        showImportResult(result);
                    }
                }
        );

        viewModel.getBackupDestinationRequest().observe(
                this,
                event -> {
                    String suggestedFileName =
                            event.getContentIfNotHandled();

                    if (suggestedFileName != null) {
                        createBackupDocumentLauncher.launch(
                                suggestedFileName
                        );
                    }
                }
        );

        viewModel.getBackupSuccess().observe(
                this,
                event -> {
                    Integer backedUpCount =
                            event.getContentIfNotHandled();

                    if (backedUpCount == null) {
                        return;
                    }

                    int messageResource =
                            backedUpCount == 0
                                    ? R.string
                                      .create_backup_empty_success
                                    : 0;

                    String message =
                            messageResource != 0
                                    ? getString(messageResource)
                                    : getResources()
                                    .getQuantityString(
                                            R.plurals
                                            .create_backup_success,
                                            backedUpCount,
                                            backedUpCount
                                    );

                    Toast.makeText(
                            this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
        );
    }

    private void handleDestinationResult(
            Uri destinationUri
    ) {
        viewModel.onDestinationSelected(
                destinationUri == null
                        ? null
                        : destinationUri.toString()
        );
    }

    private void openShareChooser(
            ShareableCsvFile shareableFile
    ) {
        final Uri contentUri;

        try {
            contentUri = Uri.parse(
                    shareableFile.getContentReference()
            );
        } catch (RuntimeException exception) {
            viewModel.onInvalidShareReference();
            return;
        }

        if (!"content".equals(contentUri.getScheme())) {
            viewModel.onInvalidShareReference();
            return;
        }

        Intent sendIntent =
                new Intent(Intent.ACTION_SEND);

        sendIntent.setType(
                shareableFile.getMimeType()
        );

        sendIntent.putExtra(
                Intent.EXTRA_STREAM,
                contentUri
        );

        sendIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.share_csv_subject)
        );

        sendIntent.setClipData(
                ClipData.newUri(
                        getContentResolver(),
                        shareableFile.getFileName(),
                        contentUri
                )
        );

        sendIntent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
        );

        /*
         * No se añade FLAG_GRANT_WRITE_URI_PERMISSION.
         */
        if (sendIntent.resolveActivity(
                getPackageManager()
        ) == null) {
            viewModel.onNoShareApplicationAvailable();
            return;
        }

        Intent chooser = Intent.createChooser(
                sendIntent,
                getString(
                        R.string.share_csv_chooser_title
                )
        );

        startActivity(chooser);
        viewModel.onShareChooserLaunched();
    }

    private void renderState(
            DataManagementUiState state
    ) {
        boolean busy =
                state.getStatus()
                        == DataManagementUiState.Status
                        .SELECTING_DESTINATION
                        || state.getStatus()
                        == DataManagementUiState.Status
                        .SELECTING_SOURCE
                        || state.getStatus()
                        == DataManagementUiState.Status
                        .EXPORTING
                        || state.getStatus()
                        == DataManagementUiState.Status
                        .PREPARING_SHARE
                        || state.getStatus()
                        == DataManagementUiState.Status
                        .IMPORTING
                        || state.getStatus()
                        == DataManagementUiState.Status
                        .SELECTING_BACKUP_DESTINATION
                        || state.getStatus()
                        == DataManagementUiState.Status
                        .CREATING_BACKUP;

        binding.exportCsvButton.setEnabled(!busy);
        binding.shareCsvButton.setEnabled(!busy);
        binding.importCsvButton.setEnabled(!busy);
        binding.createBackupButton.setEnabled(!busy);

        binding.exportProgress.setVisibility(
                state.getStatus()
                        == DataManagementUiState.Status
                        .EXPORTING
                        || state.getStatus()
                        == DataManagementUiState.Status
                        .PREPARING_SHARE
                        || state.getStatus()
                        == DataManagementUiState.Status
                        .IMPORTING
                        || state.getStatus()
                        == DataManagementUiState.Status
                        .CREATING_BACKUP
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.exportStatusText.setVisibility(
                state.getStatus()
                        == DataManagementUiState.Status.IDLE
                        ? View.GONE
                        : View.VISIBLE
        );

        binding.retryExportButton.setVisibility(
                state.getStatus()
                        == DataManagementUiState.Status.ERROR
                        ? View.VISIBLE
                        : View.GONE
        );

        switch (state.getStatus()) {
            case SELECTING_DESTINATION:
                binding.exportStatusText.setText(
                        R.string
                                .export_csv_selecting_destination
                );
                break;

            case EXPORTING:
                binding.exportStatusText.setText(
                        R.string.export_csv_in_progress
                );
                break;

            case PREPARING_SHARE:
                binding.exportStatusText.setText(
                        R.string.share_csv_preparing
                );
                break;

            case EMPTY_DATABASE:
            case ERROR:
                binding.exportStatusText.setText(
                        state.getMessage()
                );
                break;

            case IDLE:
            default:
                binding.exportStatusText.setText("");
                break;

            case SELECTING_SOURCE:
                binding.exportStatusText.setText(
                        R.string.import_csv_selecting_source
                );
                break;

            case IMPORTING:
                binding.exportStatusText.setText(
                        R.string.import_csv_in_progress
                );
                break;

            case SELECTING_BACKUP_DESTINATION:
                binding.exportStatusText.setText(
                        R.string
                                .create_backup_selecting_destination
                );
                break;

            case CREATING_BACKUP:
                binding.exportStatusText.setText(
                        R.string.create_backup_in_progress
                );
                break;
        }
    }

    private void handleImportSourceResult(
            Uri sourceUri
    ) {
        viewModel.onImportSourceSelected(
                sourceUri == null
                        ? null
                        : sourceUri.toString()
        );
    }

    private void showImportResult(
            ImportWarehouseItemsResult result
    ) {
        switch (result.getStatus()) {
            case SUCCESS:
            case PARTIAL_SUCCESS:
            case NO_VALID_ROWS:
                showCompletedImportResult(result);
                break;

            default:
                break;
        }
    }

    private void showCompletedImportResult(
            ImportWarehouseItemsResult result
    ) {
        StringBuilder message = new StringBuilder();

        if (result.getStatus()
                == ImportWarehouseItemsResult.Status
                .NO_VALID_ROWS) {
            message.append(
                    getString(
                            R.string.import_csv_no_valid_rows
                    )
            ).append("\n\n");
        }

        message.append(
                getString(
                        R.string.import_csv_detailed_summary,
                        result.getTotalRows(),
                        result.getImportedCount(),
                        result.getDuplicateCount(),
                        result.getInvalidCount()
                )
        );

        if (result.hasIssues()) {
            appendImportIssues(
                    message,
                    result.getIssues()
            );
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        result.getStatus()
                                == ImportWarehouseItemsResult
                                .Status.SUCCESS
                                ? R.string
                                  .import_csv_success_title
                                : R.string
                                  .import_csv_result_title
                )
                .setMessage(message.toString())
                .setPositiveButton(
                        R.string.close_action,
                        null
                )
                .show();
    }

    private void appendImportIssues(
            StringBuilder message,
            List<ImportWarehouseItemIssue> issues
    ) {
        message.append("\n\n")
                .append(
                        getString(
                                R.string
                                        .import_csv_issues_title
                        )
                );

        int visibleCount = Math.min(
                issues.size(),
                MAX_VISIBLE_IMPORT_ISSUES
        );

        for (int index = 0;
             index < visibleCount;
             index++) {

            ImportWarehouseItemIssue issue =
                    issues.get(index);

            message.append("\n")
                    .append(
                            getString(
                                    R.string
                                            .import_csv_issue_format,
                                    issue.getRowNumber(),
                                    issue.getMessage()
                            )
                    );
        }

        if (issues.size() > visibleCount) {
            message.append("\n\n")
                    .append(
                            getString(
                                    R.string
                                            .import_csv_visible_issues_limit,
                                    visibleCount,
                                    issues.size()
                            )
                    );
        }
    }

    private void handleBackupDestinationResult(
            Uri destinationUri
    ) {
        viewModel.onBackupDestinationSelected(
                destinationUri == null
                        ? null
                        : destinationUri.toString()
        );
    }
}