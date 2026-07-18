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

import com.rndymi.almacentracker.AlmacenTrackerApplication;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.adapter.in.ui.state.DataManagementUiState;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.DataManagementViewModel;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.DataManagementViewModelFactory;
import com.rndymi.almacentracker.application.result.ShareableCsvFile;
import com.rndymi.almacentracker.databinding.ActivityDataManagementBinding;

public final class DataManagementActivity
        extends AppCompatActivity {

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
                        .EXPORTING
                        || state.getStatus()
                        == DataManagementUiState.Status
                        .PREPARING_SHARE;

        binding.exportCsvButton.setEnabled(!busy);
        binding.shareCsvButton.setEnabled(!busy);

        binding.exportProgress.setVisibility(
                state.getStatus()
                        == DataManagementUiState.Status
                        .EXPORTING
                        || state.getStatus()
                        == DataManagementUiState.Status
                        .PREPARING_SHARE
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
        }
    }
}