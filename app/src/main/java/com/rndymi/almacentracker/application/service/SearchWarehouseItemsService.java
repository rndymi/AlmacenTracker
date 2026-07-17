package com.rndymi.almacentracker.application.service;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.port.in.SearchWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;

import java.util.Objects;

public final class SearchWarehouseItemsService
        implements SearchWarehouseItemsUseCase {
    private final WarehouseItemRepository repository;
    public SearchWarehouseItemsService(
            WarehouseItemRepository repository
    ) {
       this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public LiveData<WarehouseItemsResult> search(String query) {
        String normalizedQuery = query == null
                ? ""
                : query.trim();

        if (normalizedQuery.isEmpty()) {
            return repository.observeAll();
        }

        return repository.search(normalizedQuery);
    }
}
