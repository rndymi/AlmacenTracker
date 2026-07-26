package com.rndymi.almacentracker.feature.reference_list.location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceLocation;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class ReferenceListLocationServiceTest {

    @Test
    public void emptyInputReturnsInvalidInput() {
        ReferenceListLocationService service =
                new ReferenceListLocationService(
                        new WarehouseItemRepositoryStub()
                );

        AtomicReference<
                ReferenceListLocationResult
                > resultReference =
                new AtomicReference<>();

        service.locate(
                Collections.emptyList(),
                resultReference::set
        );

        assertNotNull(resultReference.get());

        assertEquals(
                ReferenceListLocationResult
                        .Status.INVALID_INPUT,
                resultReference.get().getStatus()
        );
    }

    @Test
    public void resultPreservesOrderAndCountsStatuses() {
        WarehouseReference first =
                new WarehouseReference(
                        "MR",
                        "1210A"
                );

        WarehouseReference second =
                new WarehouseReference(
                        "MZ",
                        "1300C"
                );

        WarehouseItemRepository repository =
                new WarehouseItemRepositoryStub() {

                    @Override
                    public void findAllByReferences(
                            List<WarehouseReference>
                                    references,
                            RepositoryCallback<
                                    List<WarehouseReferenceLocation>
                                    > callback
                    ) {
                        callback.onSuccess(
                                Arrays.asList(
                                        WarehouseReferenceLocation
                                                .found(
                                                        first,
                                                        7L,
                                                        "A1",
                                                        "2"
                                                ),
                                        WarehouseReferenceLocation
                                                .notFound(
                                                        second
                                                )
                                )
                        );
                    }
                };

        ReferenceListLocationService service =
                new ReferenceListLocationService(
                        repository
                );

        AtomicReference<
                ReferenceListLocationResult
                > resultReference =
                new AtomicReference<>();

        service.locate(
                Arrays.asList(
                        first,
                        second
                ),
                resultReference::set
        );

        ReferenceListLocationResult result =
                resultReference.get();

        assertNotNull(result);

        assertEquals(
                ReferenceListLocationResult
                        .Status.SUCCESS,
                result.getStatus()
        );

        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getFoundCount());
        assertEquals(1, result.getNotFoundCount());

        assertEquals(
                first,
                result.getLocations()
                        .get(0)
                        .getReference()
        );

        assertEquals(
                second,
                result.getLocations()
                        .get(1)
                        .getReference()
        );
    }

    @Test
    public void duplicateInputKeepsFirstOccurrence() {
        WarehouseReference reference =
                new WarehouseReference(
                        "MR",
                        "1210A"
                );

        AtomicReference<Integer> receivedCount =
                new AtomicReference<>(0);

        WarehouseItemRepository repository =
                new WarehouseItemRepositoryStub() {

                    @Override
                    public void findAllByReferences(
                            List<WarehouseReference>
                                    references,
                            RepositoryCallback<
                                    List<WarehouseReferenceLocation>
                                    > callback
                    ) {
                        receivedCount.set(
                                references.size()
                        );

                        callback.onSuccess(
                                Collections.singletonList(
                                        WarehouseReferenceLocation
                                                .notFound(
                                                        references.get(0)
                                                )
                                )
                        );
                    }
                };

        ReferenceListLocationService service =
                new ReferenceListLocationService(
                        repository
                );

        service.locate(
                Arrays.asList(
                        reference,
                        reference
                ),
                ignored -> {
                }
        );

        assertEquals(
                Integer.valueOf(1),
                receivedCount.get()
        );
    }
}