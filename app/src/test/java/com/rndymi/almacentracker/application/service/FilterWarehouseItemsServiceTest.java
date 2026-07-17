package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.application.port.in.PositionFilter;
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

import org.junit.Rule;
import org.junit.Test;

import java.util.List;

public class FilterWarehouseItemsServiceTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    @Test
    public void normalizesCriteriaBeforeDelegating() {
        FakeRepository repository = new FakeRepository();

        FilterWarehouseItemsService service =
                new FilterWarehouseItemsService(repository);

        LiveData<WarehouseItemsResult> expected =
                new MutableLiveData<>();

        repository.filterResult = expected;

        LiveData<WarehouseItemsResult> actual =
                service.filter(
                        WarehouseItemFilterCriteria.of(
                                "  105  ",
                                "  MR  ",
                                "  A1  ",
                                PositionFilter.exact(
                                        "  Nivel 2  "
                                )
                        )
                );

        assertSame(expected, actual);
        assertEquals(
                "105",
                repository.criteria.getQuery()
        );
        assertEquals(
                "MR",
                repository.criteria.getCategory()
        );
        assertEquals(
                "A1",
                repository.criteria.getSite()
        );
        assertEquals(
                "Nivel 2",
                repository.criteria
                        .getPositionFilter()
                        .getValue()
        );
    }

    @Test
    public void nullCriteriaUsesEmptyCriteria() {
        FakeRepository repository = new FakeRepository();

        FilterWarehouseItemsService service =
                new FilterWarehouseItemsService(repository);

        service.filter(null);

        assertEquals(
                "",
                repository.criteria.getQuery()
        );

        assertEquals(
                PositionFilter.Type.ALL,
                repository.criteria
                        .getPositionFilter()
                        .getType()
        );
    }

    private static final class FakeRepository
            implements WarehouseItemRepository {

        private WarehouseItemFilterCriteria criteria;

        private LiveData<WarehouseItemsResult> filterResult =
                new MutableLiveData<>();

        @Override
        public LiveData<WarehouseItemsResult> observeAll() {
            return new MutableLiveData<>();
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
            this.criteria = criteria;
            return filterResult;
        }

        @Override
        public LiveData<WarehouseItemFilterOptionsResult>
        observeFilterOptions() {
            return new MutableLiveData<>();
        }

        @Override
        public LiveData<WarehouseItemDetailResult> observeById(
                long warehouseItemId
        ) {
            return new MutableLiveData<>();
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

        @Override
        public void deleteById(
                long warehouseItemId,
                WarehouseItemDeleteCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteByIds(
                List<Long> warehouseItemIds,
                WarehouseItemsDeleteCallback callback
        ) {
            throw new UnsupportedOperationException();
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
