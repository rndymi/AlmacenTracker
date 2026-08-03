package com.rndymi.almacentracker.feature.withdrawal_history.create;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraft;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraftEntry;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraftValidationResult;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraftValidator;
import com.rndymi.almacentracker.feature.withdrawal_history.common.WithdrawalHistoryCreateInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class WithdrawalHistoryCreateViewModel
        extends ViewModel {

    private static final String SAVE_ERROR_MESSAGE =
            "No se pudo guardar el historial.";

    private final WithdrawalHistorySaveService saveService;
    private final WithdrawalHistoryDraftValidator validator;

    private final MutableLiveData<
            WithdrawalHistoryCreateUiState> uiState =
            new MutableLiveData<>(
                    WithdrawalHistoryCreateUiState
                            .initializing()
            );

    private final MutableLiveData<
            UiEvent<WithdrawalHistoryDraft>>
            confirmationEvent =
            new MutableLiveData<>();

    private final MutableLiveData<
            UiEvent<Long>> savedEvent =
            new MutableLiveData<>();

    private boolean initialized;
    private boolean saveInProgress;
    private boolean saveCompleted;
    private long saveRequestToken;

    public WithdrawalHistoryCreateViewModel(
            WithdrawalHistorySaveService saveService,
            WithdrawalHistoryDraftValidator validator
    ) {
        this.saveService =
                Objects.requireNonNull(
                        saveService,
                        "saveService"
                );

        this.validator =
                Objects.requireNonNull(
                        validator,
                        "validator"
                );
    }

    public LiveData<WithdrawalHistoryCreateUiState>
    getUiState() {
        return uiState;
    }

    public LiveData<UiEvent<WithdrawalHistoryDraft>>
    getConfirmationEvent() {
        return confirmationEvent;
    }

    public LiveData<UiEvent<Long>>
    getSavedEvent() {
        return savedEvent;
    }

    public void initialize(
            List<WithdrawalHistoryCreateInput> input,
            String initialTitle,
            long now
    ) {
        if (initialized) {
            return;
        }

        initialized = true;

        if (input == null
                || input.isEmpty()
                || now <= 0L) {
            uiState.setValue(
                    WithdrawalHistoryCreateUiState
                            .invalidInitialInput()
            );
            return;
        }

        List<WithdrawalHistoryDraftEntryUiModel>
                entries =
                new ArrayList<>();

        long stableId = 1L;

        for (
                WithdrawalHistoryCreateInput value
                : input
        ) {
            if (value == null) {
                continue;
            }

            String quantity =
                    value.getQuantityProposal() == null
                            ? ""
                            : String.valueOf(
                            value.getQuantityProposal()
                    );

            String unit =
                    value.getUnitProposal() == null
                            ? ""
                            : value.getUnitProposal();

            entries.add(
                    new WithdrawalHistoryDraftEntryUiModel(
                            stableId++,
                            entries.size(),
                            value.getCategory(),
                            value.getCode(),
                            quantity,
                            unit,
                            value.getWarehouseItemIdSnapshot(),
                            value.getSiteSnapshot(),
                            value.getPositionSnapshot(),
                            value.getLocationStatus(),
                            null,
                            null
                    )
            );
        }

        if (entries.isEmpty()) {
            uiState.setValue(
                    WithdrawalHistoryCreateUiState
                            .invalidInitialInput()
            );
            return;
        }

        uiState.setValue(
                WithdrawalHistoryCreateUiState.ready(
                        normalizeInitialTitle(
                                initialTitle
                        ),
                        now,
                        entries
                )
        );
    }

    public void onTitleChanged(
            String value
    ) {
        WithdrawalHistoryCreateUiState current =
                currentEditableState();

        if (current == null) {
            return;
        }

        uiState.setValue(
                WithdrawalHistoryCreateUiState.ready(
                        value == null ? "" : value,
                        current.getRegisteredAt(),
                        current.getEntries()
                )
        );
    }

    public void onQuantityChanged(
            long stableId,
            String value
    ) {
        updateEntry(
                stableId,
                entry -> entry.withQuantity(
                        value == null ? "" : value
                )
        );
    }

    public void onUnitChanged(
            long stableId,
            String value
    ) {
        updateEntry(
                stableId,
                entry -> entry.withUnit(
                        value == null ? "" : value
                )
        );
    }

    public void requestSaveConfirmation(
            long now
    ) {
        if (saveInProgress || saveCompleted) {
            return;
        }

        WithdrawalHistoryCreateUiState current =
                currentEditableState();

        if (current == null) {
            return;
        }

        WithdrawalHistoryDraft draft =
                buildValidatedDraft(
                        current,
                        now
                );

        if (draft == null) {
            return;
        }

        confirmationEvent.setValue(
                new UiEvent<>(draft)
        );
    }

    public void confirmSave(
            long now
    ) {
        if (saveInProgress || saveCompleted) {
            return;
        }

        WithdrawalHistoryCreateUiState current =
                currentEditableState();

        if (current == null) {
            return;
        }

        WithdrawalHistoryDraft draft =
                buildValidatedDraft(
                        current,
                        now
                );

        if (draft == null) {
            return;
        }

        saveInProgress = true;

        long requestToken =
                ++saveRequestToken;

        uiState.setValue(
                WithdrawalHistoryCreateUiState.saving(
                        current.getTitle(),
                        current.getRegisteredAt(),
                        current.getEntries()
                )
        );

        saveService.save(
                draft,
                new RepositoryCallback<Long>() {
                    @Override
                    public void onSuccess(
                            Long generatedId
                    ) {
                        handleSaveSuccess(
                                requestToken,
                                generatedId
                        );
                    }

                    @Override
                    public void onError(
                            Throwable cause
                    ) {
                        handleSaveError(
                                requestToken
                        );
                    }
                }
        );
    }

    private void handleSaveSuccess(
            long requestToken,
            Long generatedId
    ) {
        if (!isActiveRequest(requestToken)
                || generatedId == null
                || generatedId <= 0L) {
            if (isActiveRequest(requestToken)) {
                handleSaveError(requestToken);
            }
            return;
        }

        saveInProgress = false;
        saveCompleted = true;

        WithdrawalHistoryCreateUiState current =
                uiState.getValue();

        if (current == null) {
            return;
        }

        uiState.postValue(
                WithdrawalHistoryCreateUiState.saved(
                        current.getTitle(),
                        current.getRegisteredAt(),
                        current.getEntries()
                )
        );

        savedEvent.postValue(
                new UiEvent<>(generatedId)
        );
    }

    private void handleSaveError(
            long requestToken
    ) {
        if (!isActiveRequest(requestToken)) {
            return;
        }

        saveInProgress = false;

        WithdrawalHistoryCreateUiState current =
                uiState.getValue();

        if (current == null) {
            return;
        }

        uiState.postValue(
                WithdrawalHistoryCreateUiState.saveError(
                        current.getTitle(),
                        current.getRegisteredAt(),
                        current.getEntries(),
                        SAVE_ERROR_MESSAGE
                )
        );
    }

    private boolean isActiveRequest(
            long requestToken
    ) {
        return saveInProgress
                && !saveCompleted
                && saveRequestToken == requestToken;
    }

    private WithdrawalHistoryDraft buildValidatedDraft(
            WithdrawalHistoryCreateUiState current,
            long now
    ) {
        WithdrawalHistoryDraftValidationResult result =
                validator.validate(
                        current.getTitle(),
                        current.getRegisteredAt(),
                        new ArrayList<>(
                                current.getEntries()
                        ),
                        now
                );

        if (!result.isValid()) {
            uiState.setValue(
                    applyValidationErrors(
                            current,
                            result
                    )
            );
            return null;
        }

        List<WithdrawalHistoryDraftEntry>
                draftEntries =
                new ArrayList<>();

        for (
                WithdrawalHistoryDraftEntryUiModel entry
                : current.getEntries()
        ) {
            draftEntries.add(
                    new WithdrawalHistoryDraftEntry(
                            entry.getOrderIndex(),
                            entry.getCategory(),
                            entry.getCode(),
                            validator.parseQuantity(
                                    entry.getQuantityText()
                            ),
                            normalizeUnit(
                                    entry.getUnitText()
                            ),
                            entry.getWarehouseItemIdSnapshot(),
                            entry.getSiteSnapshot(),
                            entry.getPositionSnapshot(),
                            entry.getLocationStatus()
                    )
            );
        }

        return new WithdrawalHistoryDraft(
                validator.normalizeTitle(
                        current.getTitle()
                ),
                current.getRegisteredAt(),
                draftEntries
        );
    }

    private WithdrawalHistoryCreateUiState
    applyValidationErrors(
            WithdrawalHistoryCreateUiState current,
            WithdrawalHistoryDraftValidationResult result
    ) {
        List<WithdrawalHistoryDraftEntryUiModel>
                updatedEntries =
                new ArrayList<>();

        Map<Long,
                WithdrawalHistoryDraftValidationResult
                        .EntryErrors> errors =
                result.getEntryErrors();

        for (
                WithdrawalHistoryDraftEntryUiModel entry
                : current.getEntries()
        ) {
            WithdrawalHistoryDraftValidationResult
                    .EntryErrors entryErrors =
                    errors.get(
                            entry.getStableId()
                    );

            updatedEntries.add(
                    entryErrors == null
                            ? entry.withErrors(
                            null,
                            null
                    )
                            : entry.withErrors(
                            entryErrors.getQuantityError(),
                            entryErrors.getUnitError()
                    )
            );
        }

        return WithdrawalHistoryCreateUiState.invalid(
                current.getTitle(),
                current.getRegisteredAt(),
                updatedEntries,
                result.getTitleError(),
                result.getDateError()
        );
    }

    private void updateEntry(
            long stableId,
            EntryUpdater updater
    ) {
        WithdrawalHistoryCreateUiState current =
                currentEditableState();

        if (current == null) {
            return;
        }

        List<WithdrawalHistoryDraftEntryUiModel>
                updated =
                new ArrayList<>();

        for (
                WithdrawalHistoryDraftEntryUiModel entry
                : current.getEntries()
        ) {
            updated.add(
                    entry.getStableId() == stableId
                            ? updater.update(entry)
                            : entry
            );
        }

        uiState.setValue(
                WithdrawalHistoryCreateUiState.ready(
                        current.getTitle(),
                        current.getRegisteredAt(),
                        updated
                )
        );
    }

    private WithdrawalHistoryCreateUiState
    currentEditableState() {
        WithdrawalHistoryCreateUiState current =
                uiState.getValue();

        if (current == null
                || !current.isEditable()) {
            return null;
        }

        return current;
    }

    private static String normalizeUnit(
            String value
    ) {
        if (value == null
                || value.trim().isEmpty()) {
            return null;
        }

        return value
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }

    private interface EntryUpdater {

        WithdrawalHistoryDraftEntryUiModel update(
                WithdrawalHistoryDraftEntryUiModel entry
        );
    }

    private String normalizeInitialTitle(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value
                .replace('\u00A0', ' ')
                .replaceAll(
                        "[\\p{Z}\\s]+",
                        " "
                )
                .trim();
    }

    public void initialize(
            List<WithdrawalHistoryCreateInput> input,
            long now
    ) {
        initialize(
                input,
                null,
                now
        );
    }
}