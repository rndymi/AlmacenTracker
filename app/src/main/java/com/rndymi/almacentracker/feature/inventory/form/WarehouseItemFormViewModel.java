package com.rndymi.almacentracker.feature.inventory.form;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.data.repository.WarehouseItemDetailResult;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.domain.rule.WarehouseItemNormalizer;

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

    private final WarehouseItemSaveService saveService;
    private final WarehouseItemRepository repository;
    private final WarehouseItemNormalizer normalizer;

    private final long warehouseItemId;

    private final MediatorLiveData<WarehouseItemFormUiState>
            uiState = new MediatorLiveData<>();

    private volatile WarehouseItemFormUiState currentState;

    private final MutableLiveData<UiEvent<Long>>
            creationSuccess = new MutableLiveData<>();

    private final MutableLiveData<UiEvent<Boolean>>
            updateSuccess = new MutableLiveData<>();

    private LiveData<WarehouseItemDetailResult>
            detailSource;

    private boolean initialCodeApplied;
    private boolean initialDataApplied;
    private boolean userHasEdited;
    private boolean saveInProgress;

    public WarehouseItemFormViewModel(
            WarehouseItemSaveService saveService,
            WarehouseItemRepository repository,
            WarehouseItemNormalizer normalizer,
            long warehouseItemId
    ) {
        this.saveService =
                Objects.requireNonNull(saveService);

        this.repository =
                Objects.requireNonNull(repository);

        this.normalizer =
                Objects.requireNonNull(normalizer);

        this.warehouseItemId = warehouseItemId;

        initialize();
    }

    private void initialize() {
        if (warehouseItemId == 0L) {
            setInitialState(
                    WarehouseItemFormUiState.createMode()
            );
            return;
        }

        if (warehouseItemId < 0L) {
            setInitialState(
                    invalidIdState()
            );
            return;
        }

        setInitialState(
                WarehouseItemFormUiState.editLoading(
                        warehouseItemId
                )
        );

        detailSource =
                repository.observeById(warehouseItemId);

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

    public void applyInitialCode(String initialCode) {
        WarehouseItemFormUiState current = requireState();

        if (initialCodeApplied) {
            return;
        }

        initialCodeApplied = true;

        if (current.getMode() != WarehouseItemFormMode.CREATE
                || userHasEdited
                || !current.isEditable()
                || !current.getCode().isEmpty()) {
            return;
        }

        applyNormalizedCode(current, initialCode);
    }

    public void applyScannedCode(String scannedCode) {
        WarehouseItemFormUiState current = requireState();

        if (current.getMode() != WarehouseItemFormMode.CREATE
                || !current.isEditable()
                || saveInProgress) {
            return;
        }

        applyNormalizedCode(current, scannedCode);
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

        if (saveInProgress || !current.isEditable()) {
            return;
        }

        saveInProgress = true;

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
        saveService.create(
                formDataFrom(current),
                this::handleSaveResult
        );
    }

    private void update(WarehouseItemFormUiState current) {
        saveService.update(
                current.getWarehouseItemId(),
                formDataFrom(current),
                this::handleSaveResult
        );
    }

    private WarehouseItemFormData formDataFrom(
            WarehouseItemFormUiState state
    ) {
        return new WarehouseItemFormData(
                state.getCategory(),
                state.getCode(),
                state.getSite(),
                state.getPosition(),
                state.getObservations()
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

    private void handleSaveResult(
            WarehouseItemSaveResult result
    ) {
        saveInProgress = false;

        switch (result.getStatus()) {
            case SUCCESS:
                publish(
                        withSaving(requireState(), false)
                );

                if (requireState().getMode()
                        == WarehouseItemFormMode.CREATE) {
                    creationSuccess.postValue(
                            new UiEvent<>(
                                    result.getWarehouseItemId()
                            )
                    );
                } else {
                    updateSuccess.postValue(
                            new UiEvent<>(true)
                    );
                }
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
                        requireState().getMode()
                                == WarehouseItemFormMode.CREATE
                                ? CREATE_DUPLICATE_ERROR
                                : UPDATE_DUPLICATE_ERROR
                );
                break;

            case NOT_FOUND:
                if (requireState().getMode()
                        == WarehouseItemFormMode.EDIT) {
                    publish(notFoundState());
                } else {
                    publishGeneralError(CREATE_ERROR);
                }
                break;

            case PERSISTENCE_ERROR:
                publishGeneralError(
                        requireState().getMode()
                                == WarehouseItemFormMode.CREATE
                                ? CREATE_ERROR
                                : UPDATE_ERROR
                );
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
        WarehouseItemFormUiState current = currentState;

        return current != null
                ? current
                : WarehouseItemFormUiState.createMode();
    }

    private void setInitialState(
            WarehouseItemFormUiState state
    ) {
        currentState = Objects.requireNonNull(state);
        uiState.setValue(state);
    }

    private String optionalToText(String value) {
        return value == null ? "" : value;
    }

    private void publish(
            WarehouseItemFormUiState state
    ) {
        currentState = Objects.requireNonNull(state);
        uiState.postValue(state);
    }

    private void applyNormalizedCode(
            WarehouseItemFormUiState current,
            String code
    ) {
        String normalizedCode =
                normalizer.normalizeCode(code);

        if (normalizedCode.isEmpty()) {
            return;
        }

        userHasEdited = true;

        publish(
                copy(
                        current,
                        current.getCategory(),
                        normalizedCode,
                        current.getSite(),
                        current.getPosition(),
                        current.getObservations(),
                        current.getCategoryError(),
                        null,
                        current.getSiteError(),
                        current.getGeneralError(),
                        current.isLoading(),
                        current.isSaving(),
                        current.isNotFound(),
                        current.isInvalidId()
                )
        );
    }

    @Override
    protected void onCleared() {
        if (detailSource != null) {
            uiState.removeSource(detailSource);
        }

        super.onCleared();
    }
}
