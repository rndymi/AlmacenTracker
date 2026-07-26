package com.rndymi.almacentracker.feature.reference_list.location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.domain.reference.WarehouseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ReferenceListLocationViewModel
        extends ViewModel {

    private final ReferenceListLocationService service;

    private final MutableLiveData<
            ReferenceListLocationUiState
            > uiState =
            new MutableLiveData<>(
                    ReferenceListLocationUiState.idle()
            );

    private List<WarehouseReference>
            currentReferences =
            Collections.emptyList();

    private boolean initialReferencesApplied;
    private boolean queryInProgress;

    public ReferenceListLocationViewModel(
            ReferenceListLocationService service
    ) {
        this.service = Objects.requireNonNull(
                service,
                "service"
        );
    }

    public LiveData<ReferenceListLocationUiState>
    getUiState() {
        return uiState;
    }

    public void applyInitialReferences(
            List<WarehouseReference> references
    ) {
        if (initialReferencesApplied) {
            return;
        }

        initialReferencesApplied = true;

        currentReferences =
                references == null
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(
                        new ArrayList<>(
                                references
                        )
                );

        locate();
    }

    public void retry() {
        ReferenceListLocationUiState currentState =
                uiState.getValue();

        if (currentState == null
                || !currentState.canRetry()) {
            return;
        }

        locate();
    }

    private void locate() {
        if (queryInProgress) {
            return;
        }

        queryInProgress = true;

        uiState.setValue(
                ReferenceListLocationUiState.loading()
        );

        service.locate(
                currentReferences,
                result -> {
                    queryInProgress = false;

                    switch (result.getStatus()) {
                        case SUCCESS:
                            uiState.postValue(
                                    ReferenceListLocationUiState
                                            .content(result)
                            );
                            break;

                        case INVALID_INPUT:
                            uiState.postValue(
                                    ReferenceListLocationUiState
                                            .invalidInput()
                            );
                            break;

                        case ERROR:
                            uiState.postValue(
                                    ReferenceListLocationUiState
                                            .error()
                            );
                            break;
                    }
                }
        );
    }
}