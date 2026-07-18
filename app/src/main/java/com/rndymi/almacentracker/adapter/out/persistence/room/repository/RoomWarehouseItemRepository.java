package com.rndymi.almacentracker.adapter.out.persistence.room.repository;

import android.database.sqlite.SQLiteConstraintException;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.rndymi.almacentracker.adapter.out.persistence.room.dao.WarehouseItemDao;
import com.rndymi.almacentracker.adapter.out.persistence.room.entity.WarehouseItemEntity;
import com.rndymi.almacentracker.adapter.out.persistence.room.mapper.WarehouseItemPersistenceMapper;
import com.rndymi.almacentracker.application.port.in.PositionFilter;
import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDuplicateCheckCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemUpdateCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsFindCallback;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptions;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

public final class RoomWarehouseItemRepository
        implements WarehouseItemRepository {

    private static final int POSITION_MODE_ALL = 0;
    private static final int POSITION_MODE_WITHOUT_POSITION = 1;
    private static final int POSITION_MODE_EXACT_VALUE = 2;

    private final WarehouseItemDao warehouseItemDao;
    private final WarehouseItemPersistenceMapper mapper;
    private final Executor executor;

    public RoomWarehouseItemRepository(
            WarehouseItemDao warehouseItemDao,
            WarehouseItemPersistenceMapper mapper,
            Executor executor
    ) {
        this.warehouseItemDao = Objects.requireNonNull(
                warehouseItemDao
        );
        this.mapper = Objects.requireNonNull(mapper);
        this.executor = Objects.requireNonNull(executor);
    }

    @Override
    public LiveData<WarehouseItemsResult> observeAll() {
        return mapWarehouseItems(
                warehouseItemDao.observeAll()
        );
    }

    @Override
    public LiveData<WarehouseItemsResult> search(String query) {
        Objects.requireNonNull(query);

        return mapWarehouseItems(
                warehouseItemDao.search(query)
        );
    }

    @Override
    public LiveData<WarehouseItemsResult> filter(
            WarehouseItemFilterCriteria criteria
    ) {
        Objects.requireNonNull(criteria);

        PositionFilter positionFilter =
                criteria.getPositionFilter();

        int positionMode = toPositionMode(positionFilter);

        String positionValue =
                positionFilter.getType()
                        == PositionFilter.Type.EXACT_VALUE
                        ? positionFilter.getValue()
                        : null;

        return mapWarehouseItems(
                warehouseItemDao.filter(
                        criteria.getQuery(),
                        criteria.getCategory(),
                        criteria.getSite(),
                        positionMode,
                        positionValue
                )
        );
    }

    @Override
    public LiveData<WarehouseItemFilterOptionsResult>
    observeFilterOptions() {
        MediatorLiveData<WarehouseItemFilterOptionsResult> result =
                new MediatorLiveData<>();

        LiveData<List<String>> categoriesSource =
                warehouseItemDao.observeCategories();

        LiveData<List<String>> sitesSource =
                warehouseItemDao.observeSites();

        LiveData<List<String>> positionsSource =
                warehouseItemDao.observePositions();

        LiveData<Integer> withoutPositionCountSource =
                warehouseItemDao.observeWithoutPositionCount();

        FilterOptionsAccumulator accumulator =
                new FilterOptionsAccumulator();

        result.addSource(
                categoriesSource,
                categories -> {
                    accumulator.categories =
                            safeList(categories);
                    accumulator.categoriesLoaded = true;
                    emitFilterOptions(result, accumulator);
                }
        );

        result.addSource(
                sitesSource,
                sites -> {
                    accumulator.sites = safeList(sites);
                    accumulator.sitesLoaded = true;
                    emitFilterOptions(result, accumulator);
                }
        );

        result.addSource(
                positionsSource,
                positions -> {
                    accumulator.positions =
                            safeList(positions);
                    accumulator.positionsLoaded = true;
                    emitFilterOptions(result, accumulator);
                }
        );

        result.addSource(
                withoutPositionCountSource,
                count -> {
                    accumulator.hasItemsWithoutPosition =
                            count != null && count > 0;

                    accumulator.withoutPositionLoaded = true;
                    emitFilterOptions(result, accumulator);
                }
        );

        return result;
    }

    @Override
    public LiveData<WarehouseItemDetailResult> observeById(
            long warehouseItemId
    ) {
        return Transformations.map(
                warehouseItemDao.observeById(warehouseItemId),
                entity -> {
                    if (entity == null) {
                        return WarehouseItemDetailResult.notFound();
                    }

                    try {
                        return WarehouseItemDetailResult.found(
                                mapper.toDomain(entity)
                        );
                    } catch (RuntimeException exception) {
                        return WarehouseItemDetailResult.error(
                                exception
                        );
                    }
                }
        );
    }

    @Override
    public void findAll(
            WarehouseItemsFindCallback callback
    ) {
        Objects.requireNonNull(callback);

        executor.execute(() -> {
            try {
                List<WarehouseItemEntity> entities =
                        warehouseItemDao.findAll();

                callback.onSuccess(
                        mapper.toDomainList(entities)
                );
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void findById(
            long warehouseItemId,
            WarehouseItemFindCallback callback
    ) {
        Objects.requireNonNull(callback);

        executor.execute(() -> {
            try {
                WarehouseItemEntity entity =
                        warehouseItemDao.findById(
                                warehouseItemId
                        );

                if (entity == null) {
                    callback.onNotFound();
                    return;
                }

                callback.onFound(
                        mapper.toDomain(entity)
                );
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void existsByCategoryAndCode(
            String category,
            String code,
            WarehouseItemDuplicateCheckCallback callback
    ) {
        Objects.requireNonNull(category);
        Objects.requireNonNull(code);
        Objects.requireNonNull(callback);

        executor.execute(() -> {
            try {
                boolean exists =
                        warehouseItemDao
                                .existsByCategoryAndCode(
                                        category,
                                        code
                                );

                callback.onResult(exists);
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void existsByCategoryAndCodeExcludingId(
            String category,
            String code,
            long excludedWarehouseItemId,
            WarehouseItemDuplicateCheckCallback callback
    ) {
        Objects.requireNonNull(category);
        Objects.requireNonNull(code);
        Objects.requireNonNull(callback);

        executor.execute(() -> {
            try {
                boolean exists =
                        warehouseItemDao
                                .existsByCategoryAndCodeExcludingId(
                                        category,
                                        code,
                                        excludedWarehouseItemId
                                );

                callback.onResult(exists);
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void update(
            WarehouseItem warehouseItem,
            WarehouseItemUpdateCallback callback
    ) {
        Objects.requireNonNull(warehouseItem);
        Objects.requireNonNull(callback);

        executor.execute(() -> {
            try {
                WarehouseItemEntity entity =
                        mapper.toEntity(warehouseItem);

                int affectedRows =
                        warehouseItemDao.update(entity);

                if (affectedRows == 0) {
                    callback.onNotFound();
                    return;
                }

                callback.onSuccess();
            } catch (SQLiteConstraintException exception) {
                callback.onDuplicate();
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void deleteById(
            long warehouseItemId,
            WarehouseItemDeleteCallback callback
    ) {
        Objects.requireNonNull(callback);

        executor.execute(() -> {
            int affectedRows;

            try {
                affectedRows =
                        warehouseItemDao.deleteById(
                                warehouseItemId
                        );
            } catch (RuntimeException exception) {
                callback.onError(exception);
                return;
            }

            if (affectedRows == 0) {
                callback.onNotFound();
                return;
            }

            callback.onSuccess();
        });
    }

    @Override
    public void deleteByIds(
            List<Long> warehouseItemIds,
            WarehouseItemsDeleteCallback callback
    ) {
        Objects.requireNonNull(warehouseItemIds);
        Objects.requireNonNull(callback);

        List<Long> idsCopy =
                Collections.unmodifiableList(
                        new ArrayList<>(warehouseItemIds)
                );

        executor.execute(() -> {
            try {
                int deletedCount =
                        warehouseItemDao.deleteByIds(
                                idsCopy
                        );

                callback.onComplete(deletedCount);
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void insert(
            WarehouseItem warehouseItem,
            WarehouseItemInsertCallback callback
    ) {
        Objects.requireNonNull(warehouseItem);
        Objects.requireNonNull(callback);

        executor.execute(() -> {
            try {
                WarehouseItemEntity entity =
                        mapper.toEntity(warehouseItem);

                long generatedId =
                        warehouseItemDao.insert(entity);

                callback.onSuccess(generatedId);
            } catch (SQLiteConstraintException exception) {
                callback.onDuplicate();
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    private LiveData<WarehouseItemsResult> mapWarehouseItems(
            LiveData<List<WarehouseItemEntity>> source
    ) {
        return Transformations.map(
                source,
                entities -> {
                    try {
                        return WarehouseItemsResult.success(
                                mapper.toDomainList(entities)
                        );
                    } catch (RuntimeException exception) {
                        return WarehouseItemsResult.error(
                                exception
                        );
                    }
                }
        );
    }

    private int toPositionMode(
            PositionFilter positionFilter
    ) {
        switch (positionFilter.getType()) {
            case WITHOUT_POSITION:
                return POSITION_MODE_WITHOUT_POSITION;

            case EXACT_VALUE:
                return POSITION_MODE_EXACT_VALUE;

            case ALL:
            default:
                return POSITION_MODE_ALL;
        }
    }

    private void emitFilterOptions(
            MediatorLiveData<WarehouseItemFilterOptionsResult> result,
            FilterOptionsAccumulator accumulator
    ) {
        if (!accumulator.isComplete()) {
            return;
        }

        WarehouseItemFilterOptions options =
                new WarehouseItemFilterOptions(
                        accumulator.categories,
                        accumulator.sites,
                        accumulator.positions,
                        accumulator.hasItemsWithoutPosition
                );

        result.setValue(
                WarehouseItemFilterOptionsResult.success(options)
        );
    }

    private List<String> safeList(List<String> values) {
        return values == null
                ? Collections.emptyList()
                : values;
    }

    private static final class FilterOptionsAccumulator {

        private List<String> categories =
                Collections.emptyList();

        private List<String> sites =
                Collections.emptyList();

        private List<String> positions =
                Collections.emptyList();

        private boolean hasItemsWithoutPosition;

        private boolean categoriesLoaded;
        private boolean sitesLoaded;
        private boolean positionsLoaded;
        private boolean withoutPositionLoaded;

        private boolean isComplete() {
            return categoriesLoaded
                    && sitesLoaded
                    && positionsLoaded
                    && withoutPositionLoaded;
        }
    }
}
