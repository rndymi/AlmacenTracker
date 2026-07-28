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
    private final boolean rawTextExpanded;

    private ReferenceListCaptureUiState(
            Status status,
            String imageUri,
            DocumentImageSource imageSource,
            RecognizedDocument recognizedDocument,
            boolean rawTextExpanded
    ) {
        this.status = status;
        this.imageUri = imageUri;
        this.imageSource = imageSource;
        this.recognizedDocument =
                recognizedDocument;
        this.rawTextExpanded =
                rawTextExpanded;
    }

    public static ReferenceListCaptureUiState empty() {
        return create(
                Status.EMPTY,
                null,
                null,
                null,
                false
        );
    }

    public static ReferenceListCaptureUiState
    imageSelected(
            String imageUri,
            DocumentImageSource imageSource
    ) {
        return create(
                Status.IMAGE_SELECTED,
                imageUri,
                imageSource,
                null,
                false
        );
    }

    public static ReferenceListCaptureUiState
    processing(
            String imageUri,
            DocumentImageSource imageSource
    ) {
        return create(
                Status.PROCESSING,
                imageUri,
                imageSource,
                null,
                false
        );
    }

    public static ReferenceListCaptureUiState
    textRecognized(
            String imageUri,
            DocumentImageSource imageSource,
            RecognizedDocument document
    ) {
        return create(
                Status.TEXT_RECOGNIZED,
                imageUri,
                imageSource,
                document,
                false
        );
    }

    public static ReferenceListCaptureUiState
    noTextFound(
            String imageUri,
            DocumentImageSource imageSource
    ) {
        return create(
                Status.NO_TEXT_FOUND,
                imageUri,
                imageSource,
                null,
                false
        );
    }

    public static ReferenceListCaptureUiState
    imageError(
            String imageUri,
            DocumentImageSource imageSource
    ) {
        return create(
                Status.IMAGE_ERROR,
                imageUri,
                imageSource,
                null,
                false
        );
    }

    public static ReferenceListCaptureUiState
    recognitionError(
            String imageUri,
            DocumentImageSource imageSource
    ) {
        return create(
                Status.RECOGNITION_ERROR,
                imageUri,
                imageSource,
                null,
                false
        );
    }

    private static ReferenceListCaptureUiState create(
            Status status,
            String imageUri,
            DocumentImageSource imageSource,
            RecognizedDocument recognizedDocument,
            boolean rawTextExpanded
    ) {
        return new ReferenceListCaptureUiState(
                status,
                imageUri,
                imageSource,
                recognizedDocument,
                rawTextExpanded
        );
    }

    public ReferenceListCaptureUiState
    withRawTextExpanded(boolean expanded) {
        if (!shouldShowRecognizedText()) {
            return this;
        }

        return create(
                status,
                imageUri,
                imageSource,
                recognizedDocument,
                expanded
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

    public boolean isRawTextExpanded() {
        return rawTextExpanded;
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

    public int getRecognizedLineCount() {
        if (!shouldShowRecognizedText()) {
            return 0;
        }

        return recognizedDocument.getLineCount();
    }
}