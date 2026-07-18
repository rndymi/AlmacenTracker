package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.application.port.in.ExportWarehouseItemsUseCase;

import java.util.Objects;
import java.util.function.Supplier;

public final class DataManagementViewModelFactory
        implements ViewModelProvider.Factory {

    private final ExportWarehouseItemsUseCase exportUseCase;
    private final Supplier<String> fileNameSupplier;

    public DataManagementViewModelFactory(
            ExportWarehouseItemsUseCase exportUseCase,
            Supplier<String> fileNameSupplier
    ) {
        this.exportUseCase = Objects.requireNonNull(exportUseCase);
        this.fileNameSupplier = Objects.requireNonNull(fileNameSupplier);
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {
        if (!modelClass.isAssignableFrom(
                DataManagementViewModel.class
        )) {
            throw new IllegalArgumentException(
                    "Unknown ViewModel class: "
                            + modelClass.getName()
            );
        }

        return modelClass.cast(
                new DataManagementViewModel(
                        exportUseCase,
                        fileNameSupplier
                )
        );
    }
}