package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.port.out.WarehouseItemFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemUpdateCallback;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

public class SearchWarehouseItemsServiceTest {

    @Test
    public void delegatesNormalizedQueryToRepository() {
        FakeRepository repository = new FakeRepository();

        SearchWarehouseItemsService service =
                new SearchWarehouseItemsService(repository);

        LiveData<WarehouseItemsResult> result =
                service.search("  A1  ");

        assertEquals("A1", repository.searchQuery);
        assertEquals(1, repository.searchCalls);
        assertSame(repository.searchResult, result);
    }

    @Test
    public void observesAllItemsWhenQueryIsEmpty() {
        FakeRepository repository = new FakeRepository();

        SearchWarehouseItemsService service =
                new SearchWarehouseItemsService(repository);

        LiveData<WarehouseItemsResult> result =
                service.search("   ");

        assertEquals(1, repository.observeAllCalls);
        assertEquals(0, repository.searchCalls);
        assertSame(repository.allItemsResult, result);
    }

    @Test
    public void observesAllItemsWhenQueryIsNull() {
        FakeRepository repository = new FakeRepository();

        SearchWarehouseItemsService service =
                new SearchWarehouseItemsService(repository);

        service.search(null);

        assertEquals(1, repository.observeAllCalls);
        assertEquals(0, repository.searchCalls);
    }

    private static final class FakeRepository
            implements WarehouseItemRepository {

        private final MutableLiveData<WarehouseItemsResult>
                allItemsResult = new MutableLiveData<>();

        private final MutableLiveData<WarehouseItemsResult>
                searchResult = new MutableLiveData<>();

        private int observeAllCalls;
        private int searchCalls;
        private String searchQuery;

        @Override
        public LiveData<WarehouseItemsResult> observeAll() {
            observeAllCalls++;
            return allItemsResult;
        }

        @Override
        public LiveData<WarehouseItemsResult> search(
                String query
        ) {
            searchCalls++;
            searchQuery = query;
            return searchResult;
        }

        @Override
        public LiveData<WarehouseItemsResult> filter(
                WarehouseItemFilterCriteria criteria
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiveData<WarehouseItemFilterOptionsResult>
        observeFilterOptions() {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiveData<WarehouseItemDetailResult> observeById(
                long warehouseItemId
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void findById(
                long warehouseItemId,
                WarehouseItemFindCallback callback
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
        public void update(
                WarehouseItem warehouseItem,
                WarehouseItemUpdateCallback callback
        ) {
            throw new UnsupportedOperationException();
        }
    }
}
