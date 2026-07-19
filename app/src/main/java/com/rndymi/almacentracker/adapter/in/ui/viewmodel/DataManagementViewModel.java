package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.adapter.in.ui.state.DataManagementUiState;
import com.rndymi.almacentracker.adapter.in.ui.state.UiEvent;
import com.rndymi.almacentracker.application.port.in.CreateWarehouseBackupUseCase;
import com.rndymi.almacentracker.application.port.in.ExportWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.ImportWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.ShareWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.result.CreateWarehouseBackupResult;
import com.rndymi.almacentracker.application.result.ExportWarehouseItemsResult;
import com.rndymi.almacentracker.application.result.ImportWarehouseItemsResult;
import com.rndymi.almacentracker.application.result.ShareWarehouseItemsResult;
import com.rndymi.almacentracker.application.result.ShareableCsvFile;

import java.util.Objects;
import java.util.function.Supplier;

public final class DataManagementViewModel
        extends ViewModel {

    private static final String EXPORT_EMPTY_MESSAGE =
            "No hay mercancía para exportar.";

    private static final String SHARE_EMPTY_MESSAGE =
            "No hay mercancía para compartir.";

    private static final String INVALID_DESTINATION_MESSAGE =
            "No se pudo acceder al destino seleccionado.";

    private static final String READ_ERROR_MESSAGE =
            "No se pudo leer la mercancía almacenada.";

    private static final String SERIALIZATION_ERROR_MESSAGE =
            "No se pudo generar el archivo CSV.";

    private static final String WRITE_ERROR_MESSAGE =
            "No se pudo escribir el archivo CSV.";

    private static final String TEMP_FILE_ERROR_MESSAGE =
            "No se pudo preparar el archivo temporal.";

    private static final String FILE_PROVIDER_ERROR_MESSAGE =
            "No se pudo compartir el archivo de forma segura.";

    private static final String NO_RECEIVER_MESSAGE =
            "No hay ninguna aplicación disponible "
                    + "para compartir el archivo CSV.";

    private static final String EXPORT_UNKNOWN_ERROR_MESSAGE =
            "No se pudo exportar la mercancía.";

    private static final String SHARE_UNKNOWN_ERROR_MESSAGE =
            "No se pudo preparar la mercancía para compartir.";

    private static final String INVALID_IMPORT_SOURCE_MESSAGE =
            "No se pudo acceder al archivo seleccionado.";

    private static final String INVALID_IMPORT_FORMAT_MESSAGE =
            "El archivo no utiliza el formato CSV esperado.";

    private static final String IMPORT_READ_ERROR_MESSAGE =
            "No se pudo leer el archivo CSV.";

    private static final String IMPORT_PERSISTENCE_ERROR_MESSAGE =
            "No se pudo guardar la mercancía importada.";

    private static final String IMPORT_UNKNOWN_ERROR_MESSAGE =
            "No se pudo importar la mercancía.";

    private static final String BACKUP_INVALID_DATA_MESSAGE =
            "La mercancía contiene fechas no válidas para crear la copia.";

    private static final String BACKUP_SERIALIZATION_ERROR_MESSAGE =
            "No se pudo generar el archivo de copia de seguridad.";

    private static final String BACKUP_WRITE_ERROR_MESSAGE =
            "No se pudo escribir la copia de seguridad.";

    private static final String BACKUP_UNKNOWN_ERROR_MESSAGE =
            "No se pudo crear la copia de seguridad.";

    private final ExportWarehouseItemsUseCase exportUseCase;
    private final ShareWarehouseItemsUseCase shareUseCase;
    private final ImportWarehouseItemsUseCase importUseCase;
    private final CreateWarehouseBackupUseCase createWarehouseBackupUseCase;
    private final Supplier<String> exportFileNameSupplier;
    private final Supplier<String> backupFileNameSupplier;

    private final MutableLiveData<DataManagementUiState>
            uiState = new MutableLiveData<>(
            DataManagementUiState.idle()
    );

    private final MutableLiveData<UiEvent<String>>
            destinationRequest = new MutableLiveData<>();

    private final MutableLiveData<UiEvent<Integer>>
            exportSuccess = new MutableLiveData<>();

    private final MutableLiveData<UiEvent<ShareableCsvFile>>
            shareFileReady = new MutableLiveData<>();

    private final MutableLiveData<UiEvent<Boolean>>
            sourceRequest = new MutableLiveData<>();

    private final MutableLiveData<
            UiEvent<ImportWarehouseItemsResult>>
            importCompleted = new MutableLiveData<>();

    private final MutableLiveData<UiEvent<String>>
            backupDestinationRequest =
            new MutableLiveData<>();

    private final MutableLiveData<UiEvent<Integer>>
            backupSuccess =
            new MutableLiveData<>();

    private boolean selectorRequested;
    private boolean operationInProgress;

    public DataManagementViewModel(
            ExportWarehouseItemsUseCase exportUseCase,
            ShareWarehouseItemsUseCase shareUseCase,
            ImportWarehouseItemsUseCase importUseCase,
            CreateWarehouseBackupUseCase
                    createWarehouseBackupUseCase,
            Supplier<String> exportFileNameSupplier,
            Supplier<String> backupFileNameSupplier
    ) {
        this.exportUseCase =
                Objects.requireNonNull(exportUseCase);

        this.shareUseCase =
                Objects.requireNonNull(shareUseCase);

        this.importUseCase =
                Objects.requireNonNull(importUseCase);

        this.createWarehouseBackupUseCase =
                Objects.requireNonNull(
                        createWarehouseBackupUseCase
                );

        this.exportFileNameSupplier =
                Objects.requireNonNull(
                        exportFileNameSupplier
                );

        this.backupFileNameSupplier =
                Objects.requireNonNull(
                        backupFileNameSupplier
                );
    }

    public LiveData<DataManagementUiState> getUiState() {
        return uiState;
    }

    public LiveData<UiEvent<String>>
    getDestinationRequest() {
        return destinationRequest;
    }

    public LiveData<UiEvent<Integer>> getExportSuccess() {
        return exportSuccess;
    }

    public LiveData<UiEvent<ShareableCsvFile>>
    getShareFileReady() {
        return shareFileReady;
    }

    public LiveData<UiEvent<Boolean>>
    getSourceRequest() {
        return sourceRequest;
    }

    public LiveData<UiEvent<ImportWarehouseItemsResult>>
    getImportCompleted() {
        return importCompleted;
    }

    public LiveData<UiEvent<String>>
    getBackupDestinationRequest() {
        return backupDestinationRequest;
    }

    public LiveData<UiEvent<Integer>>
    getBackupSuccess() {
        return backupSuccess;
    }

    public void requestExportDestination() {
        if (isBusy()) {
            return;
        }

        final String suggestedFileName;

        try {
            suggestedFileName =
                    exportFileNameSupplier.get();
        } catch (RuntimeException exception) {
            uiState.setValue(
                    DataManagementUiState.error(
                            EXPORT_UNKNOWN_ERROR_MESSAGE
                    )
            );
            return;
        }

        selectorRequested = true;

        uiState.setValue(
                DataManagementUiState
                        .selectingDestination()
        );

        destinationRequest.setValue(
                new UiEvent<>(suggestedFileName)
        );
    }

    public void requestImportSource() {
        if (isBusy()) {
            return;
        }

        selectorRequested = true;

        uiState.setValue(
                DataManagementUiState.selectingSource()
        );

        sourceRequest.setValue(
                new UiEvent<>(true)
        );
    }

    public void onImportSourceSelected(
            String sourceReference
    ) {
        if (!selectorRequested || operationInProgress) {
            return;
        }

        selectorRequested = false;

        if (sourceReference == null
                || sourceReference.trim().isEmpty()) {
            uiState.setValue(
                    DataManagementUiState.idle()
            );
            return;
        }

        operationInProgress = true;

        uiState.setValue(
                DataManagementUiState.importing()
        );

        importUseCase.importWarehouseItems(
                sourceReference,
                this::handleImportResult
        );
    }

    public void onDestinationSelected(
            String destinationReference
    ) {
        if (!selectorRequested || operationInProgress) {
            return;
        }

        selectorRequested = false;

        if (destinationReference == null
                || destinationReference.trim().isEmpty()) {
            uiState.setValue(
                    DataManagementUiState.idle()
            );
            return;
        }

        operationInProgress = true;

        uiState.setValue(
                DataManagementUiState.exporting()
        );

        exportUseCase.exportWarehouseItems(
                destinationReference,
                this::handleExportResult
        );
    }

    public void shareWarehouseItems() {
        if (isBusy()) {
            return;
        }

        operationInProgress = true;

        uiState.setValue(
                DataManagementUiState.preparingShare()
        );

        shareUseCase.prepareWarehouseItemsForSharing(
                this::handleShareResult
        );
    }

    public void onShareChooserLaunched() {
        operationInProgress = false;

        uiState.setValue(
                DataManagementUiState.idle()
        );
    }

    public void onNoShareApplicationAvailable() {
        operationInProgress = false;

        uiState.setValue(
                DataManagementUiState.error(
                        NO_RECEIVER_MESSAGE
                )
        );
    }

    public void onInvalidShareReference() {
        operationInProgress = false;

        uiState.setValue(
                DataManagementUiState.error(
                        FILE_PROVIDER_ERROR_MESSAGE
                )
        );
    }

    public void retry() {
        if (!isBusy()) {
            uiState.setValue(
                    DataManagementUiState.idle()
            );
        }
    }

    private boolean isBusy() {
        return selectorRequested || operationInProgress;
    }

    private void handleExportResult(
            ExportWarehouseItemsResult result
    ) {
        operationInProgress = false;

        switch (result.getStatus()) {
            case SUCCESS:
                uiState.postValue(
                        DataManagementUiState.idle()
                );

                exportSuccess.postValue(
                        new UiEvent<>(
                                result.getExportedCount()
                        )
                );
                break;

            case EMPTY_DATABASE:
                postEmpty(EXPORT_EMPTY_MESSAGE);
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
                postError(
                        EXPORT_UNKNOWN_ERROR_MESSAGE
                );
                break;
        }
    }

    private void handleShareResult(
            ShareWarehouseItemsResult result
    ) {
        switch (result.getStatus()) {
            case SUCCESS:
                ShareableCsvFile shareableFile =
                        result.getShareableFile();

                if (shareableFile == null) {
                    operationInProgress = false;
                    postError(
                            SHARE_UNKNOWN_ERROR_MESSAGE
                    );
                    return;
                }

                shareFileReady.postValue(
                        new UiEvent<>(shareableFile)
                );
                break;

            case EMPTY_DATABASE:
                operationInProgress = false;
                postEmpty(SHARE_EMPTY_MESSAGE);
                break;

            case READ_ERROR:
                operationInProgress = false;
                postError(READ_ERROR_MESSAGE);
                break;

            case SERIALIZATION_ERROR:
                operationInProgress = false;
                postError(SERIALIZATION_ERROR_MESSAGE);
                break;

            case TEMP_FILE_ERROR:
                operationInProgress = false;
                postError(TEMP_FILE_ERROR_MESSAGE);
                break;

            case FILE_PROVIDER_ERROR:
                operationInProgress = false;
                postError(FILE_PROVIDER_ERROR_MESSAGE);
                break;

            case UNKNOWN_ERROR:
            default:
                operationInProgress = false;
                postError(
                        SHARE_UNKNOWN_ERROR_MESSAGE
                );
                break;
        }
    }

    private void postEmpty(String message) {
        uiState.postValue(
                DataManagementUiState.empty(
                        message
                )
        );
    }

    private void postError(String message) {
        uiState.postValue(
                DataManagementUiState.error(message)
        );
    }

    private void handleImportResult(
            ImportWarehouseItemsResult result
    ) {
        operationInProgress = false;

        switch (result.getStatus()) {
            case SUCCESS:
            case PARTIAL_SUCCESS:
            case NO_VALID_ROWS:
                uiState.postValue(
                        DataManagementUiState.idle()
                );

                importCompleted.postValue(
                        new UiEvent<>(result)
                );
                break;

            case INVALID_SOURCE:
                postError(
                        INVALID_IMPORT_SOURCE_MESSAGE
                );
                break;

            case INVALID_FORMAT:
                postError(
                        INVALID_IMPORT_FORMAT_MESSAGE
                );
                break;

            case READ_ERROR:
                postError(
                        IMPORT_READ_ERROR_MESSAGE
                );
                break;

            case PERSISTENCE_ERROR:
                postError(
                        IMPORT_PERSISTENCE_ERROR_MESSAGE
                );
                break;

            case UNKNOWN_ERROR:
            default:
                postError(
                        IMPORT_UNKNOWN_ERROR_MESSAGE
                );
                break;
        }
    }

    public void requestBackupDestination() {
        if (isBusy()) {
            return;
        }

        final String suggestedFileName;

        try {
            suggestedFileName =
                    backupFileNameSupplier.get();
        } catch (RuntimeException exception) {
            uiState.setValue(
                    DataManagementUiState.error(
                            BACKUP_UNKNOWN_ERROR_MESSAGE
                    )
            );
            return;
        }

        selectorRequested = true;

        uiState.setValue(
                DataManagementUiState
                        .selectingBackupDestination()
        );

        backupDestinationRequest.setValue(
                new UiEvent<>(suggestedFileName)
        );
    }

    public void onBackupDestinationSelected(
            String destinationReference
    ) {
        if (!selectorRequested || operationInProgress) {
            return;
        }

        selectorRequested = false;

        if (destinationReference == null
                || destinationReference.trim().isEmpty()) {
            uiState.setValue(
                    DataManagementUiState.idle()
            );
            return;
        }

        operationInProgress = true;

        uiState.setValue(
                DataManagementUiState.creatingBackup()
        );

        createWarehouseBackupUseCase.createBackup(
                destinationReference,
                this::handleBackupResult
        );
    }

    private void handleBackupResult(
            CreateWarehouseBackupResult result
    ) {
        operationInProgress = false;

        switch (result.getStatus()) {
            case SUCCESS:
                uiState.postValue(
                        DataManagementUiState.idle()
                );

                backupSuccess.postValue(
                        new UiEvent<>(
                                result.getBackedUpCount()
                        )
                );
                break;

            case INVALID_DESTINATION:
                postError(INVALID_DESTINATION_MESSAGE);
                break;

            case READ_ERROR:
                postError(READ_ERROR_MESSAGE);
                break;

            case INVALID_DATA:
                postError(BACKUP_INVALID_DATA_MESSAGE);
                break;

            case SERIALIZATION_ERROR:
                postError(
                        BACKUP_SERIALIZATION_ERROR_MESSAGE
                );
                break;

            case WRITE_ERROR:
                postError(BACKUP_WRITE_ERROR_MESSAGE);
                break;

            case UNKNOWN_ERROR:
            default:
                postError(BACKUP_UNKNOWN_ERROR_MESSAGE);
                break;
        }
    }
}
