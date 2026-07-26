package com.rndymi.almacentracker.feature.reference_list.capture;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.core.document.DocumentTextRecognizer;

import java.util.Objects;

public final class ReferenceListCaptureViewModelFactory
        implements ViewModelProvider.Factory {

    private final DocumentTextRecognizer textRecognizer;

    public ReferenceListCaptureViewModelFactory(
            DocumentTextRecognizer textRecognizer
    ) {
        this.textRecognizer =
                Objects.requireNonNull(
                        textRecognizer,
                        "textRecognizer"
                );
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {
        if (!modelClass.isAssignableFrom(
                ReferenceListCaptureViewModel.class
        )) {
            throw new IllegalArgumentException(
                    "Unknown ViewModel class: "
                            + modelClass.getName()
            );
        }

        return modelClass.cast(
                new ReferenceListCaptureViewModel(
                        textRecognizer
                )
        );
    }
}