package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.adapter.in.ui.state.UiEvent;
import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemFormUiState;
import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemCommand;
import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.result.CreateWarehouseItemResult;

import java.util.Objects;

public final class WarehouseItemFormViewModel
        extends ViewModel {

    private static final String REQUIRED_FIELD_ERROR =
            "Este campo es obligatorio.";

    private static final String DUPLICATE_ERROR =
            "Ya existe una mercancía con esta categoría y código.";

    private static final String PERSISTENCE_ERROR =
            "No se pudo registrar la mercancía.";

    private final CreateWarehouseItemUseCase
            createWarehouseItemUseCase;

    private final MutableLiveData<WarehouseItemFormUiState>
            uiState = new MutableLiveData<>(
            WarehouseItemFormUiState.initial()
    );

    private final MutableLiveData<UiEvent<Long>>
            creationSuccess = new MutableLiveData<>();

    public WarehouseItemFormViewModel(
            CreateWarehouseItemUseCase
                    createWarehouseItemUseCase
    ) {
        this.createWarehouseItemUseCase =
                Objects.requireNonNull(
                        createWarehouseItemUseCase
                );
    }

    public LiveData<WarehouseItemFormUiState> getUiState() {
        return uiState;
    }

    public LiveData<UiEvent<Long>> getCreationSuccess() {
        return creationSuccess;
    }

    public void onCategoryChanged(String value) {
        WarehouseItemFormUiState current =
                requireState();

        uiState.setValue(
                copy(
                        current,
                        value,
                        current.getCode(),
                        current.getSite(),
                        current.getPosition(),
                        current.getObservations(),
                        null,
                        current.getCodeError(),
                        current.getSiteError(),
                        current.getGeneralError(),
                        current.isSaving()
                )
        );
    }

    public void onCodeChanged(String value) {
        WarehouseItemFormUiState current =
                requireState();

        uiState.setValue(
                copy(
                        current,
                        current.getCategory(),
                        value,
                        current.getSite(),
                        current.getPosition(),
                        current.getObservations(),
                        current.getCategoryError(),
                        null,
                        current.getSiteError(),
                        current.getGeneralError(),
                        current.isSaving()
                )
        );
    }

    public void onSiteChanged(String value) {
        WarehouseItemFormUiState current =
                requireState();

        uiState.setValue(
                copy(
                        current,
                        current.getCategory(),
                        current.getCode(),
                        value,
                        current.getPosition(),
                        current.getObservations(),
                        current.getCategoryError(),
                        current.getCodeError(),
                        null,
                        current.getGeneralError(),
                        current.isSaving()
                )
        );
    }

    public void onPositionChanged(String value) {
        WarehouseItemFormUiState current =
                requireState();

        uiState.setValue(
                copy(
                        current,
                        current.getCategory(),
                        current.getCode(),
                        current.getSite(),
                        value,
                        current.getObservations(),
                        current.getCategoryError(),
                        current.getCodeError(),
                        current.getSiteError(),
                        current.getGeneralError(),
                        current.isSaving()
                )
        );
    }

    public void onObservationsChanged(String value) {
        WarehouseItemFormUiState current =
                requireState();

        uiState.setValue(
                copy(
                        current,
                        current.getCategory(),
                        current.getCode(),
                        current.getSite(),
                        current.getPosition(),
                        value,
                        current.getCategoryError(),
                        current.getCodeError(),
                        current.getSiteError(),
                        current.getGeneralError(),
                        current.isSaving()
                )
        );
    }

    public void save() {
        WarehouseItemFormUiState current =
                requireState();

        if (current.isSaving()) {
            return;
        }

        uiState.setValue(
                copy(
                        current,
                        current.getCategory(),
                        current.getCode(),
                        current.getSite(),
                        current.getPosition(),
                        current.getObservations(),
                        null,
                        null,
                        null,
                        null,
                        true
                )
        );

        CreateWarehouseItemCommand command =
                new CreateWarehouseItemCommand(
                        current.getCategory(),
                        current.getCode(),
                        current.getSite(),
                        current.getPosition(),
                        current.getObservations()
                );

        createWarehouseItemUseCase.createWarehouseItem(
                command,
                this::handleResult
        );
    }

    private void handleResult(
            CreateWarehouseItemResult result
    ) {
        switch (result.getStatus()) {
            case SUCCESS:
                uiState.postValue(
                        withSaving(requireState(), false)
                );

                creationSuccess.postValue(
                        new UiEvent<>(
                                result.getCreatedItemId()
                        )
                );
                break;

            case VALIDATION_ERROR:
                WarehouseItemFormUiState current =
                        requireState();

                uiState.postValue(
                        copy(
                                current,
                                current.getCategory(),
                                current.getCode(),
                                current.getSite(),
                                current.getPosition(),
                                current.getObservations(),
                                result.isCategoryRequired()
                                        ? REQUIRED_FIELD_ERROR
                                        : null,
                                result.isCodeRequired()
                                        ? REQUIRED_FIELD_ERROR
                                        : null,
                                result.isSiteRequired()
                                        ? REQUIRED_FIELD_ERROR
                                        : null,
                                null,
                                false
                        )
                );
                break;

            case DUPLICATE:
                uiState.postValue(
                        withGeneralError(
                                requireState(),
                                DUPLICATE_ERROR
                        )
                );
                break;

            case PERSISTENCE_ERROR:
                uiState.postValue(
                        withGeneralError(
                                requireState(),
                                PERSISTENCE_ERROR
                        )
                );
                break;
        }
    }

    private WarehouseItemFormUiState requireState() {
        WarehouseItemFormUiState current =
                uiState.getValue();

        return current != null
                ? current
                : WarehouseItemFormUiState.initial();
    }

    private WarehouseItemFormUiState withSaving(
            WarehouseItemFormUiState state,
            boolean saving
    ) {
        return copy(
                state,
                state.getCategory(),
                state.getCode(),
                state.getSite(),
                state.getPosition(),
                state.getObservations(),
                state.getCategoryError(),
                state.getCodeError(),
                state.getSiteError(),
                state.getGeneralError(),
                saving
        );
    }

    private WarehouseItemFormUiState withGeneralError(
            WarehouseItemFormUiState state,
            String error
    ) {
        return copy(
                state,
                state.getCategory(),
                state.getCode(),
                state.getSite(),
                state.getPosition(),
                state.getObservations(),
                state.getCategoryError(),
                state.getCodeError(),
                state.getSiteError(),
                error,
                false
        );
    }

    private WarehouseItemFormUiState copy(
            WarehouseItemFormUiState ignored,
            String category,
            String code,
            String site,
            String position,
            String observations,
            String categoryError,
            String codeError,
            String siteError,
            String generalError,
            boolean saving
    ) {
        return new WarehouseItemFormUiState(
                category,
                code,
                site,
                position,
                observations,
                categoryError,
                codeError,
                siteError,
                generalError,
                saving
        );
    }
}