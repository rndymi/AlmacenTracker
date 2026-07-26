package com.rndymi.almacentracker.feature.reference_list.capture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.rndymi.almacentracker.core.document.DocumentImageSource;
import com.rndymi.almacentracker.core.document.DocumentRecognitionCallback;
import com.rndymi.almacentracker.core.document.DocumentTextRecognizer;
import com.rndymi.almacentracker.core.document.RecognizedDocument;
import com.rndymi.almacentracker.core.document.RecognizedTextLine;

import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;

public final class ReferenceListCaptureViewModelTest {

    @Rule
    public final InstantTaskExecutorRule
            instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    @Test
    public void startsEmpty() {
        ReferenceListCaptureViewModel viewModel =
                new ReferenceListCaptureViewModel(
                        new FakeRecognizer()
                );

        assertEquals(
                ReferenceListCaptureUiState.Status.EMPTY,
                requireState(viewModel).getStatus()
        );
    }

    @Test
    public void selectedImageBecomesReady() {
        ReferenceListCaptureViewModel viewModel =
                new ReferenceListCaptureViewModel(
                        new FakeRecognizer()
                );

        viewModel.selectImage(
                "content://image",
                DocumentImageSource.PHOTO_PICKER
        );

        ReferenceListCaptureUiState state =
                requireState(viewModel);

        assertEquals(
                ReferenceListCaptureUiState
                        .Status.IMAGE_SELECTED,
                state.getStatus()
        );

        assertEquals(
                "content://image",
                state.getImageUri()
        );
    }

    @Test
    public void recognizedLinesArePublished() {
        FakeRecognizer recognizer =
                new FakeRecognizer();

        ReferenceListCaptureViewModel viewModel =
                new ReferenceListCaptureViewModel(
                        recognizer
                );

        viewModel.selectImage(
                "content://image",
                DocumentImageSource.CAMERA
        );

        viewModel.processSelectedImage();

        recognizer.completeWith(
                new RecognizedDocument(
                        DocumentImageSource.CAMERA,
                        Collections.singletonList(
                                new RecognizedTextLine(
                                        0,
                                        "MR 1210 A"
                                )
                        ),
                        1000L
                )
        );

        ReferenceListCaptureUiState state =
                requireState(viewModel);

        assertEquals(
                ReferenceListCaptureUiState
                        .Status.TEXT_RECOGNIZED,
                state.getStatus()
        );

        assertEquals(
                "MR 1210 A",
                state.getRecognizedDocument()
                        .getLines()
                        .get(0)
                        .getRawText()
        );
    }

    @Test
    public void emptyRecognitionProducesNoTextState() {
        FakeRecognizer recognizer =
                new FakeRecognizer();

        ReferenceListCaptureViewModel viewModel =
                new ReferenceListCaptureViewModel(
                        recognizer
                );

        viewModel.selectImage(
                "content://image",
                DocumentImageSource.CAMERA
        );

        viewModel.processSelectedImage();

        recognizer.completeWith(
                new RecognizedDocument(
                        DocumentImageSource.CAMERA,
                        Collections.emptyList(),
                        1000L
                )
        );

        assertEquals(
                ReferenceListCaptureUiState
                        .Status.NO_TEXT_FOUND,
                requireState(viewModel).getStatus()
        );
    }

    @Test
    public void secondProcessingRequestIsIgnored() {
        FakeRecognizer recognizer =
                new FakeRecognizer();

        ReferenceListCaptureViewModel viewModel =
                new ReferenceListCaptureViewModel(
                        recognizer
                );

        viewModel.selectImage(
                "content://image",
                DocumentImageSource.CAMERA
        );

        viewModel.processSelectedImage();
        viewModel.processSelectedImage();

        assertEquals(1, recognizer.requestCount);
    }

    @Test
    public void imageOpenErrorIsControlled() {
        FakeRecognizer recognizer =
                new FakeRecognizer();

        ReferenceListCaptureViewModel viewModel =
                new ReferenceListCaptureViewModel(
                        recognizer
                );

        viewModel.selectImage(
                "content://missing",
                DocumentImageSource.PHOTO_PICKER
        );

        viewModel.processSelectedImage();

        recognizer.failOpeningImage();

        assertEquals(
                ReferenceListCaptureUiState
                        .Status.IMAGE_ERROR,
                requireState(viewModel).getStatus()
        );
    }

    @Test
    public void recognitionErrorKeepsImageForRetry() {
        FakeRecognizer recognizer =
                new FakeRecognizer();

        ReferenceListCaptureViewModel viewModel =
                new ReferenceListCaptureViewModel(
                        recognizer
                );

        viewModel.selectImage(
                "content://image",
                DocumentImageSource.PHOTO_PICKER
        );

        viewModel.processSelectedImage();

        recognizer.failRecognition();

        ReferenceListCaptureUiState state =
                requireState(viewModel);

        assertEquals(
                ReferenceListCaptureUiState
                        .Status.RECOGNITION_ERROR,
                state.getStatus()
        );

        assertEquals(
                "content://image",
                state.getImageUri()
        );
    }

    private ReferenceListCaptureUiState requireState(
            ReferenceListCaptureViewModel viewModel
    ) {
        ReferenceListCaptureUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        return state;
    }

    private static final class FakeRecognizer
            implements DocumentTextRecognizer {

        private DocumentRecognitionCallback callback;
        private int requestCount;

        @Override
        public void recognize(
                String imageUri,
                DocumentImageSource sourceType,
                DocumentRecognitionCallback callback
        ) {
            requestCount++;
            this.callback = callback;
        }

        private void completeWith(
                RecognizedDocument document
        ) {
            callback.onSuccess(document);
        }

        private void failOpeningImage() {
            callback.onImageOpenError();
        }

        private void failRecognition() {
            callback.onRecognitionError();
        }

        @Override
        public void close() {

        }
    }
}