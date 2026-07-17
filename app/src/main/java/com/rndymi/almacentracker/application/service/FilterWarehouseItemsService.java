package com.rndymi.almacentracker.application.service;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.port.in.FilterWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.in.PositionFilter;
import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;

import java.util.Objects;

public final class FilterWarehouseItemsService
        implements FilterWarehouseItemsUseCase {

    private final WarehouseItemRepository repository;

    public FilterWarehouseItemsService(
            WarehouseItemRepository repository
    ) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public LiveData<WarehouseItemsResult> filter(
            WarehouseItemFilterCriteria criteria
    ) {
        WarehouseItemFilterCriteria safeCriteria =
                criteria == null
                        ? WarehouseItemFilterCriteria.empty()
                        : WarehouseItemFilterCriteria.of(
                        criteria.getQuery(),
                        criteria.getCategory(),
                        criteria.getSite(),
                        normalizePositionFilter(
                                criteria.getPositionFilter()
                        )
                );

        return repository.filter(safeCriteria);
    }

    private PositionFilter normalizePositionFilter(
            PositionFilter filter
    ) {
        if (filter == null) {
            return PositionFilter.all();
        }

        if (filter.getType()
                != PositionFilter.Type.EXACT_VALUE) {
            return filter;
        }

        return PositionFilter.exact(filter.getValue());
    }
}