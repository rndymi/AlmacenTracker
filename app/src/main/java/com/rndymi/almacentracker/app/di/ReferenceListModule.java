package com.rndymi.almacentracker.app.di;

import android.content.Context;

import com.rndymi.almacentracker.core.document.DocumentTextRecognizer;
import com.rndymi.almacentracker.data.document.MlKitDocumentTextRecognizer;
import com.rndymi.almacentracker.feature.reference_list.capture.ReferenceListCaptureViewModelFactory;

import java.util.Objects;

public final class ReferenceListModule {

    private final Context applicationContext;

    public ReferenceListModule(
            Context context
    ) {
        applicationContext =
                Objects.requireNonNull(
                        context,
                        "context"
                ).getApplicationContext();
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
}