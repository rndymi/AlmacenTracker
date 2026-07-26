package com.rndymi.almacentracker.feature.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.core.scanner.ScannedCode;
import com.rndymi.almacentracker.core.scanner.ScannedCodeFormat;

import org.junit.Rule;
import org.junit.Test;

public final class ScannerViewModelTest {

    @Rule
    public final InstantTaskExecutorRule
            instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    @Test
    public void startsInitializing() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        ScannerUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);

        assertEquals(
                ScannerUiState.Status.INITIALIZING,
                state.getStatus()
        );
    }

    @Test
    public void grantedCameraBecomesScanning() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        viewModel.onCameraReady();

        assertEquals(
                ScannerUiState.Status.SCANNING,
                viewModel
                        .getUiState()
                        .getValue()
                        .getStatus()
        );
    }

    @Test
    public void firstDetectionIsAccepted() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        ScannedCode scannedCode =
                new ScannedCode(
                        "001050",
                        ScannedCodeFormat.CODE_128
                );

        viewModel.onCodeDetected(scannedCode);

        assertTrue(viewModel.hasAcceptedResult());

        assertEquals(
                ScannerUiState.Status.CODE_DETECTED,
                viewModel
                        .getUiState()
                        .getValue()
                        .getStatus()
        );

        UiEvent<ScannedCode> event =
                viewModel
                        .getScannedCodeEvent()
                        .getValue();

        assertNotNull(event);
        assertEquals(
                scannedCode,
                event.getContentIfNotHandled()
        );
    }

    @Test
    public void eventCanOnlyBeConsumedOnce() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        viewModel.onCodeDetected(
                new ScannedCode(
                        "001050",
                        ScannedCodeFormat.CODE_128
                )
        );

        UiEvent<ScannedCode> event =
                viewModel
                        .getScannedCodeEvent()
                        .getValue();

        assertNotNull(event);
        assertNotNull(event.getContentIfNotHandled());
        assertNull(event.getContentIfNotHandled());
    }

    @Test
    public void secondDetectionIsIgnored() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        ScannedCode first =
                new ScannedCode(
                        "001050",
                        ScannedCodeFormat.CODE_128
                );

        ScannedCode second =
                new ScannedCode(
                        "999999",
                        ScannedCodeFormat.QR_CODE
                );

        viewModel.onCodeDetected(first);

        UiEvent<ScannedCode> firstEvent =
                viewModel
                        .getScannedCodeEvent()
                        .getValue();

        viewModel.onCodeDetected(second);

        UiEvent<ScannedCode> currentEvent =
                viewModel
                        .getScannedCodeEvent()
                        .getValue();

        assertEquals(firstEvent, currentEvent);
        assertEquals(
                first,
                currentEvent.peekContent()
        );
    }

    @Test
    public void leadingZerosRemainInResult() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        viewModel.onCodeDetected(
                new ScannedCode(
                        "001050",
                        ScannedCodeFormat.EAN_13
                )
        );

        ScannedCode result =
                viewModel
                        .getScannedCodeEvent()
                        .getValue()
                        .peekContent();

        assertEquals("001050", result.getValue());
    }

    @Test
    public void urlRemainsPlainText() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        viewModel.onCodeDetected(
                new ScannedCode(
                        "https://example.com",
                        ScannedCodeFormat.QR_CODE
                )
        );

        ScannedCode result =
                viewModel
                        .getScannedCodeEvent()
                        .getValue()
                        .peekContent();

        assertEquals(
                "https://example.com",
                result.getValue()
        );
    }

    @Test
    public void temporaryPermissionDenialCanRetry() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        viewModel.onPermissionDenied(false);

        ScannerUiState state =
                viewModel.getUiState().getValue();

        assertEquals(
                ScannerUiState.Status.PERMISSION_DENIED,
                state.getStatus()
        );

        assertTrue(state.canRetryPermission());
        assertFalse(state.canOpenSettings());
    }

    @Test
    public void permanentPermissionDenialOffersSettings() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        viewModel.onPermissionDenied(true);

        ScannerUiState state =
                viewModel.getUiState().getValue();

        assertEquals(
                ScannerUiState.Status
                        .PERMISSION_DENIED_PERMANENTLY,
                state.getStatus()
        );

        assertFalse(state.canRetryPermission());
        assertTrue(state.canOpenSettings());
    }

    @Test
    public void scannerErrorIsControlled() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        viewModel.onScannerErrorOnce(
                "No se pudo analizar el código."
        );

        ScannerUiState state =
                viewModel.getUiState().getValue();

        assertEquals(
                ScannerUiState.Status.ERROR,
                state.getStatus()
        );

        assertNotNull(state.getMessage());
    }

    @Test
    public void firstFatalErrorIsAccepted() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        boolean accepted =
                viewModel.onScannerErrorOnce(
                        "No se pudo analizar el código."
                );

        assertTrue(accepted);
        assertTrue(viewModel.hasAcceptedFatalError());

        ScannerUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);

        assertEquals(
                ScannerUiState.Status.ERROR,
                state.getStatus()
        );

        assertEquals(
                "No se pudo analizar el código.",
                state.getMessage()
        );
    }

    @Test
    public void secondFatalErrorIsIgnored() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        assertTrue(
                viewModel.onScannerErrorOnce(
                        "Primer error"
                )
        );

        assertFalse(
                viewModel.onScannerErrorOnce(
                        "Segundo error"
                )
        );

        ScannerUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        assertEquals("Primer error", state.getMessage());
    }

    @Test
    public void retryClearsFatalErrorLock() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        viewModel.onScannerErrorOnce(
                "Primer error"
        );

        viewModel.retry();

        assertFalse(viewModel.hasAcceptedFatalError());

        assertEquals(
                ScannerUiState.Status.INITIALIZING,
                viewModel
                        .getUiState()
                        .getValue()
                        .getStatus()
        );

        assertTrue(
                viewModel.onScannerErrorOnce(
                        "Error después del reintento"
                )
        );
    }

    @Test
    public void acceptedResultBlocksFatalErrors() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        viewModel.onCodeDetected(
                new ScannedCode(
                        "001050",
                        ScannedCodeFormat.CODE_128
                )
        );

        boolean accepted =
                viewModel.onScannerErrorOnce(
                        "Error tardío"
                );

        assertFalse(accepted);

        assertEquals(
                ScannerUiState.Status.CODE_DETECTED,
                viewModel
                        .getUiState()
                        .getValue()
                        .getStatus()
        );
    }

    @Test
    public void acceptedResultBlocksRetry() {
        ScannerViewModel viewModel =
                new ScannerViewModel();

        viewModel.onCodeDetected(
                new ScannedCode(
                        "001050",
                        ScannedCodeFormat.CODE_128
                )
        );

        viewModel.retry();

        assertEquals(
                ScannerUiState.Status.CODE_DETECTED,
                viewModel
                        .getUiState()
                        .getValue()
                        .getStatus()
        );
    }
}