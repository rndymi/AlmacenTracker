package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.application.port.in.ExportWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.ShareWarehouseItemsUseCase;

import java.util.Objects;
import java.util.function.Supplier;

public final class DataManagementViewModelFactory
        implements ViewModelProvider.Factory {

    private final ExportWarehouseItemsUseCase exportUseCase;
    private final ShareWarehouseItemsUseCase shareUseCase;
    private final Supplier<String> exportFileNameSupplier;

    public DataManagementViewModelFactory(
            ExportWarehouseItemsUseCase exportUseCase,
            ShareWarehouseItemsUseCase shareUseCase,
            Supplier<String> exportFileNameSupplier
    ) {
        this.exportUseCase =
                Objects.requireNonNull(exportUseCase);

        this.shareUseCase =
                Objects.requireNonNull(shareUseCase);

        this.exportFileNameSupplier =
                Objects.requireNonNull(
                        exportFileNameSupplier
                );
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
                        shareUseCase,
                        exportFileNameSupplier
                )
        );
    }
}