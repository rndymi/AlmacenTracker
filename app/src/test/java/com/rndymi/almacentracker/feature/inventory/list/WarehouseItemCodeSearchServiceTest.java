package com.rndymi.almacentracker.feature.inventory.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.domain.rule.WarehouseItemNormalizer;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class WarehouseItemCodeSearchServiceTest {

    @Test
    public void returnsInvalidCodeWhenValueIsBlank() {
        TestRepository repository =
                new TestRepository();

        WarehouseItemCodeSearchService service =
                createService(repository);

        AtomicReference<WarehouseItemCodeSearchResult>
                resultReference = new AtomicReference<>();

        service.search(
                "   ",
                resultReference::set
        );

        assertEquals(
                WarehouseItemCodeSearchResult.Status
                        .INVALID_CODE,
                resultReference.get().getStatus()
        );

        assertEquals(0, repository.searchCount);
    }

    @Test
    public void normalizesCodeBeforeSearching() {
        TestRepository repository =
                new TestRepository();

        repository.response =
                Collections.singletonList(
                        createItem(
                                1L,
                                "MR",
                                "AB-10"
                        )
                );

        WarehouseItemCodeSearchService service =
                createService(repository);

        service.search(
                "  ab-10  ",
                ignored -> {
                }
        );

        assertEquals("AB-10", repository.requestedCode);
    }

    @Test
    public void returnsNotFoundWhenRepositoryIsEmpty() {
        TestRepository repository =
                new TestRepository();

        repository.response = Collections.emptyList();

        AtomicReference<WarehouseItemCodeSearchResult>
                resultReference = new AtomicReference<>();

        createService(repository).search(
                "1050",
                resultReference::set
        );

        assertEquals(
                WarehouseItemCodeSearchResult.Status.NOT_FOUND,
                resultReference.get().getStatus()
        );
    }

    @Test
    public void returnsSingleMatch() {
        TestRepository repository =
                new TestRepository();

        WarehouseItem item =
                createItem(
                        7L,
                        "MR",
                        "1050"
                );

        repository.response =
                Collections.singletonList(item);

        AtomicReference<WarehouseItemCodeSearchResult>
                resultReference = new AtomicReference<>();

        createService(repository).search(
                "1050",
                resultReference::set
        );

        WarehouseItemCodeSearchResult result =
                resultReference.get();

        assertEquals(
                WarehouseItemCodeSearchResult.Status
                        .SINGLE_MATCH,
                result.getStatus()
        );

        assertSame(item, result.getSingleMatch());
    }

    @Test
    public void returnsMultipleMatches() {
        TestRepository repository =
                new TestRepository();

        repository.response =
                Arrays.asList(
                        createItem(
                                1L,
                                "MD",
                                "1050"
                        ),
                        createItem(
                                2L,
                                "MR",
                                "1050"
                        )
                );

        AtomicReference<WarehouseItemCodeSearchResult>
                resultReference = new AtomicReference<>();

        createService(repository).search(
                "1050",
                resultReference::set
        );

        WarehouseItemCodeSearchResult result =
                resultReference.get();

        assertEquals(
                WarehouseItemCodeSearchResult.Status
                        .MULTIPLE_MATCHES,
                result.getStatus()
        );

        assertEquals(2, result.getMatches().size());
    }

    @Test
    public void returnsErrorWhenRepositoryFails() {
        TestRepository repository =
                new TestRepository();

        IllegalStateException expectedCause =
                new IllegalStateException(
                        "Database error"
                );

        repository.error = expectedCause;

        AtomicReference<WarehouseItemCodeSearchResult>
                resultReference = new AtomicReference<>();

        createService(repository).search(
                "1050",
                resultReference::set
        );

        WarehouseItemCodeSearchResult result =
                resultReference.get();

        assertEquals(
                WarehouseItemCodeSearchResult.Status.ERROR,
                result.getStatus()
        );

        assertSame(expectedCause, result.getCause());
    }

    private WarehouseItemCodeSearchService createService(
            TestRepository repository
    ) {
        return new WarehouseItemCodeSearchService(
                repository,
                new WarehouseItemNormalizer()
        );
    }

    private WarehouseItem createItem(
            long id,
            String category,
            String code
    ) {
        return new WarehouseItem(
                id,
                category,
                code,
                "A1",
                null,
                null,
                1L,
                1L
        );
    }

    private static final class TestRepository
            extends WarehouseItemRepositoryStub {

        private List<WarehouseItem> response =
                Collections.emptyList();

        private Throwable error;
        private String requestedCode;
        private int searchCount;

        @Override
        public void findAllByCode(
                String code,
                RepositoryCallback<List<WarehouseItem>>
                        callback
        ) {
            searchCount++;
            requestedCode = code;

            if (error != null) {
                callback.onError(error);
                return;
            }

            callback.onSuccess(response);
        }
    }
}