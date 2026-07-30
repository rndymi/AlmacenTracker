package com.rndymi.almacentracker.feature.reference_list.capture;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.core.document.DocumentImage;
import com.rndymi.almacentracker.core.document.DocumentImageProcessingCallback;
import com.rndymi.almacentracker.core.document.DocumentImageProcessor;
import com.rndymi.almacentracker.core.document.DocumentImageSource;
import com.rndymi.almacentracker.core.document.DocumentRecognitionCallback;
import com.rndymi.almacentracker.core.document.DocumentTextRecognizer;
import com.rndymi.almacentracker.core.document.RecognizedDocument;

import java.util.Objects;

public final class ReferenceListCaptureViewModel
        extends ViewModel {

    private final DocumentImageProcessor imageProcessor;
    private final DocumentTextRecognizer textRecognizer;

    private final MutableLiveData
            <ReferenceListCaptureUiState> uiState =
            new MutableLiveData<>(
                    ReferenceListCaptureUiState.empty()
            );

    private long processingRequestId;
    private boolean processing;

    public ReferenceListCaptureViewModel(
            DocumentImageProcessor imageProcessor,
            DocumentTextRecognizer textRecognizer
    ) {
        this.imageProcessor =
                Objects.requireNonNull(
                        imageProcessor,
                        "imageProcessor"
                );

        this.textRecognizer =
                Objects.requireNonNull(
                        textRecognizer,
                        "textRecognizer"
                );
    }

    public LiveData<ReferenceListCaptureUiState>
    getUiState() {
        return uiState;
    }

    public void selectImage(
            String imageUri,
            DocumentImageSource imageSource
    ) {
        if (processing
                || imageUri == null
                || imageUri.trim().isEmpty()
                || imageSource == null) {
            return;
        }

        processingRequestId++;

        uiState.setValue(
                ReferenceListCaptureUiState
                        .imageSelected(
                                imageUri,
                                imageSource
                        )
        );
    }

    public void processSelectedImage() {
        ReferenceListCaptureUiState state =
                uiState.getValue();

        if (processing
                || state == null
                || !state.canProcessImage()) {
            return;
        }

        String imageUri =
                state.getImageUri();

        DocumentImageSource imageSource =
                state.getImageSource();

        processing = true;

        long requestId =
                ++processingRequestId;

        uiState.setValue(
                ReferenceListCaptureUiState
                        .processing(
                                imageUri,
                                imageSource
                        )
        );

        imageProcessor.process(
                imageUri,
                new DocumentImageProcessingCallback() {

                    @Override
                    public void onSuccess(
                            DocumentImage documentImage
                    ) {
                        if (!isCurrentRequest(requestId)) {
                            documentImage.close();
                            return;
                        }

                        recognizeProcessedImage(
                                requestId,
                                imageUri,
                                imageSource,
                                documentImage
                        );
                    }

                    @Override
                    public void onImageOpenError() {
                        completeWithImageError(
                                requestId,
                                imageUri,
                                imageSource
                        );
                    }

                    @Override
                    public void onProcessingError() {
                        completeWithRecognitionError(
                                requestId,
                                imageUri,
                                imageSource
                        );
                    }
                }
        );
    }

    private void recognizeProcessedImage(
            long requestId,
            String imageUri,
            DocumentImageSource imageSource,
            DocumentImage documentImage
    ) {
        textRecognizer.recognize(
                documentImage,
                imageSource,
                new DocumentRecognitionCallback() {

                    @Override
                    public void onSuccess(
                            RecognizedDocument document
                    ) {
                        if (!completeRequest(requestId)) {
                            return;
                        }

                        if (document == null
                                || !document.hasLines()) {
                            uiState.postValue(
                                    ReferenceListCaptureUiState
                                            .noTextFound(
                                                    imageUri,
                                                    imageSource
                                            )
                            );
                            return;
                        }

                        uiState.postValue(
                                ReferenceListCaptureUiState
                                        .textRecognized(
                                                imageUri,
                                                imageSource,
                                                document
                                        )
                        );
                    }

                    @Override
                    public void onImageOpenError() {
                        completeWithImageError(
                                requestId,
                                imageUri,
                                imageSource
                        );
                    }

                    @Override
                    public void onRecognitionError() {
                        completeWithRecognitionError(
                                requestId,
                                imageUri,
                                imageSource
                        );
                    }
                }
        );
    }

    private void completeWithImageError(
            long requestId,
            String imageUri,
            DocumentImageSource imageSource
    ) {
        if (!completeRequest(requestId)) {
            return;
        }

        uiState.postValue(
                ReferenceListCaptureUiState
                        .imageError(
                                imageUri,
                                imageSource
                        )
        );
    }

    private void completeWithRecognitionError(
            long requestId,
            String imageUri,
            DocumentImageSource imageSource
    ) {
        if (!completeRequest(requestId)) {
            return;
        }

        uiState.postValue(
                ReferenceListCaptureUiState
                        .recognitionError(
                                imageUri,
                                imageSource
                        )
        );
    }

    public void toggleRawText() {
        ReferenceListCaptureUiState current =
                uiState.getValue();

        if (processing
                || current == null
                || !current.shouldShowRecognizedText()) {
            return;
        }

        uiState.setValue(
                current.withRawTextExpanded(
                        !current.isRawTextExpanded()
                )
        );
    }

    public void clearImage() {
        if (processing) {
            return;
        }

        processingRequestId++;

        uiState.setValue(
                ReferenceListCaptureUiState.empty()
        );
    }

    private synchronized boolean
    isCurrentRequest(long requestId) {
        return processing
                && requestId
                == processingRequestId;
    }

    private synchronized boolean
    completeRequest(long requestId) {
        if (!processing
                || requestId
                != processingRequestId) {
            return false;
        }

        processing = false;
        return true;
    }

    @Override
    protected void onCleared() {
        processingRequestId++;
        processing = false;

        imageProcessor.close();
        textRecognizer.close();
    }
}