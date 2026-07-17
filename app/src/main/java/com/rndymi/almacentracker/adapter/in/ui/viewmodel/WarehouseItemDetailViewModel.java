package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.adapter.in.ui.state.UiEvent;
import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemDetailUiState;
import com.rndymi.almacentracker.application.port.in.DeleteWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.in.GetWarehouseItemDetailUseCase;
import com.rndymi.almacentracker.application.result.DeleteWarehouseItemResult;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Objects;

public final class WarehouseItemDetailViewModel
        extends ViewModel {

    private static final String DEFAULT_ERROR_MESSAGE =
            "No se pudo cargar el detalle de la mercancía.";

    private static final String DELETE_ERROR_MESSAGE =
            "No se pudo eliminar la mercancía.";

    private final long warehouseItemId;
    private final DeleteWarehouseItemUseCase
            deleteWarehouseItemUseCase;

    private final MediatorLiveData<WarehouseItemDetailUiState>
            uiState = new MediatorLiveData<>();

    private final MutableLiveData<UiEvent<Boolean>>
            deletionSuccess = new MutableLiveData<>();

    private WarehouseItem currentWarehouseItem;
    private boolean deletionInProgress;
    private boolean deletionCompleted;

    public WarehouseItemDetailViewModel(
            GetWarehouseItemDetailUseCase getDetailUseCase,
            DeleteWarehouseItemUseCase deleteWarehouseItemUseCase,
            long warehouseItemId
    ) {
        Objects.requireNonNull(getDetailUseCase);
        this.deleteWarehouseItemUseCase =
                Objects.requireNonNull(
                        deleteWarehouseItemUseCase
                );

        this.warehouseItemId = warehouseItemId;

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
                getDetailUseCase
                        .observeWarehouseItemDetail(
                                warehouseItemId
                        );

        uiState.addSource(
                source,
                this::handleDetailResult
        );
    }

    public LiveData<WarehouseItemDetailUiState> getUiState() {
        return uiState;
    }

    public LiveData<UiEvent<Boolean>> getDeletionSuccess() {
        return deletionSuccess;
    }

    public void deleteWarehouseItem() {
        if (warehouseItemId <= 0L
                || currentWarehouseItem == null
                || deletionInProgress
                || deletionCompleted) {
            return;
        }

        deletionInProgress = true;

        uiState.setValue(
                WarehouseItemDetailUiState.deleting(
                        currentWarehouseItem
                )
        );

        deleteWarehouseItemUseCase.deleteWarehouseItem(
                warehouseItemId,
                this::handleDeleteResult
        );
    }

    private void handleDetailResult(
            WarehouseItemDetailResult result
    ) {
        if (deletionCompleted) {
            return;
        }

        if (result instanceof WarehouseItemDetailResult.Found) {
            WarehouseItemDetailResult.Found found =
                    (WarehouseItemDetailResult.Found) result;

            currentWarehouseItem =
                    found.getWarehouseItem();

            if (!deletionInProgress) {
                uiState.setValue(
                        WarehouseItemDetailUiState.content(
                                currentWarehouseItem
                        )
                );
            }

            return;
        }

        if (result instanceof WarehouseItemDetailResult.NotFound) {
            currentWarehouseItem = null;

            if (!deletionInProgress) {
                uiState.setValue(
                        WarehouseItemDetailUiState.notFound()
                );
            }

            return;
        }

        if (result instanceof WarehouseItemDetailResult.InvalidId) {
            currentWarehouseItem = null;
            uiState.setValue(
                    WarehouseItemDetailUiState.invalidId()
            );
            return;
        }

        currentWarehouseItem = null;
        uiState.setValue(
                WarehouseItemDetailUiState.error(
                        DEFAULT_ERROR_MESSAGE
                )
        );
    }

    private void handleDeleteResult(
            DeleteWarehouseItemResult result
    ) {
        deletionInProgress = false;

        switch (result.getStatus()) {
            case SUCCESS:
                deletionCompleted = true;

                deletionSuccess.postValue(
                        new UiEvent<>(true)
                );
                break;

            case INVALID_ID:
                currentWarehouseItem = null;

                uiState.postValue(
                        WarehouseItemDetailUiState.invalidId()
                );
                break;

            case NOT_FOUND:
                currentWarehouseItem = null;

                uiState.postValue(
                        WarehouseItemDetailUiState.notFound()
                );
                break;

            case PERSISTENCE_ERROR:
            default:
                if (currentWarehouseItem == null) {
                    uiState.postValue(
                            WarehouseItemDetailUiState.notFound()
                    );
                    return;
                }

                uiState.postValue(
                        WarehouseItemDetailUiState.deleteError(
                                currentWarehouseItem,
                                DELETE_ERROR_MESSAGE
                        )
                );
                break;
        }
    }
}
