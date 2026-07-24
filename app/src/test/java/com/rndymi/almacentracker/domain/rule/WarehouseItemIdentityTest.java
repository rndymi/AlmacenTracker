package com.rndymi.almacentracker.domain.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public final class WarehouseItemIdentityTest {

    @Test
    public void sameCategoryAndCodeHaveSameIdentity() {
        WarehouseItemIdentity first =
                new WarehouseItemIdentity("MR", "1050");
        WarehouseItemIdentity second =
                new WarehouseItemIdentity("MR", "1050");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void identityNormalizesSpacesCaseAndUnicode() {
        WarehouseItemIdentity first =
                new WarehouseItemIdentity(
                        "  almacén ñ ",
                        " á-1 "
                );
        WarehouseItemIdentity second =
                new WarehouseItemIdentity(
                        "ALMACÉN Ñ",
                        "Á-1"
                );

        assertEquals(first, second);
    }

    @Test
    public void differentCategoryHasDifferentIdentity() {
        assertNotEquals(
                new WarehouseItemIdentity("MR", "1050"),
                new WarehouseItemIdentity("MD", "1050")
        );
    }

    @Test
    public void differentCodeHasDifferentIdentity() {
        assertNotEquals(
                new WarehouseItemIdentity("MR", "1050"),
                new WarehouseItemIdentity("MR", "2050")
        );
    }
}
