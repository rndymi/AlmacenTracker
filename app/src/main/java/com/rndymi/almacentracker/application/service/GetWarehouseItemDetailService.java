package com.rndymi.almacentracker.application.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.application.port.in.GetWarehouseItemDetailUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;

import java.util.Objects;

public final class GetWarehouseItemDetailService
        implements GetWarehouseItemDetailUseCase {
    private final WarehouseItemRepository repository;
    public GetWarehouseItemDetailService(
            WarehouseItemRepository repository
    ) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public LiveData<WarehouseItemDetailResult>
    observerWarehouseItemDetail(long warehouseItemId) {
        if (warehouseItemId <= 0L) {
            MutableLiveData<WarehouseItemDetailResult> result =
                    new MutableLiveData<>();
            result.setValue(
                    WarehouseItemDetailResult.invalidId()
            );
        }

        return repository.observeById(warehouseItemId);
    }
}
