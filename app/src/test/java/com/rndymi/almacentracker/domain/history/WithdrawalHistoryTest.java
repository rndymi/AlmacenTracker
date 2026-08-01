package com.rndymi.almacentracker.domain.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WithdrawalHistoryTest {

    @Test
    public void nullTitleIsAllowed() {
        WithdrawalHistory history =
                new WithdrawalHistory(
                        0L,
                        null,
                        100L,
                        100L,
                        100L
                );

        assertNull(history.getTitle());
        assertFalse(history.hasTitle());
    }

    @Test
    public void blankTitleIsNormalizedToNull() {
        WithdrawalHistory history =
                new WithdrawalHistory(
                        0L,
                        "   ",
                        100L,
                        100L,
                        100L
                );

        assertNull(history.getTitle());
    }

    @Test
    public void titleRemovesExternalSpaces() {
        WithdrawalHistory history =
                new WithdrawalHistory(
                        0L,
                        "  Reposición centro  ",
                        100L,
                        110L,
                        120L
                );

        assertEquals(
                "Reposición centro",
                history.getTitle()
        );
        assertTrue(history.hasTitle());
    }

    @Test
    public void zeroIdIsAllowedForUnpersistedHistory() {
        WithdrawalHistory history =
                new WithdrawalHistory(
                        0L,
                        null,
                        100L,
                        100L,
                        100L
                );

        assertEquals(0L, history.getId());
    }

    @Test
    public void negativeIdIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WithdrawalHistory(
                        -1L,
                        null,
                        100L,
                        100L,
                        100L
                )
        );
    }

    @Test
    public void nonPositiveTimestampsAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WithdrawalHistory(
                        0L,
                        null,
                        0L,
                        100L,
                        100L
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new WithdrawalHistory(
                        0L,
                        null,
                        100L,
                        0L,
                        100L
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new WithdrawalHistory(
                        0L,
                        null,
                        100L,
                        100L,
                        0L
                )
        );
    }
}