package com.rndymi.almacentracker.feature.reference_list.capture;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.core.document.DocumentImageSource;

import org.junit.Test;

public final class ReferenceListCaptureUiStateTest {

    @Test
    public void emptyStateAllowsImageSelection() {
        ReferenceListCaptureUiState state =
                ReferenceListCaptureUiState.empty();

        assertTrue(state.canChooseImage());
        assertFalse(state.canProcessImage());
        assertFalse(state.shouldShowPreview());
    }

    @Test
    public void selectedImageCanBeProcessed() {
        ReferenceListCaptureUiState state =
                ReferenceListCaptureUiState
                        .imageSelected(
                                "content://selected",
                                DocumentImageSource
                                        .PHOTO_PICKER
                        );

        assertTrue(state.canProcessImage());
        assertTrue(state.shouldShowPreview());
        assertFalse(state.isProcessing());
    }

    @Test
    public void processingBlocksNewActions() {
        ReferenceListCaptureUiState state =
                ReferenceListCaptureUiState
                        .processing(
                                "content://selected",
                                DocumentImageSource.CAMERA
                        );

        assertTrue(state.isProcessing());
        assertFalse(state.canChooseImage());
        assertFalse(state.canProcessImage());
    }

    @Test
    public void recognitionErrorCanRetry() {
        ReferenceListCaptureUiState state =
                ReferenceListCaptureUiState
                        .recognitionError(
                                "content://selected",
                                DocumentImageSource.CAMERA
                        );

        assertTrue(state.canRetryRecognition());
        assertTrue(state.shouldShowPreview());
    }

    @Test
    public void imageErrorCannotRetryRecognition() {
        ReferenceListCaptureUiState state =
                ReferenceListCaptureUiState
                        .imageError(
                                "content://selected",
                                DocumentImageSource
                                        .PHOTO_PICKER
                        );

        assertFalse(state.canRetryRecognition());
        assertFalse(state.shouldShowPreview());
    }
}