package com.rndymi.almacentracker.feature.inventory.list;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.domain.rule.WarehouseItemNormalizer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WarehouseItemCodeSearchService {

    public interface Callback {

        void onResult(
                WarehouseItemCodeSearchResult result
        );
    }

    private final WarehouseItemRepository repository;
    private final WarehouseItemNormalizer normalizer;

    public WarehouseItemCodeSearchService(
            WarehouseItemRepository repository,
            WarehouseItemNormalizer normalizer
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.normalizer = Objects.requireNonNull(normalizer);
    }

    public void search(
            String scannedCode,
            Callback callback
    ) {
        Objects.requireNonNull(callback);

        String normalizedCode =
                normalizer.normalizeCode(scannedCode);

        if (normalizedCode.isEmpty()) {
            callback.onResult(
                    WarehouseItemCodeSearchResult.invalidCode()
            );
            return;
        }

        repository.findAllByCode(
                normalizedCode,
                new RepositoryCallback<List<WarehouseItem>>() {
                    @Override
                    public void onSuccess(
                            List<WarehouseItem> matches
                    ) {
                        classifyMatches(
                                normalizedCode,
                                matches,
                                callback
                        );
                    }

                    @Override
                    public void onError(Throwable cause) {
                        callback.onResult(
                                WarehouseItemCodeSearchResult.error(
                                        normalizedCode,
                                        cause
                                )
                        );
                    }
                }
        );
    }

    private void classifyMatches(
            String normalizedCode,
            List<WarehouseItem> matches,
            Callback callback
    ) {
        List<WarehouseItem> safeMatches =
                matches == null
                        ? Collections.emptyList()
                        : matches;

        if (safeMatches.isEmpty()) {
            callback.onResult(
                    WarehouseItemCodeSearchResult.notFound(
                            normalizedCode
                    )
            );
            return;
        }

        if (safeMatches.size() == 1) {
            callback.onResult(
                    WarehouseItemCodeSearchResult.singleMatch(
                            normalizedCode,
                            safeMatches.get(0)
                    )
            );
            return;
        }

        callback.onResult(
                WarehouseItemCodeSearchResult.multipleMatches(
                        normalizedCode,
                        safeMatches
                )
        );
    }
}