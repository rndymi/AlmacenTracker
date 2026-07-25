package com.rndymi.almacentracker.data.repository;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.data.repository.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.data.repository.WarehouseItemDetailResult;
import com.rndymi.almacentracker.data.repository.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.data.repository.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;

public interface WarehouseItemRepository {

    LiveData<WarehouseItemsResult> observeAll();

    LiveData<WarehouseItemsResult> search(String query);

    LiveData<WarehouseItemsResult> filter(
            WarehouseItemFilterCriteria criteria
    );

    LiveData<WarehouseItemFilterOptionsResult>
    observeFilterOptions();

    LiveData<WarehouseItemDetailResult> observeById(
            long warehouseItemId
    );

    void findAll(
            RepositoryCallback<List<WarehouseItem>> callback
    );

    void findById(
            long warehouseItemId,
            RepositoryCallback<WarehouseItem> callback
    );

    void findAllByCode(
            String code,
            RepositoryCallback<List<WarehouseItem>> callback
    );

    void existsByCategoryAndCode(
            String category,
            String code,
            RepositoryCallback<Boolean> callback
    );

    void existsByCategoryAndCodeExcludingId(
            String category,
            String code,
            long excludedWarehouseItemId,
            RepositoryCallback<Boolean> callback
    );

    void insert(
            WarehouseItem warehouseItem,
            RepositoryCallback<Long> callback
    );

    void insertAll(
            List<WarehouseItem> warehouseItems,
            RepositoryCallback<Integer> callback
    );

    void replaceAll(
            List<WarehouseItem> warehouseItems,
            RepositoryCallback<Integer> callback
    );

    void update(
            WarehouseItem warehouseItem,
            RepositoryCallback<Void> callback
    );

    void deleteById(
            long warehouseItemId,
            RepositoryCallback<Void> callback
    );

    void deleteByIds(
            List<Long> warehouseItemIds,
            RepositoryCallback<Integer> callback
    );
}
