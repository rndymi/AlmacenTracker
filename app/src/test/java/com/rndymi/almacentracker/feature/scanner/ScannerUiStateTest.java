package com.rndymi.almacentracker.feature.scanner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ScannerUiStateTest {

    @Test
    public void temporaryPermissionDenialOffersRetryAndManualEntry() {
        ScannerUiState state =
                ScannerUiState.permissionDenied();

        assertTrue(state.canRetryPermission());
        assertTrue(state.canContinueManually());
        assertFalse(state.canOpenSettings());
        assertFalse(state.canRetryScanner());
    }

    @Test
    public void permanentPermissionDenialOffersSettingsAndManualEntry() {
        ScannerUiState state =
                ScannerUiState
                        .permissionDeniedPermanently();

        assertTrue(state.canOpenSettings());
        assertTrue(state.canContinueManually());
        assertFalse(state.canRetryPermission());
        assertFalse(state.canRetryScanner());
    }

    @Test
    public void cameraUnavailableOnlyOffersManualEntry() {
        ScannerUiState state =
                ScannerUiState.cameraUnavailable();

        assertTrue(state.canContinueManually());
        assertFalse(state.canRetryPermission());
        assertFalse(state.canOpenSettings());
        assertFalse(state.canRetryScanner());
    }

    @Test
    public void scannerErrorOffersRetryAndManualEntry() {
        ScannerUiState state =
                ScannerUiState.error(
                        "No se pudo iniciar el escáner."
                );

        assertTrue(state.canRetryScanner());
        assertTrue(state.canContinueManually());
        assertFalse(state.canRetryPermission());
        assertFalse(state.canOpenSettings());
    }

    @Test
    public void permissionRequestDoesNotExposeRecoveryActions() {
        ScannerUiState state =
                ScannerUiState.requestingPermission();

        assertFalse(state.canRetryPermission());
        assertFalse(state.canOpenSettings());
        assertFalse(state.canRetryScanner());
        assertFalse(state.canContinueManually());
    }

    @Test
    public void acceptedCodeDoesNotExposeRecoveryActions() {
        ScannerUiState state =
                ScannerUiState.codeDetected();

        assertFalse(state.canRetryPermission());
        assertFalse(state.canOpenSettings());
        assertFalse(state.canRetryScanner());
        assertFalse(state.canContinueManually());
    }
}