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

    public LiveData<ScannerUiState> getUiState() {
        return uiState;
    }

    public LiveData<UiEvent<ScannedCode>>
    getScannedCodeEvent() {
        return scannedCodeEvent;
    }

    public void onPermissionRequestStarted() {
        if (resultAccepted.get()) {
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

        uiState.setValue(
                permanentlyDenied
                        ? ScannerUiState
                        .permissionDeniedPermanently()
                        : ScannerUiState.permissionDenied()
        );
    }

    public void onCameraInitializing() {
        if (resultAccepted.get()) {
            return;
        }

        uiState.setValue(
                ScannerUiState.initializing()
        );
    }

    public void onCameraReady() {
        if (resultAccepted.get()) {
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

        uiState.setValue(
                ScannerUiState.cameraUnavailable()
        );
    }

    public void onCodeDetected(ScannedCode scannedCode) {
        if (scannedCode == null) {
            return;
        }

        if (!resultAccepted.compareAndSet(false, true)) {
            return;
        }

        uiState.setValue(
                ScannerUiState.codeDetected()
        );

        scannedCodeEvent.setValue(
                new UiEvent<>(scannedCode)
        );
    }

    public void onScannerError() {
        if (resultAccepted.get()) {
            return;
        }

        uiState.postValue(
                ScannerUiState.error(
                        "No se pudo analizar la imagen."
                )
        );
    }

    public void retry() {
        if (resultAccepted.get()) {
            return;
        }

        uiState.setValue(
                ScannerUiState.initializing()
        );
    }

    public boolean hasAcceptedResult() {
        return resultAccepted.get();
    }
}