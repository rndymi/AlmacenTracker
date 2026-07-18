package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.adapter.in.ui.state.DataManagementUiState;
import com.rndymi.almacentracker.adapter.in.ui.state.UiEvent;
import com.rndymi.almacentracker.application.port.in.ExportWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.result.ExportWarehouseItemsResult;

import java.util.Objects;
import java.util.function.Supplier;

public final class DataManagementViewModel extends ViewModel {

    private static final String EMPTY_MESSAGE =
            "No hay mercancía para exportar.";

    private static final String INVALID_DESTINATION_MESSAGE =
            "No se pudo acceder al destino seleccionado.";

    private static final String READ_ERROR_MESSAGE =
            "No se pudo leer la mercancía almacenada.";

    private static final String SERIALIZATION_ERROR_MESSAGE =
            "No se pudo generar el archivo CSV.";

    private static final String WRITE_ERROR_MESSAGE =
            "No se pudo escribir el archivo CSV.";

    private static final String UNKNOWN_ERROR_MESSAGE =
            "No se pudo exportar la mercancía.";

    private final ExportWarehouseItemsUseCase exportUseCase;
    private final Supplier<String> fileNameSupplier;

    private final MutableLiveData<DataManagementUiState> uiState =
            new MutableLiveData<>(DataManagementUiState.idle());

    private final MutableLiveData<UiEvent<String>> destinationRequest =
            new MutableLiveData<>();

    private final MutableLiveData<UiEvent<Integer>> exportSuccess =
            new MutableLiveData<>();

    private boolean selectorRequested;
    private boolean exportInProgress;

    public DataManagementViewModel(
            ExportWarehouseItemsUseCase exportUseCase,
            Supplier<String> fileNameSupplier
    ) {
        this.exportUseCase = Objects.requireNonNull(exportUseCase);
        this.fileNameSupplier = Objects.requireNonNull(fileNameSupplier);
    }

    public LiveData<DataManagementUiState> getUiState() {
        return uiState;
    }

    public LiveData<UiEvent<String>> getDestinationRequest() {
        return destinationRequest;
    }

    public LiveData<UiEvent<Integer>> getExportSuccess() {
        return exportSuccess;
    }

    public void requestExportDestination() {
        if (selectorRequested || exportInProgress) {
            return;
        }

        selectorRequested = true;
        uiState.setValue(
                DataManagementUiState.selectingDestination()
        );

        destinationRequest.setValue(
                new UiEvent<>(fileNameSupplier.get())
        );
    }

    public void onDestinationSelected(String destinationReference) {
        if (!selectorRequested || exportInProgress) {
            return;
        }

        selectorRequested = false;

        if (destinationReference == null
                || destinationReference.trim().isEmpty()) {
            uiState.setValue(DataManagementUiState.idle());
            return;
        }

        exportInProgress = true;
        uiState.setValue(DataManagementUiState.exporting());

        exportUseCase.exportWarehouseItems(
                destinationReference,
                this::handleExportResult
        );
    }

    public void retry() {
        if (!exportInProgress && !selectorRequested) {
            uiState.setValue(DataManagementUiState.idle());
        }
    }

    private void handleExportResult(
            ExportWarehouseItemsResult result
    ) {
        exportInProgress = false;

        switch (result.getStatus()) {
            case SUCCESS:
                uiState.postValue(DataManagementUiState.idle());
                exportSuccess.postValue(
                        new UiEvent<>(result.getExportedCount())
                );
                break;

            case EMPTY_DATABASE:
                uiState.postValue(
                        DataManagementUiState.emptyDatabase(
                                EMPTY_MESSAGE
                        )
                );
                break;

            case INVALID_DESTINATION:
                postError(INVALID_DESTINATION_MESSAGE);
                break;

            case READ_ERROR:
                postError(READ_ERROR_MESSAGE);
                break;

            case SERIALIZATION_ERROR:
                postError(SERIALIZATION_ERROR_MESSAGE);
                break;

            case WRITE_ERROR:
                postError(WRITE_ERROR_MESSAGE);
                break;

            case UNKNOWN_ERROR:
            default:
                postError(UNKNOWN_ERROR_MESSAGE);
                break;
        }
    }

    private void postError(String message) {
        uiState.postValue(DataManagementUiState.error(message));
    }
}