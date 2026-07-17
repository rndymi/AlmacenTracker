package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemDetailUiState;
import com.rndymi.almacentracker.application.port.in.GetWarehouseItemDetailUseCase;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;

import java.util.Objects;

public final class WarehouseItemDetailViewModel
        extends ViewModel {
    private static final String DEFAULT_ERROR_MESSAGE =
            "No se pudo cargar el detalle de la mercadería";
    private final MediatorLiveData<WarehouseItemDetailUiState> uiState =
            new MediatorLiveData<>();
    public WarehouseItemDetailViewModel(
            GetWarehouseItemDetailUseCase useCase,
            long warehouseItemId
    ) {
        Objects.requireNonNull(useCase);

        if (warehouseItemId <= 0L) {
            uiState.setValue(
                    WarehouseItemDetailUiState.invalidId()
            );
            return;
        }

        uiState.setValue(
                WarehouseItemDetailUiState.loading()
        );

        LiveData<WarehouseItemDetailResult> source =
                useCase.observeWarehouseItemDetail(
                        warehouseItemId
                );

        uiState.addSource(source, this::handleResult);
    }

    public LiveData<WarehouseItemDetailUiState> getUiState() {
        return uiState;
    }

    private void handleResult(
            WarehouseItemDetailResult result
    ) {
        if (result instanceof WarehouseItemDetailResult.Found) {
            WarehouseItemDetailResult.Found found =
                    (WarehouseItemDetailResult.Found) result;

            uiState.setValue(
                    WarehouseItemDetailUiState.content(
                            found.getWarehouseItem()
                    )
            );
            return;
        }

        if (result instanceof WarehouseItemDetailResult.NotFound) {
            uiState.setValue(
                    WarehouseItemDetailUiState.notFound()
            );
            return;
        }

        if (result instanceof WarehouseItemDetailResult.InvalidId) {
            uiState.setValue(
                    WarehouseItemDetailUiState.invalidId()
            );
            return;
        }

        uiState.setValue(
                WarehouseItemDetailUiState.error(
                        DEFAULT_ERROR_MESSAGE
                )
        );
    }
}
