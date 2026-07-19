package com.rndymi.almacentracker.application.result;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public final class ImportWarehouseItemsResultTest {

    @Test
    public void multipleIssuesFromSameRowCountAsOneInvalidRow() {
        ImportWarehouseItemsResult result =
                ImportWarehouseItemsResult.completed(
                        1,
                        0,
                        Arrays.asList(
                                ImportWarehouseItemIssue
                                        .missingCategory(
                                                2,
                                                ""
                                        ),
                                ImportWarehouseItemIssue
                                        .missingCode(
                                                2,
                                                ""
                                        ),
                                ImportWarehouseItemIssue
                                        .missingSite(
                                                2,
                                                "",
                                                ""
                                        )
                        )
                );

        assertEquals(3, result.getIssueCount());
        assertEquals(1, result.getInvalidCount());
        assertEquals(0, result.getDuplicateCount());
    }

    @Test
    public void issuesAreExposedAsImmutableCollection() {
        ImportWarehouseItemsResult result =
                ImportWarehouseItemsResult.completed(
                        1,
                        0,
                        Collections.singletonList(
                                ImportWarehouseItemIssue
                                        .missingCode(
                                                2,
                                                "MR"
                                        )
                        )
                );

        try {
            result.getIssues().clear();
        } catch (UnsupportedOperationException expected) {
            return;
        }

        throw new AssertionError(
                "Issues collection must be immutable"
        );
    }

    @Test
    public void fullSuccessContainsNoIssues() {
        ImportWarehouseItemsResult result =
                ImportWarehouseItemsResult.completed(
                        2,
                        2,
                        Collections.emptyList()
                );

        assertEquals(
                ImportWarehouseItemsResult.Status.SUCCESS,
                result.getStatus()
        );

        assertEquals(0, result.getIssueCount());
        assertEquals(0, result.getInvalidCount());
        assertEquals(0, result.getDuplicateCount());
    }

    @Test
    public void issuesAreOrderedByRowNumber() {
        ImportWarehouseItemsResult result =
                ImportWarehouseItemsResult.completed(
                        2,
                        0,
                        Arrays.asList(
                                ImportWarehouseItemIssue
                                        .missingSite(
                                                4,
                                                "MD",
                                                "2000"
                                        ),
                                ImportWarehouseItemIssue
                                        .missingCode(
                                                2,
                                                "MR"
                                        )
                        )
                );

        assertEquals(
                2,
                result.getIssues()
                        .get(0)
                        .getRowNumber()
        );

        assertEquals(
                4,
                result.getIssues()
                        .get(1)
                        .getRowNumber()
        );
    }
}