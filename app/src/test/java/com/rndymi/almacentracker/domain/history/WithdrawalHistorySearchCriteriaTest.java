package com.rndymi.almacentracker.domain.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class WithdrawalHistorySearchCriteriaTest {

    @Test
    public void emptyCriteriaHaveNoActiveFilters() {
        WithdrawalHistorySearchCriteria criteria =
                WithdrawalHistorySearchCriteria.empty();

        assertEquals("", criteria.getQuery());
        assertNull(criteria.getRegisteredFromInclusive());
        assertNull(criteria.getRegisteredToExclusive());
        assertFalse(criteria.hasQuery());
        assertFalse(criteria.hasActiveCriteria());
        assertEquals("", criteria.createLikePattern());
    }

    @Test
    public void queryIsTrimmedWithoutRemovingLeadingZeros() {
        WithdrawalHistorySearchCriteria criteria =
                new WithdrawalHistorySearchCriteria(
                        " 001210 ",
                        null,
                        null
                );

        assertEquals(
                "001210",
                criteria.getQuery()
        );

        assertEquals(
                "%001210%",
                criteria.createLikePattern()
        );
    }

    @Test
    public void nullQueryBecomesEmpty() {
        WithdrawalHistorySearchCriteria criteria =
                new WithdrawalHistorySearchCriteria(
                        null,
                        null,
                        null
                );

        assertEquals("", criteria.getQuery());
        assertFalse(criteria.hasQuery());
    }

    @Test
    public void likeWildcardsAreEscaped() {
        WithdrawalHistorySearchCriteria criteria =
                new WithdrawalHistorySearchCriteria(
                        "MR%_\\12",
                        null,
                        null
                );

        assertEquals(
                "%MR\\%\\_\\\\12%",
                criteria.createLikePattern()
        );
    }

    @Test
    public void dateCriteriaAreActive() {
        WithdrawalHistorySearchCriteria criteria =
                new WithdrawalHistorySearchCriteria(
                        "",
                        100L,
                        200L
                );

        assertTrue(criteria.hasActiveCriteria());
        assertEquals(
                Long.valueOf(100L),
                criteria.getRegisteredFromInclusive()
        );
        assertEquals(
                Long.valueOf(200L),
                criteria.getRegisteredToExclusive()
        );
    }

    @Test
    public void invalidIntervalIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WithdrawalHistorySearchCriteria(
                        "",
                        200L,
                        100L
                )
        );
    }

    @Test
    public void equalCriteriaHaveEqualHashCodes() {
        WithdrawalHistorySearchCriteria first =
                new WithdrawalHistorySearchCriteria(
                        "MR",
                        100L,
                        200L
                );

        WithdrawalHistorySearchCriteria second =
                new WithdrawalHistorySearchCriteria(
                        "MR",
                        100L,
                        200L
                );

        WithdrawalHistorySearchCriteria different =
                new WithdrawalHistorySearchCriteria(
                        "MI",
                        100L,
                        200L
                );

        assertEquals(first, second);
        assertEquals(
                first.hashCode(),
                second.hashCode()
        );
        assertNotEquals(first, different);
    }
}
