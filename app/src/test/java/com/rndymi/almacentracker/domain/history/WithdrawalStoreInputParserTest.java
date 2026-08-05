package com.rndymi.almacentracker.domain.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;

public final class WithdrawalStoreInputParserTest {

    @Test
    public void parseNormalNumbersReturnsCanonicalCircledStores() {
        WithdrawalStoreInputParser.Result result =
                WithdrawalStoreInputParser.parse("1, 2, 13");

        assertTrue(result.isValid());
        assertEquals(
                Arrays.asList("①", "②", "⑬"),
                result.getDestinations()
        );
    }

    @Test
    public void parseAcceptsExistingStoreLabelsAndCircledNumbers() {
        WithdrawalStoreInputParser.Result result =
                WithdrawalStoreInputParser.parse("Tienda2 ⑬ 2");

        assertTrue(result.isValid());
        assertEquals(
                Arrays.asList("②", "⑬"),
                result.getDestinations()
        );
    }

    @Test
    public void formatUsesKeyboardFriendlyNumbers() {
        assertEquals(
                "1, 2, 50",
                WithdrawalStoreInputParser.format(
                        Arrays.asList("①", "Tienda 2", "㊿")
                )
        );
    }

    @Test
    public void parseRejectsTextAndNumbersOutsideSupportedRange() {
        assertFalse(
                WithdrawalStoreInputParser.parse("1, norte").isValid()
        );
        assertFalse(
                WithdrawalStoreInputParser.parse("51").isValid()
        );
    }
}
