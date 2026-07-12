package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemListUiState;
import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;
import java.util.Objects;

public final class WarehouseItemListViewModel extends ViewModel {
    private static final String DEFAULT_ERROR_MESSAGE =
            "No se pudo cargar la mercancía.";

    private final MediatorLiveData<WarehouseItemListUiState> uiState =
            new MediatorLiveData<>();

    public WarehouseItemListViewModel(
            ObserveWarehouseItemsUseCase observeWarehouseItemsUseCase
    ) {
        Objects.requireNonNull(observeWarehouseItemsUseCase);

        uiState.setValue(WarehouseItemListUiState.loading());

        LiveData<WarehouseItemsResult> source =
                observeWarehouseItemsUseCase.observeWarehouseItems();

        uiState.addSource(source, this::handleResult);
    }

    public LiveData<WarehouseItemListUiState> getUiState() {
        return uiState;
    }

    private void handleResult(WarehouseItemsResult result) {
        if (result == null) {
            uiState.setValue(
                    WarehouseItemListUiState.error(
                            DEFAULT_ERROR_MESSAGE
                    )
            );
            return;
        }

        if (result instanceof WarehouseItemsResult.Success) {
            List<WarehouseItem> items =
                    ((WarehouseItemsResult.Success) result).getItems();

            if (items.isEmpty()) {
                uiState.setValue(
                        WarehouseItemListUiState.empty()
                );
            } else {
                uiState.setValue(
                        WarehouseItemListUiState.content(items)
                );
            }

            return;
        }

        uiState.setValue(
                WarehouseItemListUiState.error(
                        DEFAULT_ERROR_MESSAGE
                )
        );
    }
}