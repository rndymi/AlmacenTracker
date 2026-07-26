package com.rndymi.almacentracker.app.di;

import android.content.Context;
import android.graphics.Bitmap;

import com.rndymi.almacentracker.core.document.DocumentImageLoader;
import com.rndymi.almacentracker.core.document.DocumentTextRecognizer;
import com.rndymi.almacentracker.data.document.AndroidDocumentImageLoader;
import com.rndymi.almacentracker.data.document.MlKitDocumentTextRecognizer;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceParser;
import com.rndymi.almacentracker.feature.reference_list.capture.ReferenceListCaptureViewModelFactory;
import com.rndymi.almacentracker.feature.reference_list.review.ReferenceListReviewViewModelFactory;

import java.util.Objects;

public final class ReferenceListModule {

    private final Context applicationContext;
    private final DocumentImageLoader<Bitmap> imageLoader;

    public ReferenceListModule(
            Context context
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
    }

    public ReferenceListCaptureViewModelFactory
    provideReferenceListCaptureViewModelFactory() {
        DocumentTextRecognizer recognizer =
                new MlKitDocumentTextRecognizer(
                        applicationContext
                );

        return new ReferenceListCaptureViewModelFactory(
                recognizer
        );
    }

    public ReferenceListReviewViewModelFactory
    provideReferenceListReviewViewModelFactory() {
        return new ReferenceListReviewViewModelFactory(
                new WarehouseReferenceParser()
        );
    }

    public DocumentImageLoader<Bitmap>
    provideDocumentImageLoader() {
        return imageLoader;
    }
}