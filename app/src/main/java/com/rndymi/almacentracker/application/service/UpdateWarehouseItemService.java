package com.rndymi.almacentracker.application.service;

import com.rndymi.almacentracker.application.port.in.UpdateWarehouseItemCommand;
import com.rndymi.almacentracker.application.port.in.UpdateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDuplicateCheckCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemUpdateCallback;
import com.rndymi.almacentracker.application.result.UpdateWarehouseItemResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

public final class UpdateWarehouseItemService
        implements UpdateWarehouseItemUseCase {

    private final WarehouseItemRepository repository;
    private final LongSupplier currentTimeProvider;

    public UpdateWarehouseItemService(
            WarehouseItemRepository repository,
            LongSupplier currentTimeProvider
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.currentTimeProvider =
                Objects.requireNonNull(currentTimeProvider);
    }

    @Override
    public void updateWarehouseItem(
            UpdateWarehouseItemCommand command,
            Consumer<UpdateWarehouseItemResult> callback
    ) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(callback);

        if (command.getWarehouseItemId() <= 0L) {
            callback.accept(
                    UpdateWarehouseItemResult.notFound()
            );
            return;
        }

        String category = normalizeUppercase(
                command.getCategory()
        );

        String code = normalizeUppercase(
                command.getCode()
        );

        String site = normalizeUppercase(
                command.getSite()
        );

        String position = normalizeOptional(
                command.getPosition()
        );

        String observations = normalizeOptional(
                command.getObservations()
        );

        boolean categoryRequired = category.isEmpty();
        boolean codeRequired = code.isEmpty();
        boolean siteRequired = site.isEmpty();

        if (categoryRequired || codeRequired || siteRequired) {
            callback.accept(
                    UpdateWarehouseItemResult.validationError(
                            categoryRequired,
                            codeRequired,
                            siteRequired
                    )
            );
            return;
        }

        repository.findById(
                command.getWarehouseItemId(),
                new WarehouseItemFindCallback() {
                    @Override
                    public void onFound(
                            WarehouseItem original
                    ) {
                        checkDuplicateAndUpdate(
                                original,
                                category,
                                code,
                                site,
                                position,
                                observations,
                                callback
                        );
                    }

                    @Override
                    public void onNotFound() {
                        callback.accept(
                                UpdateWarehouseItemResult.notFound()
                        );
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.accept(
                                UpdateWarehouseItemResult
                                        .persistenceError(
                                                throwable
                                        )
                        );
                    }
                }
        );
    }

    private void checkDuplicateAndUpdate(
            WarehouseItem original,
            String category,
            String code,
            String site,
            String position,
            String observations,
            Consumer<UpdateWarehouseItemResult> callback
    ) {
        repository.existsByCategoryAndCodeExcludingId(
                category,
                code,
                original.getId(),
                new WarehouseItemDuplicateCheckCallback() {
                    @Override
                    public void onResult(boolean exists) {
                        if (exists) {
                            callback.accept(
                                    UpdateWarehouseItemResult
                                            .duplicate()
                            );
                            return;
                        }

                        updateExistingItem(
                                original,
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
                                UpdateWarehouseItemResult
                                        .persistenceError(
                                                throwable
                                        )
                        );
                    }
                }
        );
    }

    private void updateExistingItem(
            WarehouseItem original,
            String category,
            String code,
            String site,
            String position,
            String observations,
            Consumer<UpdateWarehouseItemResult> callback
    ) {
        WarehouseItem updatedItem = new WarehouseItem(
                original.getId(),
                category,
                code,
                site,
                position,
                observations,
                original.getCreatedAt(),
                currentTimeProvider.getAsLong()
        );

        repository.update(
                updatedItem,
                new WarehouseItemUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        callback.accept(
                                UpdateWarehouseItemResult.success()
                        );
                    }

                    @Override
                    public void onDuplicate() {
                        /*
                         * The unique Room index remains the final
                         * protection after the previous check.
                         */
                        callback.accept(
                                UpdateWarehouseItemResult.duplicate()
                        );
                    }

                    @Override
                    public void onNotFound() {
                        callback.accept(
                                UpdateWarehouseItemResult.notFound()
                        );
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.accept(
                                UpdateWarehouseItemResult
                                        .persistenceError(
                                                throwable
                                        )
                        );
                    }
                }
        );
    }

    private String normalizeUppercase(String value) {
        return value == null
                ? ""
                : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}