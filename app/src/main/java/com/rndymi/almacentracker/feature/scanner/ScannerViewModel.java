package com.rndymi.almacentracker.feature.scanner;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.core.scanner.ScannedCode;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ScannerViewModel extends ViewModel {

    private final MutableLiveData<ScannerUiState> uiState =
            new MutableLiveData<>(
                    ScannerUiState.initializing()
            );

    private final MutableLiveData<UiEvent<ScannedCode>>
            scannedCodeEvent =
            new MutableLiveData<>();

    private final AtomicBoolean resultAccepted =
            new AtomicBoolean(false);

    private final AtomicBoolean fatalErrorAccepted =
            new AtomicBoolean(false);

    public LiveData<ScannerUiState> getUiState() {
        return uiState;
    }

    public LiveData<UiEvent<ScannedCode>>
    getScannedCodeEvent() {
        return scannedCodeEvent;
    }

    public void onPermissionRequestStarted() {
        if (!canChangeScannerState()) {
            return;
        }

        uiState.setValue(
                ScannerUiState.requestingPermission()
        );
    }

    public void onPermissionDenied(
            boolean permanentlyDenied
    ) {
        if (resultAccepted.get()) {
            return;
        }

        fatalErrorAccepted.set(false);

        uiState.setValue(
                permanentlyDenied
                        ? ScannerUiState
                        .permissionDeniedPermanently()
                        : ScannerUiState.permissionDenied()
        );
    }

    public void onCameraInitializing() {
        if (!canChangeScannerState()) {
            return;
        }

        uiState.setValue(
                ScannerUiState.initializing()
        );
    }

    public void onCameraReady() {
        if (!canChangeScannerState()) {
            return;
        }

        uiState.setValue(
                ScannerUiState.scanning()
        );
    }

    public void onCameraUnavailable() {
        if (resultAccepted.get()) {
            return;
        }

        fatalErrorAccepted.set(false);

        uiState.setValue(
                ScannerUiState.cameraUnavailable()
        );
    }

    public void onCodeDetected(ScannedCode scannedCode) {
        if (scannedCode == null) {
            return;
        }

        if (!resultAccepted.compareAndSet(
                false,
                true
        )) {
            return;
        }

        uiState.setValue(
                ScannerUiState.codeDetected()
        );

        scannedCodeEvent.setValue(
                new UiEvent<>(scannedCode)
        );
    }

    public boolean onScannerErrorOnce(
            String message
    ) {
        if (resultAccepted.get()) {
            return false;
        }

        if (!fatalErrorAccepted.compareAndSet(
                false,
                true
        )) {
            return false;
        }

        uiState.setValue(
                ScannerUiState.error(message)
        );

        return true;
    }

    public void retry() {
        if (resultAccepted.get()) {
            return;
        }

        fatalErrorAccepted.set(false);

        uiState.setValue(
                ScannerUiState.initializing()
        );
    }

    public boolean hasAcceptedResult() {
        return resultAccepted.get();
    }

    public boolean hasAcceptedFatalError() {
        return fatalErrorAccepted.get();
    }

    private boolean canChangeScannerState() {
        return !resultAccepted.get()
                && !fatalErrorAccepted.get();
    }
}