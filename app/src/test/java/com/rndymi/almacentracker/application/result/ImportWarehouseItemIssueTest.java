package com.rndymi.almacentracker.application.result;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ImportWarehouseItemIssueTest {

    @Test
    public void invalidColumnIssueContainsRowTypeAndCoherentSummary() {
        ImportWarehouseItemIssue issue =
                ImportWarehouseItemIssue.invalidColumnCount(
                        7,
                        3,
                        5
                );

        assertEquals(7, issue.getRowNumber());
        assertEquals(
                ImportIssueType.INVALID_COLUMN_COUNT,
                issue.getType()
        );
        assertTrue(issue.getMessage().contains("3 columnas"));
        assertTrue(issue.getMessage().contains("5"));
        assertTrue(issue.isInvalid());
        assertFalse(issue.isDuplicate());
    }

    @Test
    public void duplicateInFileContainsIdentityAndRelatedRow() {
        ImportWarehouseItemIssue issue =
                ImportWarehouseItemIssue.duplicateInFile(
                        9,
                        " MR ",
                        " 1050 ",
                        4
                );

        assertEquals(9, issue.getRowNumber());
        assertEquals(
                ImportIssueType.DUPLICATE_IN_FILE,
                issue.getType()
        );
        assertEquals("MR", issue.getCategory());
        assertEquals("1050", issue.getCode());
        assertEquals(
                Integer.valueOf(4),
                issue.getRelatedRowNumber()
        );
        assertTrue(issue.getMessage().contains("MR + 1050"));
        assertTrue(issue.getMessage().contains("fila 4"));
        assertTrue(issue.isDuplicate());
        assertFalse(issue.isInvalid());
    }

    @Test
    public void issueRejectsHeaderAndInvalidRelatedRows() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ImportWarehouseItemIssue.missingCode(
                        1,
                        "MR"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ImportWarehouseItemIssue.duplicateInFile(
                        3,
                        "MR",
                        "1050",
                        1
                )
        );
    }
}
