package com.rndymi.almacentracker.application.service;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.port.in.ObserveWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;

import java.util.Objects;

public final class ObserveWarehouseItemsService
        implements ObserveWarehouseItemsUseCase {

    private final WarehouseItemRepository repository;

    public ObserveWarehouseItemsService(
            WarehouseItemRepository repository
    ) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public LiveData<WarehouseItemsResult> observeWarehouseItems() {
        return repository.observeAll();
    }
}