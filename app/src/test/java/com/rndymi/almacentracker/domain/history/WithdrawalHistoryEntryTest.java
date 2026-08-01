package com.rndymi.almacentracker.domain.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WithdrawalHistoryEntryTest {

    @Test
    public void foundEntryKeepsLocationSnapshot() {
        WithdrawalHistoryEntry entry =
                createFoundEntry(
                        "MR",
                        "001210A",
                        4,
                        "CAJAS",
                        "A1",
                        null
                );

        assertEquals("MR", entry.getCategory());
        assertEquals("001210A", entry.getCode());
        assertEquals(
                Integer.valueOf(4),
                entry.getQuantity()
        );
        assertEquals("CAJAS", entry.getUnit());
        assertEquals(
                Long.valueOf(7L),
                entry.getWarehouseItemIdSnapshot()
        );
        assertEquals("A1", entry.getSiteSnapshot());
        assertNull(entry.getPositionSnapshot());
        assertEquals(
                WithdrawalLocationStatus.FOUND,
                entry.getLocationStatus()
        );
        assertTrue(entry.hasQuantity());
        assertTrue(entry.hasUnit());
        assertFalse(entry.hasPositionSnapshot());
    }

    @Test
    public void notFoundEntryAllowsNullDocumentaryData() {
        WithdrawalHistoryEntry entry =
                new WithdrawalHistoryEntry(
                        0L,
                        0L,
                        0,
                        "MZ",
                        "0900",
                        null,
                        null,
                        null,
                        null,
                        null,
                        WithdrawalLocationStatus.NOT_FOUND
                );

        assertEquals("0900", entry.getCode());
        assertNull(entry.getQuantity());
        assertNull(entry.getUnit());
        assertNull(entry.getWarehouseItemIdSnapshot());
        assertNull(entry.getSiteSnapshot());
        assertNull(entry.getPositionSnapshot());
        assertFalse(entry.hasQuantity());
    }

    @Test
    public void zeroAndNegativeQuantitiesAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createFoundEntry(
                        "MR",
                        "1210",
                        0,
                        "CAJAS",
                        "A1",
                        null
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> createFoundEntry(
                        "MR",
                        "1210",
                        -2,
                        "CAJAS",
                        "A1",
                        null
                )
        );
    }

    @Test
    public void foundEntryRequiresWarehouseItemId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WithdrawalHistoryEntry(
                        0L,
                        0L,
                        0,
                        "MR",
                        "1210",
                        null,
                        null,
                        null,
                        "A1",
                        null,
                        WithdrawalLocationStatus.FOUND
                )
        );
    }

    @Test
    public void foundEntryRequiresSite() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WithdrawalHistoryEntry(
                        0L,
                        0L,
                        0,
                        "MR",
                        "1210",
                        null,
                        null,
                        7L,
                        null,
                        null,
                        WithdrawalLocationStatus.FOUND
                )
        );
    }

    @Test
    public void notFoundEntryRejectsLocationSnapshot() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WithdrawalHistoryEntry(
                        0L,
                        0L,
                        0,
                        "MR",
                        "1210",
                        null,
                        null,
                        7L,
                        "A1",
                        null,
                        WithdrawalLocationStatus.NOT_FOUND
                )
        );
    }

    @Test
    public void blankCategoryAndCodeAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WithdrawalHistoryEntry(
                        0L,
                        0L,
                        0,
                        " ",
                        "1210",
                        null,
                        null,
                        null,
                        null,
                        null,
                        WithdrawalLocationStatus.NOT_FOUND
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new WithdrawalHistoryEntry(
                        0L,
                        0L,
                        0,
                        "MR",
                        " ",
                        null,
                        null,
                        null,
                        null,
                        null,
                        WithdrawalLocationStatus.NOT_FOUND
                )
        );
    }

    @Test
    public void negativeOrderIndexIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WithdrawalHistoryEntry(
                        0L,
                        0L,
                        -1,
                        "MR",
                        "1210",
                        null,
                        null,
                        null,
                        null,
                        null,
                        WithdrawalLocationStatus.NOT_FOUND
                )
        );
    }

    private WithdrawalHistoryEntry createFoundEntry(
            String category,
            String code,
            Integer quantity,
            String unit,
            String site,
            String position
    ) {
        return new WithdrawalHistoryEntry(
                0L,
                0L,
                0,
                category,
                code,
                quantity,
                unit,
                7L,
                site,
                position,
                WithdrawalLocationStatus.FOUND
        );
    }
}