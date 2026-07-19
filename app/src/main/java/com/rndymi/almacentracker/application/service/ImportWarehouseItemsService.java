package com.rndymi.almacentracker.application.service;

import com.rndymi.almacentracker.application.port.in.ImportWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvReadCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvReader;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsInsertCallback;
import com.rndymi.almacentracker.application.result.ImportWarehouseItemsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvReadResult;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvRow;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.LongSupplier;

public final class ImportWarehouseItemsService
        implements ImportWarehouseItemsUseCase {

    private final WarehouseItemCsvReader csvReader;
    private final WarehouseItemRepository repository;
    private final LongSupplier currentTimeSupplier;

    public ImportWarehouseItemsService(
            WarehouseItemCsvReader csvReader,
            WarehouseItemRepository repository,
            LongSupplier currentTimeSupplier
    ) {
        this.csvReader =
                Objects.requireNonNull(csvReader);

        this.repository =
                Objects.requireNonNull(repository);

        this.currentTimeSupplier =
                Objects.requireNonNull(
                        currentTimeSupplier
                );
    }

    @Override
    public void importWarehouseItems(
            String sourceReference,
            Callback callback
    ) {
        Objects.requireNonNull(callback);

        if (sourceReference == null
                || sourceReference.trim().isEmpty()) {
            callback.onResult(
                    ImportWarehouseItemsResult.of(
                            ImportWarehouseItemsResult.Status
                                    .INVALID_SOURCE
                    )
            );
            return;
        }

        csvReader.read(
                sourceReference,
                new WarehouseItemCsvReadCallback() {
                    @Override
                    public void onSuccess(
                            WarehouseItemCsvReadResult result
                    ) {
                        classifyAgainstExistingItems(
                                result,
                                callback
                        );
                    }

                    @Override
                    public void onInvalidFormat() {
                        callback.onResult(
                                ImportWarehouseItemsResult.of(
                                        ImportWarehouseItemsResult
                                                .Status
                                                .INVALID_FORMAT
                                )
                        );
                    }

                    @Override
                    public void onReadError(
                            Throwable throwable
                    ) {
                        callback.onResult(
                                ImportWarehouseItemsResult.of(
                                        ImportWarehouseItemsResult
                                                .Status
                                                .READ_ERROR
                                )
                        );
                    }

                    @Override
                    public void onUnknownError(
                            Throwable throwable
                    ) {
                        callback.onResult(
                                ImportWarehouseItemsResult.of(
                                        ImportWarehouseItemsResult
                                                .Status
                                                .UNKNOWN_ERROR
                                )
                        );
                    }
                }
        );
    }

    private void classifyAgainstExistingItems(
            WarehouseItemCsvReadResult csvResult,
            Callback callback
    ) {
        repository.findAll(
                new WarehouseItemsFindCallback() {
                    @Override
                    public void onSuccess(
                            List<WarehouseItem> existingItems
                    ) {
                        prepareImportBatch(
                                csvResult,
                                existingItems,
                                callback
                        );
                    }

                    @Override
                    public void onError(
                            Throwable throwable
                    ) {
                        callback.onResult(
                                ImportWarehouseItemsResult.of(
                                        ImportWarehouseItemsResult
                                                .Status
                                                .PERSISTENCE_ERROR
                                )
                        );
                    }
                }
        );
    }

    private void prepareImportBatch(
            WarehouseItemCsvReadResult csvResult,
            List<WarehouseItem> existingItems,
            Callback callback
    ) {
        Set<String> existingIdentities =
                buildExistingIdentities(existingItems);

        Set<String> acceptedFileIdentities =
                new HashSet<>();

        List<WarehouseItem> acceptedItems =
                new ArrayList<>();

        int duplicateCount = 0;
        int invalidCount =
                csvResult.getInvalidRowCount();

        long importTimestamp =
                currentTimeSupplier.getAsLong();

        for (WarehouseItemCsvRow row
                : csvResult.getRows()) {

            NormalizedRow normalized =
                    normalize(row);

            if (!normalized.isValid()) {
                invalidCount++;
                continue;
            }

            String identity =
                    createIdentity(
                            normalized.category,
                            normalized.code
                    );

            if (existingIdentities.contains(identity)
                    || !acceptedFileIdentities.add(
                    identity
            )) {
                duplicateCount++;
                continue;
            }

            acceptedItems.add(
                    new WarehouseItem(
                            0L,
                            normalized.category,
                            normalized.code,
                            normalized.site,
                            normalized.position,
                            normalized.observations,
                            importTimestamp,
                            importTimestamp
                    )
            );
        }

        if (acceptedItems.isEmpty()) {
            callback.onResult(
                    ImportWarehouseItemsResult.noValidRows(
                            csvResult.getTotalRows(),
                            duplicateCount,
                            invalidCount
                    )
            );
            return;
        }

        persistBatch(
                acceptedItems,
                csvResult.getTotalRows(),
                duplicateCount,
                invalidCount,
                callback
        );
    }

    private void persistBatch(
            List<WarehouseItem> acceptedItems,
            int totalRows,
            int duplicateCount,
            int invalidCount,
            Callback callback
    ) {
        repository.insertAll(
                acceptedItems,
                new WarehouseItemsInsertCallback() {
                    @Override
                    public void onSuccess(
                            int insertedCount
                    ) {
                        callback.onResult(
                                ImportWarehouseItemsResult.success(
                                        totalRows,
                                        insertedCount,
                                        duplicateCount,
                                        invalidCount
                                )
                        );
                    }

                    @Override
                    public void onDuplicate(
                            Throwable throwable
                    ) {
                        callback.onResult(
                                ImportWarehouseItemsResult.of(
                                        ImportWarehouseItemsResult
                                                .Status
                                                .PERSISTENCE_ERROR
                                )
                        );
                    }

                    @Override
                    public void onError(
                            Throwable throwable
                    ) {
                        callback.onResult(
                                ImportWarehouseItemsResult.of(
                                        ImportWarehouseItemsResult
                                                .Status
                                                .PERSISTENCE_ERROR
                                )
                        );
                    }
                }
        );
    }

    private Set<String> buildExistingIdentities(
            List<WarehouseItem> existingItems
    ) {
        Set<String> identities = new HashSet<>();

        if (existingItems == null) {
            return identities;
        }

        for (WarehouseItem item : existingItems) {
            identities.add(
                    createIdentity(
                            normalizeRequired(
                                    item.getCategory()
                            ),
                            normalizeRequired(
                                    item.getCode()
                            )
                    )
            );
        }

        return identities;
    }

    private NormalizedRow normalize(
            WarehouseItemCsvRow row
    ) {
        return new NormalizedRow(
                normalizeRequired(row.getCategory()),
                normalizeRequired(row.getCode()),
                normalizeRequired(row.getSite()),
                normalizeOptional(row.getPosition()),
                normalizeOptional(row.getObservations())
        );
    }

    private String normalizeRequired(
            String value
    ) {
        return value == null
                ? ""
                : value.trim().toUpperCase(
                Locale.ROOT
        );
    }

    private String normalizeOptional(
            String value
    ) {
        if (value == null
                || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String createIdentity(
            String category,
            String code
    ) {
        return category + '\u0000' + code;
    }

    private static final class NormalizedRow {

        private final String category;
        private final String code;
        private final String site;
        private final String position;
        private final String observations;

        private NormalizedRow(
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

        private boolean isValid() {
            return !category.isEmpty()
                    && !code.isEmpty()
                    && !site.isEmpty();
        }
    }
}