package com.rndymi.almacentracker.application.service;

import com.rndymi.almacentracker.application.port.in.ImportWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvReadCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvReader;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsInsertCallback;
import com.rndymi.almacentracker.application.result.ImportWarehouseItemIssue;
import com.rndymi.almacentracker.application.result.ImportWarehouseItemsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvReadResult;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvRow;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
                            ImportWarehouseItemsResult
                                    .Status.INVALID_SOURCE
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
                                                .Status.READ_ERROR
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
                                                .Status.UNKNOWN_ERROR
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
                                ImportWarehouseItemsResult
                                        .persistenceError(
                                                csvResult
                                                        .getTotalRows(),
                                                csvResult
                                                        .getParsingIssues()
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

        Map<String, Integer> acceptedFileRows =
                new HashMap<>();

        List<WarehouseItem> acceptedItems =
                new ArrayList<>();

        List<ImportWarehouseItemIssue> issues =
                new ArrayList<>(
                        csvResult.getParsingIssues()
                );

        long importTimestamp =
                currentTimeSupplier.getAsLong();

        for (WarehouseItemCsvRow row
                : csvResult.getRows()) {

            NormalizedRow normalized =
                    normalize(row);

            List<ImportWarehouseItemIssue>
                    validationIssues =
                    validateRequiredFields(
                            row.getRowNumber(),
                            normalized
                    );

            if (!validationIssues.isEmpty()) {
                issues.addAll(validationIssues);
                continue;
            }

            String identity =
                    createIdentity(
                            normalized.category,
                            normalized.code
                    );

            if (existingIdentities.contains(identity)) {
                issues.add(
                        ImportWarehouseItemIssue
                                .duplicateExisting(
                                        row.getRowNumber(),
                                        normalized.category,
                                        normalized.code
                                )
                );
                continue;
            }

            Integer originalRow =
                    acceptedFileRows.get(identity);

            if (originalRow != null) {
                issues.add(
                        ImportWarehouseItemIssue
                                .duplicateInFile(
                                        row.getRowNumber(),
                                        normalized.category,
                                        normalized.code,
                                        originalRow
                                )
                );
                continue;
            }

            acceptedFileRows.put(
                    identity,
                    row.getRowNumber()
            );

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

        sortIssues(issues);

        if (acceptedItems.isEmpty()) {
            callback.onResult(
                    ImportWarehouseItemsResult.completed(
                            csvResult.getTotalRows(),
                            0,
                            issues
                    )
            );
            return;
        }

        persistBatch(
                acceptedItems,
                csvResult.getTotalRows(),
                issues,
                callback
        );
    }

    private void persistBatch(
            List<WarehouseItem> acceptedItems,
            int totalRows,
            List<ImportWarehouseItemIssue> issues,
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
                                ImportWarehouseItemsResult
                                        .completed(
                                                totalRows,
                                                insertedCount,
                                                issues
                                        )
                        );
                    }

                    @Override
                    public void onDuplicate(
                            Throwable throwable
                    ) {
                        callback.onResult(
                                ImportWarehouseItemsResult
                                        .persistenceError(
                                                totalRows,
                                                issues
                                        )
                        );
                    }

                    @Override
                    public void onError(
                            Throwable throwable
                    ) {
                        callback.onResult(
                                ImportWarehouseItemsResult
                                        .persistenceError(
                                                totalRows,
                                                issues
                                        )
                        );
                    }
                }
        );
    }

    private List<ImportWarehouseItemIssue>
    validateRequiredFields(
            int rowNumber,
            NormalizedRow normalized
    ) {
        List<ImportWarehouseItemIssue> issues =
                new ArrayList<>();

        if (normalized.category.isEmpty()) {
            issues.add(
                    ImportWarehouseItemIssue
                            .missingCategory(
                                    rowNumber,
                                    normalized.code
                            )
            );
        }

        if (normalized.code.isEmpty()) {
            issues.add(
                    ImportWarehouseItemIssue
                            .missingCode(
                                    rowNumber,
                                    normalized.category
                            )
            );
        }

        if (normalized.site.isEmpty()) {
            issues.add(
                    ImportWarehouseItemIssue
                            .missingSite(
                                    rowNumber,
                                    normalized.category,
                                    normalized.code
                            )
            );
        }

        return issues;
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

    private void sortIssues(
            List<ImportWarehouseItemIssue> issues
    ) {
        issues.sort(
                (first, second) -> {
                    int rowComparison = Integer.compare(
                            first.getRowNumber(),
                            second.getRowNumber()
                    );

                    if (rowComparison != 0) {
                        return rowComparison;
                    }

                    return first.getType().compareTo(
                            second.getType()
                    );
                }
        );
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
    }
}