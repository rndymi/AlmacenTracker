package com.rndymi.almacentracker.testutil;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.port.out.RepositoryCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;

/**
 * Strict repository test double. Tests override only the operations they expect
 * their subject to invoke; every other call fails immediately.
 */
public class WarehouseItemRepositoryStub
        implements WarehouseItemRepository {

    @Override
    public LiveData<WarehouseItemsResult> observeAll() {
        throw unexpected("observeAll");
    }

    @Override
    public LiveData<WarehouseItemsResult> search(String query) {
        throw unexpected("search");
    }

    @Override
    public LiveData<WarehouseItemsResult> filter(
            WarehouseItemFilterCriteria criteria
    ) {
        throw unexpected("filter");
    }

    @Override
    public LiveData<WarehouseItemFilterOptionsResult>
    observeFilterOptions() {
        throw unexpected("observeFilterOptions");
    }

    @Override
    public LiveData<WarehouseItemDetailResult> observeById(
            long warehouseItemId
    ) {
        throw unexpected("observeById");
    }

    @Override
    public void findAll(
            RepositoryCallback<List<WarehouseItem>> callback
    ) {
        throw unexpected("findAll");
    }

    @Override
    public void findById(
            long warehouseItemId,
            RepositoryCallback<WarehouseItem> callback
    ) {
        throw unexpected("findById");
    }

    @Override
    public void existsByCategoryAndCode(
            String category,
            String code,
            RepositoryCallback<Boolean> callback
    ) {
        throw unexpected("existsByCategoryAndCode");
    }

    @Override
    public void existsByCategoryAndCodeExcludingId(
            String category,
            String code,
            long excludedWarehouseItemId,
            RepositoryCallback<Boolean> callback
    ) {
        throw unexpected("existsByCategoryAndCodeExcludingId");
    }

    @Override
    public void insert(
            WarehouseItem warehouseItem,
            RepositoryCallback<Long> callback
    ) {
        throw unexpected("insert");
    }

    @Override
    public void insertAll(
            List<WarehouseItem> warehouseItems,
            RepositoryCallback<Integer> callback
    ) {
        throw unexpected("insertAll");
    }

    @Override
    public void replaceAll(
            List<WarehouseItem> warehouseItems,
            RepositoryCallback<Integer> callback
    ) {
        throw unexpected("replaceAll");
    }

    @Override
    public void update(
            WarehouseItem warehouseItem,
            RepositoryCallback<Void> callback
    ) {
        throw unexpected("update");
    }

    @Override
    public void deleteById(
            long warehouseItemId,
            RepositoryCallback<Void> callback
    ) {
        throw unexpected("deleteById");
    }

    @Override
    public void deleteByIds(
            List<Long> warehouseItemIds,
            RepositoryCallback<Integer> callback
    ) {
        throw unexpected("deleteByIds");
    }

    private AssertionError unexpected(String operation) {
        return new AssertionError(
                "Unexpected repository operation: " + operation
        );
    }
}
