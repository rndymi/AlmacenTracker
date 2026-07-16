package com.rndymi.almacentracker.application.service;

import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemCommand;
import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.CreateWarehouseItemResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

public final class CreateWarehouseItemService
        implements CreateWarehouseItemUseCase {
    private final WarehouseItemRepository repository;
    private final LongSupplier currentTimeProvider;

    public CreateWarehouseItemService(
            WarehouseItemRepository repository,
            LongSupplier currentTimeProvider
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.currentTimeProvider = Objects.requireNonNull(currentTimeProvider);
    }

    @Override
    public void createWarehouseItem(
            CreateWarehouseItemCommand command,
            Consumer<CreateWarehouseItemResult> callback
    ) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(callback);

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
                    CreateWarehouseItemResult.validationError(
                            categoryRequired,
                            codeRequired,
                            siteRequired
                    )
            );
            return;
        }

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
                                        .persistenceError(throwable)
                        );
                    }
                }
        );
    }

    private String normalizeUppercase(String value) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toUpperCase(Locale.ROOT);
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
