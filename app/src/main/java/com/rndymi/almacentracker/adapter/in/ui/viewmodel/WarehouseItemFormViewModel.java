package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.adapter.in.ui.state.UiEvent;
import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemFormMode;
import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemFormUiState;
import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemCommand;
import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.in.GetWarehouseItemDetailUseCase;
import com.rndymi.almacentracker.application.port.in.UpdateWarehouseItemCommand;
import com.rndymi.almacentracker.application.port.in.UpdateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.result.CreateWarehouseItemResult;
import com.rndymi.almacentracker.application.result.UpdateWarehouseItemResult;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Objects;

public final class WarehouseItemFormViewModel
        extends ViewModel {

    private static final String REQUIRED_FIELD_ERROR =
            "Este campo es obligatorio.";

    private static final String CREATE_DUPLICATE_ERROR =
            "Ya existe una mercancía con esta categoría y código.";

    private static final String UPDATE_DUPLICATE_ERROR =
            "Ya existe otra mercancía con esta categoría y código.";

    private static final String CREATE_ERROR =
            "No se pudo registrar la mercancía.";

    private static final String UPDATE_ERROR =
            "No se pudieron guardar los cambios.";

    private static final String LOAD_ERROR =
            "No se pudo cargar la mercancía para editarla.";

    private static final String NOT_FOUND_ERROR =
            "La mercancía ya no está disponible.";

    private static final String INVALID_ID_ERROR =
            "No se pudo identificar la mercancía.";

    private final CreateWarehouseItemUseCase
            createWarehouseItemUseCase;

    private final UpdateWarehouseItemUseCase
            updateWarehouseItemUseCase;

    private final GetWarehouseItemDetailUseCase
            getWarehouseItemDetailUseCase;

    private final long warehouseItemId;

    private final MediatorLiveData<WarehouseItemFormUiState>
            uiState = new MediatorLiveData<>();

    private final MutableLiveData<UiEvent<Long>>
            creationSuccess = new MutableLiveData<>();

    private final MutableLiveData<UiEvent<Boolean>>
            updateSuccess = new MutableLiveData<>();

    private LiveData<WarehouseItemDetailResult>
            detailSource;

    private boolean initialDataApplied;
    private boolean userHasEdited;

    public WarehouseItemFormViewModel(
            CreateWarehouseItemUseCase createWarehouseItemUseCase,
            UpdateWarehouseItemUseCase updateWarehouseItemUseCase,
            GetWarehouseItemDetailUseCase
                    getWarehouseItemDetailUseCase,
            long warehouseItemId
    ) {
        this.createWarehouseItemUseCase =
                Objects.requireNonNull(
                        createWarehouseItemUseCase
                );

        this.updateWarehouseItemUseCase =
                Objects.requireNonNull(
                        updateWarehouseItemUseCase
                );

        this.getWarehouseItemDetailUseCase =
                Objects.requireNonNull(
                        getWarehouseItemDetailUseCase
                );

        this.warehouseItemId = warehouseItemId;

        initialize();
    }

    private void initialize() {
        if (warehouseItemId == 0L) {
            uiState.setValue(
                    WarehouseItemFormUiState.createMode()
            );
            return;
        }

        if (warehouseItemId < 0L) {
            uiState.setValue(
                    invalidIdState()
            );
            return;
        }

        uiState.setValue(
                WarehouseItemFormUiState.editLoading(
                        warehouseItemId
                )
        );

        detailSource =
                getWarehouseItemDetailUseCase
                        .observeWarehouseItemDetail(
                                warehouseItemId
                        );

        uiState.addSource(
                detailSource,
                this::handleDetailResult
        );
    }

    public LiveData<WarehouseItemFormUiState> getUiState() {
        return uiState;
    }

    public LiveData<UiEvent<Long>> getCreationSuccess() {
        return creationSuccess;
    }

    public LiveData<UiEvent<Boolean>> getUpdateSuccess() {
        return updateSuccess;
    }

    public void onCategoryChanged(String value) {
        userHasEdited = true;

        WarehouseItemFormUiState current = requireState();

        publish(
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
                        null,
                        current.isLoading(),
                        current.isSaving(),
                        current.isNotFound(),
                        current.isInvalidId()
                )
        );
    }

    public void onCodeChanged(String value) {
        userHasEdited = true;

        WarehouseItemFormUiState current = requireState();

        publish(
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
                        null,
                        current.isLoading(),
                        current.isSaving(),
                        current.isNotFound(),
                        current.isInvalidId()
                )
        );
    }

    public void onSiteChanged(String value) {
        userHasEdited = true;

        WarehouseItemFormUiState current = requireState();

        publish(
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
                        null,
                        current.isLoading(),
                        current.isSaving(),
                        current.isNotFound(),
                        current.isInvalidId()
                )
        );
    }

    public void onPositionChanged(String value) {
        userHasEdited = true;

        WarehouseItemFormUiState current = requireState();

        publish(
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
                        null,
                        current.isLoading(),
                        current.isSaving(),
                        current.isNotFound(),
                        current.isInvalidId()
                )
        );
    }

    public void onObservationsChanged(String value) {
        userHasEdited = true;

        WarehouseItemFormUiState current = requireState();

        publish(
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
                        null,
                        current.isLoading(),
                        current.isSaving(),
                        current.isNotFound(),
                        current.isInvalidId()
                )
        );
    }

    public void save() {
        WarehouseItemFormUiState current = requireState();

        if (!current.isEditable()) {
            return;
        }

        publish(
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
                        false,
                        true,
                        false,
                        false
                )
        );

        if (current.getMode() == WarehouseItemFormMode.CREATE) {
            create(current);
        } else {
            update(current);
        }
    }

    private void create(WarehouseItemFormUiState current) {
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
                this::handleCreateResult
        );
    }

    private void update(WarehouseItemFormUiState current) {
        UpdateWarehouseItemCommand command =
                new UpdateWarehouseItemCommand(
                        current.getWarehouseItemId(),
                        current.getCategory(),
                        current.getCode(),
                        current.getSite(),
                        current.getPosition(),
                        current.getObservations()
                );

        updateWarehouseItemUseCase.updateWarehouseItem(
                command,
                this::handleUpdateResult
        );
    }

    private void handleDetailResult(
            WarehouseItemDetailResult result
    ) {
        if (result instanceof WarehouseItemDetailResult.Found) {
            if (initialDataApplied || userHasEdited) {
                return;
            }

            WarehouseItem warehouseItem =
                    ((WarehouseItemDetailResult.Found) result)
                            .getWarehouseItem();

            initialDataApplied = true;

            publish(
                    loadedEditState(warehouseItem)
            );
            return;
        }

        if (result instanceof WarehouseItemDetailResult.NotFound) {
            publish(notFoundState());
            return;
        }

        if (result instanceof WarehouseItemDetailResult.InvalidId) {
            publish(invalidIdState());
            return;
        }

        publish(
                loadErrorState()
        );
    }

    private void handleCreateResult(
            CreateWarehouseItemResult result
    ) {
        switch (result.getStatus()) {
            case SUCCESS:
                publish(
                        withSaving(requireState(), false)
                );

                creationSuccess.postValue(
                        new UiEvent<>(
                                result.getCreatedItemId()
                        )
                );
                break;

            case VALIDATION_ERROR:
                publishValidationErrors(
                        result.isCategoryRequired(),
                        result.isCodeRequired(),
                        result.isSiteRequired()
                );
                break;

            case DUPLICATE:
                publishGeneralError(
                        CREATE_DUPLICATE_ERROR
                );
                break;

            case PERSISTENCE_ERROR:
                publishGeneralError(CREATE_ERROR);
                break;
        }
    }

    private void handleUpdateResult(
            UpdateWarehouseItemResult result
    ) {
        switch (result.getStatus()) {
            case SUCCESS:
                publish(
                        withSaving(requireState(), false)
                );

                updateSuccess.postValue(
                        new UiEvent<>(true)
                );
                break;

            case VALIDATION_ERROR:
                publishValidationErrors(
                        result.isCategoryRequired(),
                        result.isCodeRequired(),
                        result.isSiteRequired()
                );
                break;

            case DUPLICATE:
                publishGeneralError(
                        UPDATE_DUPLICATE_ERROR
                );
                break;

            case NOT_FOUND:
                publish(notFoundState());
                break;

            case PERSISTENCE_ERROR:
                publishGeneralError(UPDATE_ERROR);
                break;
        }
    }

    private void publishValidationErrors(
            boolean categoryRequired,
            boolean codeRequired,
            boolean siteRequired
    ) {
        WarehouseItemFormUiState current = requireState();

        publish(
                copy(
                        current,
                        current.getCategory(),
                        current.getCode(),
                        current.getSite(),
                        current.getPosition(),
                        current.getObservations(),
                        categoryRequired
                                ? REQUIRED_FIELD_ERROR
                                : null,
                        codeRequired
                                ? REQUIRED_FIELD_ERROR
                                : null,
                        siteRequired
                                ? REQUIRED_FIELD_ERROR
                                : null,
                        null,
                        false,
                        false,
                        false,
                        false
                )
        );
    }

    private void publishGeneralError(String error) {
        WarehouseItemFormUiState current = requireState();

        publish(
                copy(
                        current,
                        current.getCategory(),
                        current.getCode(),
                        current.getSite(),
                        current.getPosition(),
                        current.getObservations(),
                        current.getCategoryError(),
                        current.getCodeError(),
                        current.getSiteError(),
                        error,
                        false,
                        false,
                        false,
                        false
                )
        );
    }

    private WarehouseItemFormUiState loadedEditState(
            WarehouseItem item
    ) {
        return new WarehouseItemFormUiState(
                WarehouseItemFormMode.EDIT,
                item.getId(),
                item.getCategory(),
                item.getCode(),
                item.getSite(),
                optionalToText(item.getPosition()),
                optionalToText(item.getObservations()),
                null,
                null,
                null,
                null,
                false,
                false,
                false,
                false
        );
    }

    private WarehouseItemFormUiState notFoundState() {
        return new WarehouseItemFormUiState(
                WarehouseItemFormMode.EDIT,
                warehouseItemId,
                "",
                "",
                "",
                "",
                "",
                null,
                null,
                null,
                NOT_FOUND_ERROR,
                false,
                false,
                true,
                false
        );
    }

    private WarehouseItemFormUiState invalidIdState() {
        return new WarehouseItemFormUiState(
                WarehouseItemFormMode.EDIT,
                warehouseItemId,
                "",
                "",
                "",
                "",
                "",
                null,
                null,
                null,
                INVALID_ID_ERROR,
                false,
                false,
                false,
                true
        );
    }

    private WarehouseItemFormUiState loadErrorState() {
        return new WarehouseItemFormUiState(
                WarehouseItemFormMode.EDIT,
                warehouseItemId,
                "",
                "",
                "",
                "",
                "",
                null,
                null,
                null,
                LOAD_ERROR,
                false,
                false,
                false,
                false
        );
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
                state.isLoading(),
                saving,
                state.isNotFound(),
                state.isInvalidId()
        );
    }

    private WarehouseItemFormUiState copy(
            WarehouseItemFormUiState state,
            String category,
            String code,
            String site,
            String position,
            String observations,
            String categoryError,
            String codeError,
            String siteError,
            String generalError,
            boolean loading,
            boolean saving,
            boolean notFound,
            boolean invalidId
    ) {
        return new WarehouseItemFormUiState(
                state.getMode(),
                state.getWarehouseItemId(),
                category,
                code,
                site,
                position,
                observations,
                categoryError,
                codeError,
                siteError,
                generalError,
                loading,
                saving,
                notFound,
                invalidId
        );
    }

    private WarehouseItemFormUiState requireState() {
        WarehouseItemFormUiState current =
                uiState.getValue();

        return current != null
                ? current
                : WarehouseItemFormUiState.createMode();
    }

    private String optionalToText(String value) {
        return value == null ? "" : value;
    }

    private void publish(
            WarehouseItemFormUiState state
    ) {
        uiState.postValue(state);
    }

    @Override
    protected void onCleared() {
        if (detailSource != null) {
            uiState.removeSource(detailSource);
        }

        super.onCleared();
    }
}