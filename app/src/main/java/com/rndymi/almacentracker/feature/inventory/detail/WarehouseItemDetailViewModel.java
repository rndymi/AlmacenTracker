package com.rndymi.almacentracker.feature.inventory.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.data.repository.WarehouseItemDetailResult;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.feature.inventory.common.WarehouseItemDeleteResult;
import com.rndymi.almacentracker.feature.inventory.common.WarehouseItemDeleteService;

import java.util.Objects;

public final class WarehouseItemDetailViewModel
        extends ViewModel {

    private static final String DEFAULT_ERROR_MESSAGE =
            "No se pudo cargar el detalle de la mercancía.";

    private static final String DELETE_ERROR_MESSAGE =
            "No se pudo eliminar la mercancía.";

    private final long warehouseItemId;
    private final WarehouseItemDeleteService deleteService;

    private final MediatorLiveData<WarehouseItemDetailUiState>
            uiState = new MediatorLiveData<>();

    private final MutableLiveData<UiEvent<Boolean>>
            deletionSuccess = new MutableLiveData<>();

    private WarehouseItem currentWarehouseItem;
    private boolean deletionInProgress;
    private boolean deletionCompleted;

    public WarehouseItemDetailViewModel(
            WarehouseItemRepository repository,
            WarehouseItemDeleteService deleteService,
            long warehouseItemId
    ) {
        Objects.requireNonNull(repository);
        this.deleteService = Objects.requireNonNull(deleteService);

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
                repository.observeById(warehouseItemId);

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

        deleteService.delete(
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
            WarehouseItemDeleteResult result
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
