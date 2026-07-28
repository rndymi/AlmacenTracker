package com.rndymi.almacentracker.feature.reference_list.review;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceParser;

import java.util.Objects;

public final class ReferenceListReviewViewModelFactory
        implements ViewModelProvider.Factory {

    private final WarehouseReferenceParser parser;
    private final WarehouseItemRepository repository;

    public ReferenceListReviewViewModelFactory(
            WarehouseReferenceParser parser,
            WarehouseItemRepository repository
    ) {
        this.parser =
                Objects.requireNonNull(
                        parser,
                        "parser"
                );
        this.repository =
                Objects.requireNonNull(
                        repository,
                        "repository"
                );
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {
        if (!modelClass.isAssignableFrom(
                ReferenceListReviewViewModel.class
        )) {
            throw new IllegalArgumentException(
                    "Unknown ViewModel class"
            );
        }

        return modelClass.cast(
                new ReferenceListReviewViewModel(
                        parser,
                        repository
                )
        );
    }
}
