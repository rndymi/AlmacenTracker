package com.rndymi.almacentracker.app.di;

import android.content.Context;
import android.graphics.Bitmap;

import com.rndymi.almacentracker.core.document.DocumentImageLoader;
import com.rndymi.almacentracker.core.document.DocumentImageProcessor;
import com.rndymi.almacentracker.core.document.DocumentTextRecognizer;
import com.rndymi.almacentracker.data.document.AndroidDocumentImageLoader;
import com.rndymi.almacentracker.data.document.AndroidDocumentImageProcessor;
import com.rndymi.almacentracker.data.document.DocumentLineReconstructor;
import com.rndymi.almacentracker.data.document.MlKitDocumentTextRecognizer;
import com.rndymi.almacentracker.data.document.onnx.PaddleOcrRuntimeProvider;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.reference.DocumentReferenceDataParser;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceParser;
import com.rndymi.almacentracker.feature.reference_list.capture.ReferenceListCaptureViewModelFactory;
import com.rndymi.almacentracker.feature.reference_list.review.ReferenceListReviewViewModelFactory;

import java.util.Objects;

public final class ReferenceListModule {

    private final Context applicationContext;
    private final DocumentImageLoader<Bitmap> imageLoader;
    private final WarehouseItemRepository repository;
    private final PaddleOcrRuntimeProvider paddleOcrRuntimeProvider;

    public ReferenceListModule(
            Context context,
            WarehouseItemRepository repository,
            PaddleOcrRuntimeProvider paddleOcrRuntimeProvider
    ) {
        applicationContext =
                Objects.requireNonNull(
                        context,
                        "context"
                ).getApplicationContext();

        imageLoader =
                new AndroidDocumentImageLoader(
                        applicationContext
                );

        this.repository =
                Objects.requireNonNull(
                        repository,
                        "repository"
                );

        this.paddleOcrRuntimeProvider =
                Objects.requireNonNull(
                        paddleOcrRuntimeProvider,
                        "paddleOcrRuntimeProvider"
                );
    }

    public PaddleOcrRuntimeProvider
    providePaddleOcrRuntimeProvider() {
        return paddleOcrRuntimeProvider;
    }

    public ReferenceListCaptureViewModelFactory
    provideReferenceListCaptureViewModelFactory() {
        DocumentImageProcessor imageProcessor =
                new AndroidDocumentImageProcessor(
                        applicationContext
                );

        DocumentTextRecognizer recognizer =
                new MlKitDocumentTextRecognizer(
                        new DocumentLineReconstructor()
                );

        return new ReferenceListCaptureViewModelFactory(
                imageProcessor,
                recognizer
        );
    }

    public ReferenceListReviewViewModelFactory
    provideReferenceListReviewViewModelFactory() {
        return new ReferenceListReviewViewModelFactory(
                new WarehouseReferenceParser(),
                repository,
                new DocumentReferenceDataParser()
        );
    }

    public DocumentImageLoader<Bitmap>
    provideDocumentImageLoader() {
        return imageLoader;
    }
}
