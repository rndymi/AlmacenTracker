package com.rndymi.almacentracker.feature.withdrawal_history.create;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.core.common.event.UiEvent;
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

    private final WithdrawalHistoryDraftValidator validator;

    private final MutableLiveData<
            WithdrawalHistoryCreateUiState> uiState =
            new MutableLiveData<>(
                    WithdrawalHistoryCreateUiState
                            .initializing()
            );

    private final MutableLiveData<
            UiEvent<WithdrawalHistoryDraft>>
            continueEvent =
            new MutableLiveData<>();

    private boolean initialized;

    public WithdrawalHistoryCreateViewModel(
            WithdrawalHistoryDraftValidator validator
    ) {
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
    getContinueEvent() {
        return continueEvent;
    }

    public void initialize(
            List<WithdrawalHistoryCreateInput> input,
            long now
    ) {
        if (initialized) {
            return;
        }

        initialized = true;

        if (input == null || input.isEmpty()) {
            uiState.setValue(
                    WithdrawalHistoryCreateUiState
                            .error()
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
                            .error()
            );
            return;
        }

        uiState.setValue(
                WithdrawalHistoryCreateUiState.ready(
                        "",
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

    public void continueToConfirmation(
            long now
    ) {
        WithdrawalHistoryCreateUiState current =
                currentEditableState();

        if (current == null) {
            return;
        }

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
            return;
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

        WithdrawalHistoryDraft draft =
                new WithdrawalHistoryDraft(
                        validator.normalizeTitle(
                                current.getTitle()
                        ),
                        current.getRegisteredAt(),
                        draftEntries
                );

        continueEvent.setValue(
                new UiEvent<>(draft)
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
                            entryErrors
                                    .getQuantityError(),
                            entryErrors
                                    .getUnitError()
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
                || !current.canContinue()) {
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
}
