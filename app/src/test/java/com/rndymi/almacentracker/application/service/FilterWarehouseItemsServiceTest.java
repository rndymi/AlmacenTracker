package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.application.port.in.PositionFilter;
import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;

import org.junit.Rule;
import org.junit.Test;

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
            extends WarehouseItemRepositoryStub {

        private WarehouseItemFilterCriteria criteria;

        private LiveData<WarehouseItemsResult> filterResult =
                new MutableLiveData<>();

        @Override
        public LiveData<WarehouseItemsResult> filter(
                WarehouseItemFilterCriteria criteria
        ) {
            this.criteria = criteria;
            return filterResult;
        }

    }
}
