package com.rndymi.almacentracker.testutil;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.data.repository.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.data.repository.WarehouseItemDetailResult;
import com.rndymi.almacentracker.data.repository.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.data.repository.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceLocation;

import java.util.List;

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
    public void findAllByCode(
            String code,
            RepositoryCallback<List<WarehouseItem>> callback
    ) {
        throw unexpected("findAllByCode");
    }

    @Override
    public void findAllByReferences(
            List<WarehouseReference> references,
            RepositoryCallback<
                    List<WarehouseReferenceLocation>
                    > callback
    ) {
        throw new UnsupportedOperationException(
                "Not implemented"
        );
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
