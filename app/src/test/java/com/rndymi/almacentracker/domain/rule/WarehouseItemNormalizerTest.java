package com.rndymi.almacentracker.domain.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public final class WarehouseItemNormalizerTest {

    private WarehouseItemNormalizer normalizer;

    @Before
    public void setUp() {
        normalizer = new WarehouseItemNormalizer();
    }

    @Test
    public void requiredNullBecomesEmpty() {
        assertEquals("", normalizer.normalizeCategory(null));
        assertEquals("", normalizer.normalizeCode(null));
        assertEquals("", normalizer.normalizeSite(null));
    }

    @Test
    public void requiredWhitespaceBecomesEmpty() {
        assertEquals(
                "",
                normalizer.normalizeCategory(" \t ")
        );
    }

    @Test
    public void requiredValuesAreTrimmedAndUppercased() {
        assertEquals(
                "MR",
                normalizer.normalizeCategory("  mR  ")
        );
        assertEquals(
                "A-1050",
                normalizer.normalizeCode(" a-1050 ")
        );
        assertEquals(
                "ALMACÉN Ñ",
                normalizer.normalizeSite(" almacén ñ ")
        );
    }

    @Test
    public void codeSuffixIsSeparatedFromNumericPart() {
        assertEquals(
                "1010 A",
                normalizer.normalizeCode("1010a")
        );
        assertEquals(
                "21571 ABC",
                normalizer.normalizeCode("21571ABC")
        );
    }

    @Test
    public void codeSuffixWordsUseCanonicalSpacing() {
        assertEquals(
                "1010 DARK BLUE",
                normalizer.normalizeCode(
                        " 1010   dark   blue "
                )
        );
    }

    @Test
    public void numericCodeDoesNotReceiveTrailingSpace() {
        assertEquals(
                "1010",
                normalizer.normalizeCode(" 1010 ")
        );
    }

    @Test
    public void optionalNullEmptyAndWhitespaceBecomeNull() {
        assertNull(normalizer.normalizeOptional(null));
        assertNull(normalizer.normalizeOptional(""));
        assertNull(normalizer.normalizeOptional(" \t "));
    }

    @Test
    public void optionalValueIsTrimmedWithoutChangingCase() {
        assertEquals(
                "Nivel Bajo",
                normalizer.normalizeOptional(
                        "  Nivel Bajo  "
                )
        );
    }
}
