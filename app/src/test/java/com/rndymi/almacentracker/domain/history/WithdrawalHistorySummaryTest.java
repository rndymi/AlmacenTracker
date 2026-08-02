package com.rndymi.almacentracker.domain.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class WithdrawalHistorySummaryTest {

    @Test
    public void validSummary_preservesValues() {
        WithdrawalHistorySummary summary =
                new WithdrawalHistorySummary(
                        5L,
                        "Lista centro",
                        100L,
                        110L,
                        120L,
                        3,
                        2,
                        1
                );

        assertEquals(5L, summary.getId());
        assertEquals(
                "Lista centro",
                summary.getTitle()
        );
        assertEquals(3, summary.getEntryCount());
        assertEquals(2, summary.getFoundCount());
        assertEquals(1, summary.getNotFoundCount());
        assertTrue(summary.hasTitle());
    }

    @Test
    public void blankTitle_isNormalizedToNull() {
        WithdrawalHistorySummary summary =
                new WithdrawalHistorySummary(
                        1L,
                        "   ",
                        100L,
                        100L,
                        100L,
                        0,
                        0,
                        0
                );

        assertNull(summary.getTitle());
        assertFalse(summary.hasTitle());
    }

    @Test
    public void inconsistentCounters_areRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WithdrawalHistorySummary(
                        1L,
                        null,
                        100L,
                        100L,
                        100L,
                        3,
                        1,
                        1
                )
        );
    }

    @Test
    public void negativeCounter_isRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WithdrawalHistorySummary(
                        1L,
                        null,
                        100L,
                        100L,
                        100L,
                        -1,
                        0,
                        0
                )
        );
    }
}
