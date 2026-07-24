package com.rndymi.almacentracker.application.service;

import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemCommand;
import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDuplicateCheckCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.CreateWarehouseItemResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.domain.rule.WarehouseItemNormalizer;
import com.rndymi.almacentracker.domain.rule.WarehouseItemValidator;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

public final class CreateWarehouseItemService
        implements CreateWarehouseItemUseCase {

    private static final WarehouseItemNormalizer NORMALIZER =
            new WarehouseItemNormalizer();
    private static final WarehouseItemValidator VALIDATOR =
            new WarehouseItemValidator();

    private final WarehouseItemRepository repository;
    private final LongSupplier currentTimeProvider;

    public CreateWarehouseItemService(
            WarehouseItemRepository repository,
            LongSupplier currentTimeProvider
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.currentTimeProvider =
                Objects.requireNonNull(currentTimeProvider);
    }

    @Override
    public void createWarehouseItem(
            CreateWarehouseItemCommand command,
            Consumer<CreateWarehouseItemResult> callback
    ) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(callback);

        String category = NORMALIZER.normalizeCategory(
                command.getCategory()
        );

        String code = NORMALIZER.normalizeCode(
                command.getCode()
        );

        String site = NORMALIZER.normalizeSite(
                command.getSite()
        );

        String position = NORMALIZER.normalizeOptional(
                command.getPosition()
        );

        String observations = NORMALIZER.normalizeOptional(
                command.getObservations()
        );

        WarehouseItemValidator.ValidationResult validation =
                VALIDATOR.validateRequiredFields(
                        category,
                        code,
                        site
                );

        if (!validation.isValid()) {
            callback.accept(
                    CreateWarehouseItemResult.validationError(
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
            return;
        }

        repository.existsByCategoryAndCode(
                category,
                code,
                new WarehouseItemDuplicateCheckCallback() {
                    @Override
                    public void onResult(boolean exists) {
                        if (exists) {
                            callback.accept(
                                    CreateWarehouseItemResult
                                            .duplicate()
                            );
                            return;
                        }

                        insertWarehouseItem(
                                category,
                                code,
                                site,
                                position,
                                observations,
                                callback
                        );
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.accept(
                                CreateWarehouseItemResult
                                        .persistenceError(
                                                throwable
                                        )
                        );
                    }
                }
        );
    }

    private void insertWarehouseItem(
            String category,
            String code,
            String site,
            String position,
            String observations,
            Consumer<CreateWarehouseItemResult> callback
    ) {
        long currentTime = currentTimeProvider.getAsLong();

        WarehouseItem warehouseItem = new WarehouseItem(
                0L,
                category,
                code,
                site,
                position,
                observations,
                currentTime,
                currentTime
        );

        repository.insert(
                warehouseItem,
                new WarehouseItemInsertCallback() {
                    @Override
                    public void onSuccess(long createdItemId) {
                        callback.accept(
                                CreateWarehouseItemResult.success(
                                        createdItemId
                                )
                        );
                    }

                    @Override
                    public void onDuplicate() {
                        callback.accept(
                                CreateWarehouseItemResult.duplicate()
                        );
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.accept(
                                CreateWarehouseItemResult
                                        .persistenceError(
                                                throwable
                                        )
                        );
                    }
                }
        );
    }

}
