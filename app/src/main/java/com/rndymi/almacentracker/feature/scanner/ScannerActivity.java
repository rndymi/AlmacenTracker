package com.rndymi.almacentracker.feature.scanner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.util.concurrent.ListenableFuture;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.core.scanner.ScannedCode;
import com.rndymi.almacentracker.data.scanner.MlKitBarcodeMapper;
import com.rndymi.almacentracker.data.scanner.MlKitCodeScanner;
import com.rndymi.almacentracker.databinding.ActivityScannerBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ScannerActivity
        extends AppCompatActivity {

    public static final String EXTRA_SCANNED_VALUE =
            "extra_scanned_value";

    public static final String EXTRA_SCANNED_FORMAT =
            "extra_scanned_format";

    private static final String TAG =
            "ScannerActivity";

    private ActivityScannerBinding binding;
    private ScannerViewModel viewModel;

    private ProcessCameraProvider cameraProvider;
    private MlKitCodeScanner codeScanner;
    private ExecutorService analysisExecutor;

    private boolean permissionRequestedInCurrentFlow;

    private final ActivityResultLauncher<String>
            cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts
                            .RequestPermission(),
                    this::handleCameraPermissionResult
            );

    public static Intent createIntent(Context context) {
        return new Intent(
                context,
                ScannerActivity.class
        );
    }

    @Nullable
    public static String getScannedValue(Intent data) {
        if (data == null) {
            return null;
        }

        return data.getStringExtra(
                EXTRA_SCANNED_VALUE
        );
    }

    @Nullable
    public static String getScannedFormat(Intent data) {
        if (data == null) {
            return null;
        }

        return data.getStringExtra(
                EXTRA_SCANNED_FORMAT
        );
    }

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        binding = ActivityScannerBinding.inflate(
                getLayoutInflater()
        );

        setContentView(binding.getRoot());

        analysisExecutor =
                Executors.newSingleThreadExecutor();

        viewModel = new ViewModelProvider(this)
                .get(ScannerViewModel.class);

        configureToolbar();
        configureActions();
        observeViewModel();

        if (savedInstanceState == null) {
            prepareScanner();
        }
    }

    private void configureToolbar() {
        binding.scannerToolbar
                .setNavigationOnClickListener(
                        ignored -> cancelScanner()
                );
    }

    private void configureActions() {
        binding.scannerRetryButton
                .setOnClickListener(
                        ignored -> retryScanner()
                );

        binding.scannerSettingsButton
                .setOnClickListener(
                        ignored -> openApplicationSettings()
                );

        binding.scannerCancelButton
                .setOnClickListener(
                        ignored -> cancelScanner()
                );
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(
                this,
                this::render
        );

        viewModel.getScannedCodeEvent().observe(
                this,
                this::handleScannedCodeEvent
        );
    }

    private void prepareScanner() {
        if (!hasCameraFeature()) {
            viewModel.onCameraUnavailable();
            return;
        }

        if (hasCameraPermission()) {
            startCamera();
            return;
        }

        requestCameraPermission();
    }

    private boolean hasCameraFeature() {
        return getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY
        );
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        permissionRequestedInCurrentFlow = true;
        viewModel.onPermissionRequestStarted();

        cameraPermissionLauncher.launch(
                Manifest.permission.CAMERA
        );
    }

    private void handleCameraPermissionResult(
            boolean granted
    ) {
        if (granted) {
            startCamera();
            return;
        }

        boolean permanentlyDenied =
                permissionRequestedInCurrentFlow
                        && !shouldShowRequestPermissionRationale(
                        Manifest.permission.CAMERA
                );

        viewModel.onPermissionDenied(
                permanentlyDenied
        );
    }

    private void startCamera() {
        if (viewModel.hasAcceptedResult()) {
            return;
        }

        viewModel.onCameraInitializing();

        ListenableFuture<ProcessCameraProvider>
                providerFuture =
                ProcessCameraProvider.getInstance(this);

        providerFuture.addListener(
                () -> bindCamera(providerFuture),
                ContextCompat.getMainExecutor(this)
        );
    }

    private void bindCamera(
            ListenableFuture<ProcessCameraProvider>
                    providerFuture
    ) {
        try {
            cameraProvider = providerFuture.get();

            Preview preview =
                    new Preview.Builder().build();

            preview.setSurfaceProvider(
                    binding.previewView
                            .getSurfaceProvider()
            );

            ImageAnalysis imageAnalysis =
                    new ImageAnalysis.Builder()
                            .setBackpressureStrategy(
                                    ImageAnalysis
                                            .STRATEGY_KEEP_ONLY_LATEST
                            )
                            .build();

            closeCodeScanner();

            codeScanner = new MlKitCodeScanner(
                    new MlKitBarcodeMapper(),
                    new MlKitCodeScanner.Listener() {
                        @Override
                        public void onCodeDetected(
                                ScannedCode scannedCode
                        ) {
                            viewModel.onCodeDetected(
                                    scannedCode
                            );
                        }

                        @Override
                        public void onScannerError(
                                Throwable cause
                        ) {
                            Log.e(
                                    TAG,
                                    "Barcode analysis failed",
                                    cause
                            );

                            viewModel.onScannerError();
                        }
                    }
            );

            imageAnalysis.setAnalyzer(
                    analysisExecutor,
                    codeScanner
            );

            cameraProvider.unbindAll();

            if (!cameraProvider.hasCamera(
                    CameraSelector.DEFAULT_BACK_CAMERA
            )) {
                closeCodeScanner();
                viewModel.onCameraUnavailable();
                return;
            }

            cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
            );

            viewModel.onCameraReady();
        } catch (Exception exception) {
            Log.e(
                    TAG,
                    "Camera initialization failed",
                    exception
            );

            releaseCamera();
            viewModel.onScannerError();
        }
    }

    private void retryScanner() {
        viewModel.retry();

        if (!hasCameraPermission()) {
            requestCameraPermission();
            return;
        }

        startCamera();
    }

    private void handleScannedCodeEvent(
            UiEvent<ScannedCode> event
    ) {
        if (event == null) {
            return;
        }

        ScannedCode scannedCode =
                event.getContentIfNotHandled();

        if (scannedCode == null) {
            return;
        }

        releaseCamera();

        Intent result = new Intent();

        result.putExtra(
                EXTRA_SCANNED_VALUE,
                scannedCode.getValue()
        );

        result.putExtra(
                EXTRA_SCANNED_FORMAT,
                scannedCode.getFormat().name()
        );

        setResult(RESULT_OK, result);
        finish();
    }

    private void render(ScannerUiState state) {
        binding.scannerProgress.setVisibility(
                state.showsProgress()
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.previewView.setVisibility(
                state.showsPreview()
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        binding.scannerFrame.setVisibility(
                state.getStatus()
                        == ScannerUiState.Status.SCANNING
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.scannerHelpText.setVisibility(
                state.getStatus()
                        == ScannerUiState.Status.SCANNING
                        ? View.VISIBLE
                        : View.GONE
        );

        boolean showError =
                state.getStatus()
                        == ScannerUiState.Status
                        .PERMISSION_DENIED
                        || state.getStatus()
                        == ScannerUiState.Status
                        .PERMISSION_DENIED_PERMANENTLY
                        || state.getStatus()
                        == ScannerUiState.Status
                        .CAMERA_UNAVAILABLE
                        || state.getStatus()
                        == ScannerUiState.Status.ERROR;

        binding.scannerErrorCard.setVisibility(
                showError ? View.VISIBLE : View.GONE
        );

        if (!showError) {
            return;
        }

        binding.scannerErrorText.setText(
                getMessageForState(state)
        );

        binding.scannerRetryButton.setVisibility(
                state.canRetryPermission()
                        || state.canRetryScanner()
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.scannerSettingsButton.setVisibility(
                state.canOpenSettings()
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private String getMessageForState(
            ScannerUiState state
    ) {
        switch (state.getStatus()) {
            case PERMISSION_DENIED:
                return getString(
                        R.string.scanner_permission_denied
                );

            case PERMISSION_DENIED_PERMANENTLY:
                return getString(
                        R.string
                                .scanner_permission_permanently_denied
                );

            case CAMERA_UNAVAILABLE:
                return getString(
                        R.string.scanner_camera_unavailable
                );

            case ERROR:
                return state.getMessage() == null
                        ? getString(
                        R.string.scanner_camera_error
                )
                        : state.getMessage();

            default:
                return "";
        }
    }

    private void openApplicationSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        );

        intent.setData(
                Uri.fromParts(
                        "package",
                        getPackageName(),
                        null
                )
        );

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ScannerUiState state =
                viewModel.getUiState().getValue();

        if (state != null
                && state.getStatus()
                == ScannerUiState.Status
                .PERMISSION_DENIED_PERMANENTLY
                && hasCameraPermission()) {
            startCamera();
        }
    }

    private void cancelScanner() {
        releaseCamera();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void releaseCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }

        closeCodeScanner();
    }

    private void closeCodeScanner() {
        if (codeScanner == null) {
            return;
        }

        codeScanner.close();
        codeScanner = null;
    }

    @Override
    protected void onDestroy() {
        releaseCamera();

        if (analysisExecutor != null) {
            analysisExecutor.shutdown();
            analysisExecutor = null;
        }

        binding = null;

        super.onDestroy();
    }
}