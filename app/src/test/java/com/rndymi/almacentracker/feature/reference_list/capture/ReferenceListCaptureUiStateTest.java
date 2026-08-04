package com.rndymi.almacentracker.feature.reference_list.capture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.core.document.DocumentImageSource;
import com.rndymi.almacentracker.core.document.RecognizedDocument;
import com.rndymi.almacentracker.core.document.RecognizedTextLine;

import org.junit.Test;

import java.util.Collections;

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
                                DocumentImageSource.CAMERA,
                                0
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
                                DocumentImageSource.CAMERA,
                                0
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
                                        .PHOTO_PICKER,
                                0
                        );

        assertFalse(state.canRetryRecognition());
        assertFalse(state.shouldShowPreview());
    }

    @Test
    public void imageSelectedStartsWithZeroManualRotation() {
        ReferenceListCaptureUiState state =
                ReferenceListCaptureUiState.imageSelected(
                        "content://image",
                        DocumentImageSource.PHOTO_PICKER
                );

        assertEquals(
                0,
                state.getManualRotationDegrees()
        );
    }

    @Test
    public void processingPreservesManualRotation() {
        ReferenceListCaptureUiState state =
                ReferenceListCaptureUiState.processing(
                        "content://image",
                        DocumentImageSource.PHOTO_PICKER,
                        90
                );

        assertEquals(
                90,
                state.getManualRotationDegrees()
        );

        assertFalse(state.canRotateImage());
    }

    @Test
    public void recognizedStatePreservesManualRotation() {
        ReferenceListCaptureUiState state =
                ReferenceListCaptureUiState.textRecognized(
                        "content://image",
                        DocumentImageSource.PHOTO_PICKER,
                        createRecognizedDocument(),
                        180
                );

        assertEquals(
                180,
                state.getManualRotationDegrees()
        );
    }

    @Test
    public void noTextStateCanRotateAgain() {
        ReferenceListCaptureUiState state =
                ReferenceListCaptureUiState.noTextFound(
                        "content://image",
                        DocumentImageSource.PHOTO_PICKER,
                        270
                );

        assertTrue(state.canRotateImage());
        assertEquals(
                270,
                state.getManualRotationDegrees()
        );
    }

    @Test
    public void changingRotationInvalidatesRecognizedDocument() {
        ReferenceListCaptureUiState recognized =
                ReferenceListCaptureUiState.textRecognized(
                        "content://image",
                        DocumentImageSource.PHOTO_PICKER,
                        createRecognizedDocument(),
                        0
                );

        ReferenceListCaptureUiState rotated =
                recognized.withManualRotationDegrees(
                        90
                );

        assertEquals(
                ReferenceListCaptureUiState.Status.IMAGE_SELECTED,
                rotated.getStatus()
        );

        assertEquals(
                90,
                rotated.getManualRotationDegrees()
        );

        assertNull(rotated.getRecognizedDocument());
        assertFalse(rotated.isRawTextExpanded());
    }

    @Test
    public void rawTextExpansionPreservesManualRotation() {
        ReferenceListCaptureUiState state =
                ReferenceListCaptureUiState.textRecognized(
                        "content://image",
                        DocumentImageSource.PHOTO_PICKER,
                        createRecognizedDocument(),
                        90
                );

        ReferenceListCaptureUiState expanded =
                state.withRawTextExpanded(true);

        assertEquals(
                90,
                expanded.getManualRotationDegrees()
        );
    }

    @Test
    public void emptyStateDoesNotAllowRotation() {
        ReferenceListCaptureUiState state =
                ReferenceListCaptureUiState.empty();

        assertEquals(
                0,
                state.getManualRotationDegrees()
        );

        assertFalse(state.canRotateImage());
    }

    @Test
    public void invalidManualRotationIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReferenceListCaptureUiState
                        .imageSelected(
                                "content://image",
                                DocumentImageSource.PHOTO_PICKER,
                                45
                        )
        );
    }

    private RecognizedDocument createRecognizedDocument() {
        return new RecognizedDocument(
                DocumentImageSource.PHOTO_PICKER,
                Collections.singletonList(
                        new RecognizedTextLine(
                                0,
                                "MR 1210 A"
                        )
                ),
                1000L
        );
    }
}
