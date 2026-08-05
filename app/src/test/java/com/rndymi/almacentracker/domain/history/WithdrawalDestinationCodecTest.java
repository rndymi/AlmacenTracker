package com.rndymi.almacentracker.domain.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public final class WithdrawalDestinationCodecTest {

    @Test
    public void roundTripPreservesUnicodeAndSeparators() {
        assertEquals(
                Arrays.asList("①", "Tienda: 2", "Zürich"),
                WithdrawalDestinationCodec.decode(
                        WithdrawalDestinationCodec.encode(
                                Arrays.asList(
                                        "①",
                                        "Tienda: 2",
                                        "Zürich"
                                )
                        )
                )
        );
    }

    @Test
    public void emptyDestinationsUseNullStorage() {
        assertNull(
                WithdrawalDestinationCodec.encode(
                        Collections.emptyList()
                )
        );
        assertTrue(
                WithdrawalDestinationCodec.decode(null)
                        .isEmpty()
        );
    }
}
