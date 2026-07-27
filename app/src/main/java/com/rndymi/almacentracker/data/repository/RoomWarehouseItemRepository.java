package com.rndymi.almacentracker.data.repository;

import android.database.sqlite.SQLiteConstraintException;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.rndymi.almacentracker.data.local.room.dao.WarehouseItemDao;
import com.rndymi.almacentracker.data.local.room.entity.WarehouseItemEntity;
import com.rndymi.almacentracker.data.local.room.mapper.WarehouseItemRoomMapper;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceLocation;

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
    private final WarehouseItemRoomMapper mapper;
    private final Executor executor;

    public RoomWarehouseItemRepository(
            WarehouseItemDao warehouseItemDao,
            WarehouseItemRoomMapper mapper,
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
            RepositoryCallback<List<WarehouseItem>> callback
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
            RepositoryCallback<WarehouseItem> callback
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

                callback.onSuccess(
                        mapper.toDomain(entity)
                );
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void findAllByCode(
            String code,
            RepositoryCallback<List<WarehouseItem>> callback
    ) {
        Objects.requireNonNull(code);
        Objects.requireNonNull(callback);

        executor.execute(() -> {
            try {
                List<WarehouseItemEntity> entities =
                        warehouseItemDao.findAllByCode(code);

                callback.onSuccess(
                        mapper.toDomainList(entities)
                );
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void findAllByReferences(
            List<WarehouseReference> references,
            RepositoryCallback<
                    List<WarehouseReferenceLocation>
                    > callback
    ) {
        Objects.requireNonNull(references);
        Objects.requireNonNull(callback);

        List<WarehouseReference> referencesCopy =
                Collections.unmodifiableList(
                        new ArrayList<>(references)
                );

        executor.execute(() -> {
            try {
                List<WarehouseReferenceLocation> locations =
                        new ArrayList<>(
                                referencesCopy.size()
                        );

                for (
                        WarehouseReference reference
                        : referencesCopy
                ) {
                    if (reference == null) {
                        continue;
                    }

                    WarehouseItemEntity entity =
                            warehouseItemDao
                                    .findByCategoryAndCode(
                                            reference.getCategory(),
                                            reference.getCode()
                                    );

                    if (entity == null) {
                        locations.add(
                                WarehouseReferenceLocation
                                        .notFound(reference)
                        );

                        continue;
                    }

                    WarehouseItem warehouseItem =
                            mapper.toDomain(entity);

                    locations.add(
                            WarehouseReferenceLocation.found(
                                    reference,
                                    warehouseItem.getId(),
                                    warehouseItem.getSite(),
                                    warehouseItem.getPosition()
                            )
                    );
                }

                callback.onSuccess(
                        Collections.unmodifiableList(
                                locations
                        )
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
            RepositoryCallback<Boolean> callback
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

                callback.onSuccess(exists);
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
            RepositoryCallback<Boolean> callback
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

                callback.onSuccess(exists);
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void update(
            WarehouseItem warehouseItem,
            RepositoryCallback<Void> callback
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

                callback.onSuccess(null);
            } catch (SQLiteConstraintException exception) {
                callback.onDuplicate(exception);
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void insertAll(
            List<WarehouseItem> warehouseItems,
            RepositoryCallback<Integer> callback
    ) {
        Objects.requireNonNull(warehouseItems);
        Objects.requireNonNull(callback);

        executor.execute(() -> {
            try {
                List<WarehouseItemEntity> entities =
                        new ArrayList<>(
                                warehouseItems.size()
                        );

                for (WarehouseItem warehouseItem
                        : warehouseItems) {
                    entities.add(
                            mapper.toEntity(warehouseItem)
                    );
                }

                List<Long> generatedIds =
                        warehouseItemDao.insertAll(
                                entities
                        );

                callback.onSuccess(
                        generatedIds.size()
                );
            } catch (SQLiteConstraintException exception) {
                callback.onDuplicate(exception);
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void replaceAll(
            List<WarehouseItem> warehouseItems,
            RepositoryCallback<Integer> callback
    ) {
        Objects.requireNonNull(warehouseItems);
        Objects.requireNonNull(callback);

        List<WarehouseItem> itemsCopy =
                Collections.unmodifiableList(
                        new ArrayList<>(warehouseItems)
                );

        executor.execute(() -> {
            try {
                List<WarehouseItemEntity> entities =
                        new ArrayList<>(itemsCopy.size());

                for (WarehouseItem warehouseItem
                        : itemsCopy) {
                    WarehouseItem restoredItem =
                            new WarehouseItem(
                                    0L,
                                    warehouseItem.getCategory(),
                                    warehouseItem.getCode(),
                                    warehouseItem.getSite(),
                                    warehouseItem.getPosition(),
                                    warehouseItem.getObservations(),
                                    warehouseItem.getCreatedAt(),
                                    warehouseItem.getUpdatedAt()
                            );

                    entities.add(
                            mapper.toEntity(restoredItem)
                    );
                }

                List<Long> generatedIds =
                        warehouseItemDao.replaceAll(entities);

                callback.onSuccess(
                        generatedIds.size()
                );
            } catch (SQLiteConstraintException exception) {
                callback.onDuplicate(exception);
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void deleteById(
            long warehouseItemId,
            RepositoryCallback<Void> callback
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

            callback.onSuccess(null);
        });
    }

    @Override
    public void deleteByIds(
            List<Long> warehouseItemIds,
            RepositoryCallback<Integer> callback
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

                callback.onSuccess(deletedCount);
            } catch (RuntimeException exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void insert(
            WarehouseItem warehouseItem,
            RepositoryCallback<Long> callback
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
                callback.onDuplicate(exception);
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
