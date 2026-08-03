package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public final class DocumentTitleParserTest {

    private final DocumentTitleParser parser =
            new DocumentTitleParser(
                    new WarehouseReferenceParser()
            );

    @Test
    public void parseExtractsPersonNameBeforeReferences() {
        assertEquals(
                "Elena",
                parser.parse(
                        Arrays.asList(
                                "Elena",
                                "MR21570-5pcs",
                                "MR21571-1pcs"
                        )
                )
        );
    }

    @Test
    public void parseExtractsStoreReplenishmentTitle() {
        assertEquals(
                "REPONER",
                parser.parse(
                        Arrays.asList(
                                "REPONER",
                                "MA710-4pcs"
                        )
                )
        );
    }

    @Test
    public void parsePreservesChineseTitle() {
        assertEquals(
                "王小明",
                parser.parse(
                        Arrays.asList(
                                "王小明",
                                "MR21570-5pcs"
                        )
                )
        );
    }

    @Test
    public void parsePreservesChineseReplenishmentText() {
        assertEquals(
                "补货",
                parser.parse(
                        Arrays.asList(
                                "补货",
                                "MA710-4pcs"
                        )
                )
        );
    }

    @Test
    public void parseReturnsNullWhenDocumentStartsWithReference() {
        assertNull(
                parser.parse(
                        Arrays.asList(
                                "MR21570-5pcs",
                                "MR21571-1pcs"
                        )
                )
        );
    }

    @Test
    public void parseSkipsLeadingEmptyLines() {
        assertEquals(
                "UTOPYA",
                parser.parse(
                        Arrays.asList(
                                "",
                                "   ",
                                "UTOPYA",
                                "MA710-4pcs"
                        )
                )
        );
    }

    @Test
    public void parseReturnsNullForEmptyInput() {
        assertNull(
                parser.parse(
                        Collections.emptyList()
                )
        );
    }
}
