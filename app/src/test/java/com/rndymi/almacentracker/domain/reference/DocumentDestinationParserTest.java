package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;

public final class DocumentDestinationParserTest {

    private final DocumentDestinationParser parser =
            new DocumentDestinationParser();

    @Test
    public void parseReturnsDestinationsInOrderWithoutDuplicates() {
        assertEquals(
                Arrays.asList("①", "③", "⑩"),
                parser.parse(
                        "MR 21570 - 5 P - ①③①⑩"
                )
        );
    }

    @Test
    public void parseReturnsEmptyListWhenDestinationsAreMissing() {
        assertTrue(parser.parse(null).isEmpty());
        assertTrue(parser.parse("   ").isEmpty());
        assertTrue(
                parser.parse("MR 21570 - 5 P")
                        .isEmpty()
        );
    }

    @Test
    public void parseSupportsCircledDestinationsBeyondTen() {
        assertEquals(
                Arrays.asList("⑪", "⑬", "㉑"),
                parser.parse("1P - ⑪⑬㉑")
        );
    }
}
