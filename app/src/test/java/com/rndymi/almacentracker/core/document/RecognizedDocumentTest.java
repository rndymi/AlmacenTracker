package com.rndymi.almacentracker.core.document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public final class RecognizedDocumentTest {

    @Test
    public void preservesLineOrder() {
        List<RecognizedTextLine> lines =
                new ArrayList<>();

        lines.add(
                new RecognizedTextLine(
                        0,
                        "Primera línea"
                )
        );

        lines.add(
                new RecognizedTextLine(
                        1,
                        "Segunda línea"
                )
        );

        RecognizedDocument document =
                new RecognizedDocument(
                        DocumentImageSource.CAMERA,
                        lines,
                        1000L
                );

        assertEquals(
                "Primera línea",
                document.getLines()
                        .get(0)
                        .getRawText()
        );

        assertEquals(
                "Segunda línea",
                document.getLines()
                        .get(1)
                        .getRawText()
        );
    }

    @Test
    public void defensivelyCopiesLines() {
        List<RecognizedTextLine> lines =
                new ArrayList<>();

        lines.add(
                new RecognizedTextLine(
                        0,
                        "MR 1210 A"
                )
        );

        RecognizedDocument document =
                new RecognizedDocument(
                        DocumentImageSource.PHOTO_PICKER,
                        lines,
                        1000L
                );

        lines.clear();

        assertEquals(
                1,
                document.getLineCount()
        );
    }

    @Test
    public void returnedLinesCannotBeModified() {
        RecognizedDocument document =
                new RecognizedDocument(
                        DocumentImageSource.CAMERA,
                        List.of(
                                new RecognizedTextLine(
                                        0,
                                        "MR1210"
                                )
                        ),
                        1000L
                );

        try {
            document.getLines().clear();
            fail(
                    "Expected immutable line collection"
            );
        } catch (
                UnsupportedOperationException expected
        ) {
            assertFalse(
                    document.getLines().isEmpty()
            );
        }
    }

    @Test
    public void emptyDocumentHasNoLines() {
        RecognizedDocument document =
                new RecognizedDocument(
                        DocumentImageSource.CAMERA,
                        List.of(),
                        1000L
                );

        assertFalse(document.hasLines());
        assertEquals(0, document.getLineCount());
    }
}