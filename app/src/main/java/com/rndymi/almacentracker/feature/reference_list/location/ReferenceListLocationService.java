package com.rndymi.almacentracker.feature.reference_list.location;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ReferenceListLocationService {

    public interface Callback {

        void onResult(
                ReferenceListLocationResult result
        );
    }

    private final WarehouseItemRepository repository;

    public ReferenceListLocationService(
            WarehouseItemRepository repository
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "repository"
        );
    }

    public void locate(
            List<WarehouseReference> references,
            Callback callback
    ) {
        Objects.requireNonNull(callback, "callback");

        List<WarehouseReference> validReferences =
                sanitizeReferences(references);

        if (validReferences.isEmpty()) {
            callback.onResult(
                    ReferenceListLocationResult
                            .invalidInput()
            );

            return;
        }

        repository.findAllByReferences(
                validReferences,
                new RepositoryCallback<
                        List<WarehouseReferenceLocation>
                        >() {

                    @Override
                    public void onSuccess(
                            List<WarehouseReferenceLocation>
                                    locations
                    ) {
                        callback.onResult(
                                ReferenceListLocationResult
                                        .success(locations)
                        );
                    }

                    @Override
                    public void onError(Throwable cause) {
                        callback.onResult(
                                ReferenceListLocationResult
                                        .error(cause)
                        );
                    }
                }
        );
    }

    private List<WarehouseReference>
    sanitizeReferences(
            List<WarehouseReference> references
    ) {
        if (references == null
                || references.isEmpty()) {
            return Collections.emptyList();
        }

        List<WarehouseReference> validReferences =
                new ArrayList<>();

        Set<String> identities = new HashSet<>();

        for (
                WarehouseReference reference
                : references
        ) {
            if (reference == null) {
                continue;
            }

            String category =
                    reference.getCategory() == null
                            ? ""
                            : reference
                            .getCategory()
                            .trim();

            String code =
                    reference.getCode() == null
                            ? ""
                            : reference
                            .getCode()
                            .trim();

            if (category.isEmpty()
                    || code.isEmpty()) {
                continue;
            }

            WarehouseReference validReference =
                    new WarehouseReference(
                            category,
                            code
                    );

            if (identities.add(
                    validReference.identityKey()
            )) {
                validReferences.add(
                        validReference
                );
            }
        }

        return Collections.unmodifiableList(
                validReferences
        );
    }
}