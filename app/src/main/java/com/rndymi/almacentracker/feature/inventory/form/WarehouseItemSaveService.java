package com.rndymi.almacentracker.feature.inventory.form;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.domain.rule.WarehouseItemNormalizer;
import com.rndymi.almacentracker.domain.rule.WarehouseItemValidator;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

public class WarehouseItemSaveService {

    private static final WarehouseItemNormalizer NORMALIZER =
            new WarehouseItemNormalizer();
    private static final WarehouseItemValidator VALIDATOR =
            new WarehouseItemValidator();

    private final WarehouseItemRepository repository;
    private final LongSupplier currentTimeProvider;

    public WarehouseItemSaveService(
            WarehouseItemRepository repository,
            LongSupplier currentTimeProvider
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.currentTimeProvider =
                Objects.requireNonNull(currentTimeProvider);
    }

    public void create(
            WarehouseItemFormData formData,
            Consumer<WarehouseItemSaveResult> callback
    ) {
        Objects.requireNonNull(formData);
        Objects.requireNonNull(callback);

        PreparedFormData prepared = prepare(formData);

        if (!validate(prepared, callback)) {
            return;
        }

        repository.existsByCategoryAndCode(
                prepared.category,
                prepared.code,
                new RepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean exists) {
                        if (exists) {
                            callback.accept(
                                    WarehouseItemSaveResult.duplicate()
                            );
                            return;
                        }

                        insert(prepared, callback);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.accept(
                                WarehouseItemSaveResult
                                        .persistenceError(throwable)
                        );
                    }
                }
        );
    }

    public void update(
            long warehouseItemId,
            WarehouseItemFormData formData,
            Consumer<WarehouseItemSaveResult> callback
    ) {
        Objects.requireNonNull(formData);
        Objects.requireNonNull(callback);

        if (warehouseItemId <= 0L) {
            callback.accept(
                    WarehouseItemSaveResult.notFound()
            );
            return;
        }

        PreparedFormData prepared = prepare(formData);

        if (!validate(prepared, callback)) {
            return;
        }

        repository.findById(
                warehouseItemId,
                new RepositoryCallback<WarehouseItem>() {
                    @Override
                    public void onSuccess(WarehouseItem original) {
                        checkDuplicateAndUpdate(
                                original,
                                prepared,
                                callback
                        );
                    }

                    @Override
                    public void onNotFound() {
                        callback.accept(
                                WarehouseItemSaveResult.notFound()
                        );
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.accept(
                                WarehouseItemSaveResult
                                        .persistenceError(throwable)
                        );
                    }
                }
        );
    }

    private PreparedFormData prepare(
            WarehouseItemFormData formData
    ) {
        return new PreparedFormData(
                NORMALIZER.normalizeCategory(
                        formData.getCategory()
                ),
                NORMALIZER.normalizeCode(formData.getCode()),
                NORMALIZER.normalizeSite(formData.getSite()),
                NORMALIZER.normalizeOptional(
                        formData.getPosition()
                ),
                NORMALIZER.normalizeOptional(
                        formData.getObservations()
                )
        );
    }

    private boolean validate(
            PreparedFormData formData,
            Consumer<WarehouseItemSaveResult> callback
    ) {
        WarehouseItemValidator.ValidationResult validation =
                VALIDATOR.validateRequiredFields(
                        formData.category,
                        formData.code,
                        formData.site
                );

        if (validation.isValid()) {
            return true;
        }

        callback.accept(
                WarehouseItemSaveResult.validationError(
                        validation.isMissing(
                                WarehouseItemValidator
                                        .RequiredField.CATEGORY
                        ),
                        validation.isMissing(
                                WarehouseItemValidator
                                        .RequiredField.CODE
                        ),
                        validation.isMissing(
                                WarehouseItemValidator
                                        .RequiredField.SITE
                        )
                )
        );
        return false;
    }

    private void insert(
            PreparedFormData formData,
            Consumer<WarehouseItemSaveResult> callback
    ) {
        long currentTime = currentTimeProvider.getAsLong();

        WarehouseItem warehouseItem = new WarehouseItem(
                0L,
                formData.category,
                formData.code,
                formData.site,
                formData.position,
                formData.observations,
                currentTime,
                currentTime
        );

        repository.insert(
                warehouseItem,
                new RepositoryCallback<Long>() {
                    @Override
                    public void onSuccess(Long createdItemId) {
                        callback.accept(
                                WarehouseItemSaveResult.success(
                                        createdItemId
                                )
                        );
                    }

                    @Override
                    public void onDuplicate(Throwable cause) {
                        callback.accept(
                                WarehouseItemSaveResult.duplicate()
                        );
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.accept(
                                WarehouseItemSaveResult
                                        .persistenceError(throwable)
                        );
                    }
                }
        );
    }

    private void checkDuplicateAndUpdate(
            WarehouseItem original,
            PreparedFormData formData,
            Consumer<WarehouseItemSaveResult> callback
    ) {
        repository.existsByCategoryAndCodeExcludingId(
                formData.category,
                formData.code,
                original.getId(),
                new RepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean exists) {
                        if (exists) {
                            callback.accept(
                                    WarehouseItemSaveResult.duplicate()
                            );
                            return;
                        }

                        updateExistingItem(
                                original,
                                formData,
                                callback
                        );
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.accept(
                                WarehouseItemSaveResult
                                        .persistenceError(throwable)
                        );
                    }
                }
        );
    }

    private void updateExistingItem(
            WarehouseItem original,
            PreparedFormData formData,
            Consumer<WarehouseItemSaveResult> callback
    ) {
        WarehouseItem updatedItem = new WarehouseItem(
                original.getId(),
                formData.category,
                formData.code,
                formData.site,
                formData.position,
                formData.observations,
                original.getCreatedAt(),
                currentTimeProvider.getAsLong()
        );

        repository.update(
                updatedItem,
                new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void ignored) {
                        callback.accept(
                                WarehouseItemSaveResult.success(
                                        original.getId()
                                )
                        );
                    }

                    @Override
                    public void onDuplicate(Throwable cause) {
                        callback.accept(
                                WarehouseItemSaveResult.duplicate()
                        );
                    }

                    @Override
                    public void onNotFound() {
                        callback.accept(
                                WarehouseItemSaveResult.notFound()
                        );
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.accept(
                                WarehouseItemSaveResult
                                        .persistenceError(throwable)
                        );
                    }
                }
        );
    }

    private static final class PreparedFormData {

        private final String category;
        private final String code;
        private final String site;
        private final String position;
        private final String observations;

        private PreparedFormData(
                String category,
                String code,
                String site,
                String position,
                String observations
        ) {
            this.category = category;
            this.code = code;
            this.site = site;
            this.position = position;
            this.observations = observations;
        }
    }
}
