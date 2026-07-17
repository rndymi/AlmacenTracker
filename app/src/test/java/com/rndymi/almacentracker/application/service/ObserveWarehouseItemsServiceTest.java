package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertSame;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDuplicateCheckCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemUpdateCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsDeleteCallback;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

import java.util.List;

public class ObserveWarehouseItemsServiceTest {
    @Test
    public void observeWarehouseItemsDelegatesToRepository() {
        MutableLiveData<WarehouseItemsResult> expected =
                new MutableLiveData<>();

        WarehouseItemRepository repository =
                new WarehouseItemRepository() {
                    @Override
                    public LiveData<WarehouseItemsResult>
                    observeAll() {
                        return expected;
                    }

                    @Override
                    public LiveData<WarehouseItemsResult> search(
                            String query
                    ) {
                        throw new UnsupportedOperationException(
                                "Not required by this test"
                        );
                    }

                    @Override
                    public LiveData<WarehouseItemsResult> filter(
                            WarehouseItemFilterCriteria criteria
                    ) {
                        throw new UnsupportedOperationException(
                                "Not required by this test"
                        );
                    }

                    @Override
                    public LiveData<WarehouseItemFilterOptionsResult>
                    observeFilterOptions() {
                        throw new UnsupportedOperationException(
                                "Not required by this test"
                        );
                    }

                    @Override
                    public LiveData<WarehouseItemDetailResult>
                    observeById(long warehouseItemId) {
                        throw new UnsupportedOperationException(
                                "Not required by this test"
                        );
                    }

                    @Override
                    public void findById(
                            long warehouseItemId,
                            WarehouseItemFindCallback callback
                    ) {
                        throw new UnsupportedOperationException(
                                "Not required by this test"
                        );
                    }

                    @Override
                    public void insert(
                            WarehouseItem warehouseItem,
                            WarehouseItemInsertCallback callback
                    ) {
                        throw new UnsupportedOperationException(
                                "Not required by this test"
                        );
                    }

                    @Override
                    public void update(
                            WarehouseItem warehouseItem,
                            WarehouseItemUpdateCallback callback
                    ) {
                        throw new UnsupportedOperationException(
                                "Not required by this test"
                        );
                    }

                    @Override
                    public void deleteById(
                            long warehouseItemId,
                            WarehouseItemDeleteCallback callback
                    ) {
                        throw new UnsupportedOperationException(
                                "Not required by this test"
                        );
                    }

                    @Override
                    public void deleteByIds(
                            List<Long> warehouseItemIds,
                            WarehouseItemsDeleteCallback callback
                    ) {
                        throw new UnsupportedOperationException(
                                "Not required by this test"
                        );
                    }

                    @Override
                    public void existsByCategoryAndCode(
                            String category,
                            String code,
                            WarehouseItemDuplicateCheckCallback callback
                    ) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void existsByCategoryAndCodeExcludingId(
                            String category,
                            String code,
                            long excludedWarehouseItemId,
                            WarehouseItemDuplicateCheckCallback callback
                    ) {
                        throw new UnsupportedOperationException();
                    }
                };

        ObserveWarehouseItemsService service =
                new ObserveWarehouseItemsService(repository);

        LiveData<WarehouseItemsResult> actual =
                service.observeWarehouseItems();

        assertSame(expected, actual);
    }
}
