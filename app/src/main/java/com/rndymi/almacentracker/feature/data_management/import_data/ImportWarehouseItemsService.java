package com.rndymi.almacentracker.feature.data_management.import_data;

import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvReadCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvReader;
import com.rndymi.almacentracker.application.port.out.RepositoryCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.ImportWarehouseItemIssue;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvReadResult;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvRow;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.domain.rule.WarehouseItemIdentity;
import com.rndymi.almacentracker.domain.rule.WarehouseItemNormalizer;
import com.rndymi.almacentracker.domain.rule.WarehouseItemValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.LongSupplier;

public final class ImportWarehouseItemsService
        implements ImportWarehouseItemsUseCase {

    private static final WarehouseItemNormalizer NORMALIZER =
            new WarehouseItemNormalizer();
    private static final WarehouseItemValidator VALIDATOR =
            new WarehouseItemValidator();

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
                new RepositoryCallback<List<WarehouseItem>>() {
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
        Set<WarehouseItemIdentity> existingIdentities =
                buildExistingIdentities(existingItems);

        Map<WarehouseItemIdentity, Integer> acceptedFileRows =
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

            WarehouseItemIdentity identity =
                    new WarehouseItemIdentity(
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
                new RepositoryCallback<Integer>() {
                    @Override
                    public void onSuccess(
                            Integer insertedCount
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

        WarehouseItemValidator.ValidationResult validation =
                VALIDATOR.validateRequiredFields(
                        normalized.category,
                        normalized.code,
                        normalized.site
                );

        if (validation.isMissing(
                WarehouseItemValidator.RequiredField.CATEGORY
        )) {
            issues.add(
                    ImportWarehouseItemIssue
                            .missingCategory(
                                    rowNumber,
                                    normalized.code
                            )
            );
        }

        if (validation.isMissing(
                WarehouseItemValidator.RequiredField.CODE
        )) {
            issues.add(
                    ImportWarehouseItemIssue
                            .missingCode(
                                    rowNumber,
                                    normalized.category
                            )
            );
        }

        if (validation.isMissing(
                WarehouseItemValidator.RequiredField.SITE
        )) {
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

    private Set<WarehouseItemIdentity> buildExistingIdentities(
            List<WarehouseItem> existingItems
    ) {
        Set<WarehouseItemIdentity> identities =
                new HashSet<>();

        if (existingItems == null) {
            return identities;
        }

        for (WarehouseItem item : existingItems) {
            identities.add(
                    new WarehouseItemIdentity(
                            item.getCategory(),
                            item.getCode()
                    )
            );
        }

        return identities;
    }

    private NormalizedRow normalize(
            WarehouseItemCsvRow row
    ) {
        return new NormalizedRow(
                NORMALIZER.normalizeCategory(
                        row.getCategory()
                ),
                NORMALIZER.normalizeCode(row.getCode()),
                NORMALIZER.normalizeSite(row.getSite()),
                NORMALIZER.normalizeOptional(
                        row.getPosition()
                ),
                NORMALIZER.normalizeOptional(
                        row.getObservations()
                )
        );
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
