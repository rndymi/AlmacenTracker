package com.rndymi.almacentracker.feature.reference_list.review;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.reference.DocumentReferenceDataParser;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceParser;

import java.util.Objects;

public final class ReferenceListReviewViewModelFactory
        implements ViewModelProvider.Factory {

    private final WarehouseReferenceParser parser;
    private final WarehouseItemRepository repository;
    private final DocumentReferenceDataParser
            documentReferenceDataParser;

    public ReferenceListReviewViewModelFactory(
            WarehouseReferenceParser parser,
            WarehouseItemRepository repository,
            DocumentReferenceDataParser documentReferenceDataParser
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
        this.documentReferenceDataParser =
                Objects.requireNonNull(
                        documentReferenceDataParser,
                        "documentReferenceDataParser"
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
                        repository,
                        documentReferenceDataParser
                )
        );
    }
}
