package com.rndymi.almacentracker.feature.reference_list.capture;

import com.rndymi.almacentracker.core.document.DocumentImageSource;
import com.rndymi.almacentracker.core.document.RecognizedDocument;

public final class ReferenceListCaptureUiState {

    public enum Status {
        EMPTY,
        IMAGE_SELECTED,
        PROCESSING,
        TEXT_RECOGNIZED,
        NO_TEXT_FOUND,
        IMAGE_ERROR,
        RECOGNITION_ERROR
    }

    private final Status status;
    private final String imageUri;
    private final DocumentImageSource imageSource;
    private final RecognizedDocument recognizedDocument;

    private ReferenceListCaptureUiState(
            Status status,
            String imageUri,
            DocumentImageSource imageSource,
            RecognizedDocument recognizedDocument
    ) {
        this.status = status;
        this.imageUri = imageUri;
        this.imageSource = imageSource;
        this.recognizedDocument =
                recognizedDocument;
    }

    public static ReferenceListCaptureUiState empty() {
        return new ReferenceListCaptureUiState(
                Status.EMPTY,
                null,
                null,
                null
        );
    }

    public static ReferenceListCaptureUiState
    imageSelected(
            String imageUri,
            DocumentImageSource imageSource
    ) {
        return new ReferenceListCaptureUiState(
                Status.IMAGE_SELECTED,
                imageUri,
                imageSource,
                null
        );
    }

    public static ReferenceListCaptureUiState
    processing(
            String imageUri,
            DocumentImageSource imageSource
    ) {
        return new ReferenceListCaptureUiState(
                Status.PROCESSING,
                imageUri,
                imageSource,
                null
        );
    }

    public static ReferenceListCaptureUiState
    textRecognized(
            String imageUri,
            DocumentImageSource imageSource,
            RecognizedDocument document
    ) {
        return new ReferenceListCaptureUiState(
                Status.TEXT_RECOGNIZED,
                imageUri,
                imageSource,
                document
        );
    }

    public static ReferenceListCaptureUiState
    noTextFound(
            String imageUri,
            DocumentImageSource imageSource
    ) {
        return new ReferenceListCaptureUiState(
                Status.NO_TEXT_FOUND,
                imageUri,
                imageSource,
                null
        );
    }

    public static ReferenceListCaptureUiState
    imageError(
            String imageUri,
            DocumentImageSource imageSource
    ) {
        return new ReferenceListCaptureUiState(
                Status.IMAGE_ERROR,
                imageUri,
                imageSource,
                null
        );
    }

    public static ReferenceListCaptureUiState
    recognitionError(
            String imageUri,
            DocumentImageSource imageSource
    ) {
        return new ReferenceListCaptureUiState(
                Status.RECOGNITION_ERROR,
                imageUri,
                imageSource,
                null
        );
    }

    public Status getStatus() {
        return status;
    }

    public String getImageUri() {
        return imageUri;
    }

    public DocumentImageSource getImageSource() {
        return imageSource;
    }

    public RecognizedDocument getRecognizedDocument() {
        return recognizedDocument;
    }

    public boolean hasImage() {
        return imageUri != null
                && imageSource != null;
    }

    public boolean isProcessing() {
        return status == Status.PROCESSING;
    }

    public boolean canChooseImage() {
        return status != Status.PROCESSING;
    }

    public boolean canProcessImage() {
        return hasImage()
                && status != Status.PROCESSING;
    }

    public boolean canRetryRecognition() {
        return hasImage()
                && (
                status == Status.NO_TEXT_FOUND
                        || status
                        == Status.RECOGNITION_ERROR
        );
    }

    public boolean shouldShowPreview() {
        return hasImage()
                && status != Status.IMAGE_ERROR;
    }

    public boolean shouldShowRecognizedText() {
        return status == Status.TEXT_RECOGNIZED
                && recognizedDocument != null
                && recognizedDocument.hasLines();
    }
}