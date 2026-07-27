package com.rndymi.almacentracker.feature.reference_list.location;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.Objects;

public final class ReferenceListLocationViewModelFactory
        implements ViewModelProvider.Factory {

    private final ReferenceListLocationService service;

    public ReferenceListLocationViewModelFactory(
            ReferenceListLocationService service
    ) {
        this.service = Objects.requireNonNull(
                service,
                "service"
        );
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {
        if (!modelClass.isAssignableFrom(
                ReferenceListLocationViewModel.class
        )) {
            throw new IllegalArgumentException(
                    "Unknown ViewModel class"
            );
        }

        return modelClass.cast(
                new ReferenceListLocationViewModel(
                        service
                )
        );
    }
}