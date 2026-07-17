package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDuplicateCheckCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemUpdateCallback;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.testutil.LiveDataTestUtil;

import org.junit.Rule;
import org.junit.Test;

public final class GetWarehouseItemDetailServiceTest {

    @Rule
    public final InstantTaskExecutorRule executorRule =
            new InstantTaskExecutorRule();

    @Test
    public void observeWarehouseItemDetail_returnsInvalidId_whenIdIsZero()
            throws InterruptedException {

        FakeWarehouseItemRepository repository =
                new FakeWarehouseItemRepository();

        GetWarehouseItemDetailService service =
                new GetWarehouseItemDetailService(repository);

        WarehouseItemDetailResult result =
                LiveDataTestUtil.getOrAwaitValue(
                        service.observeWarehouseItemDetail(0L)
                );

        assertTrue(
                result instanceof
                        WarehouseItemDetailResult.InvalidId
        );

        assertEquals(0, repository.observeByIdCalls);
    }

    @Test
    public void observeWarehouseItemDetail_delegatesValidIdToRepository()
            throws InterruptedException {

        FakeWarehouseItemRepository repository =
                new FakeWarehouseItemRepository();

        WarehouseItem warehouseItem = new WarehouseItem(
                7L,
                "MR",
                "1050",
                "A1",
                "Nivel 2",
                "Caja dañada",
                1000L,
                1000L
        );

        repository.detailResult.setValue(
                WarehouseItemDetailResult.found(
                        warehouseItem
                )
        );

        GetWarehouseItemDetailService service =
                new GetWarehouseItemDetailService(repository);

        WarehouseItemDetailResult result =
                LiveDataTestUtil.getOrAwaitValue(
                        service.observeWarehouseItemDetail(7L)
                );

        assertTrue(
                result instanceof WarehouseItemDetailResult.Found
        );

        assertEquals(1, repository.observeByIdCalls);
        assertEquals(7L, repository.requestedId);
    }

    private static final class FakeWarehouseItemRepository
            implements WarehouseItemRepository {

        private final MutableLiveData<WarehouseItemDetailResult>
                detailResult = new MutableLiveData<>();

        private int observeByIdCalls;
        private long requestedId;

        @Override
        public LiveData<WarehouseItemsResult> observeAll() {
            return new MutableLiveData<>();
        }

        @Override
        public LiveData<WarehouseItemDetailResult> observeById(
                long warehouseItemId
        ) {
            observeByIdCalls++;
            requestedId = warehouseItemId;
            return detailResult;
        }

        @Override
        public void findById(
                long warehouseItemId,
                WarehouseItemFindCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(
                WarehouseItem warehouseItem,
                WarehouseItemUpdateCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteById(
                long warehouseItemId,
                WarehouseItemDeleteCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insert(
                WarehouseItem warehouseItem,
                WarehouseItemInsertCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiveData<WarehouseItemsResult> search(
                String query
        ) {
            return new MutableLiveData<>();
        }

        @Override
        public LiveData<WarehouseItemsResult> filter(
                WarehouseItemFilterCriteria criteria
        ) {
            return new MutableLiveData<>();
        }

        @Override
        public LiveData<WarehouseItemFilterOptionsResult>
        observeFilterOptions() {
            return new MutableLiveData<>();
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
    }
}
