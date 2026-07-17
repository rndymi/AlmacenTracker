package com.rndymi.almacentracker.application.service;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemFilterOptionsUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;

import java.util.Objects;

public final class ObserveWarehouseItemFilterOptionsService
        implements ObserveWarehouseItemFilterOptionsUseCase {

    private final WarehouseItemRepository repository;

    public ObserveWarehouseItemFilterOptionsService(
            WarehouseItemRepository repository
    ) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public LiveData<WarehouseItemFilterOptionsResult>
    observeFilterOptions() {
        return repository.observeFilterOptions();
    }
}