package com.rndymi.almacentracker.data.document.onnx;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public final class PaddleOcrDictionaryTest {

    @Test
    public void fromUtf8_preservesOrder() throws IOException {
        PaddleOcrDictionary dictionary =
                PaddleOcrDictionary.fromUtf8(
                        "A\nB\n0\n".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        assertEquals(3, dictionary.size());
        assertEquals("A", dictionary.get(0));
        assertEquals("B", dictionary.get(1));
        assertEquals("0", dictionary.get(2));
    }

    @Test
    public void fromUtf8_supportsUnicode() throws IOException {
        PaddleOcrDictionary dictionary =
                PaddleOcrDictionary.fromUtf8(
                        "Á\nñ\n€\n".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        assertEquals("Á", dictionary.get(0));
        assertEquals("ñ", dictionary.get(1));
        assertEquals("€", dictionary.get(2));
    }

    @Test
    public void fromUtf8_rejectsEmptyDictionary() {
        assertThrows(
                IOException.class,
                () -> PaddleOcrDictionary.fromUtf8(
                        new byte[0]
                )
        );
    }

    @Test
    public void get_rejectsNegativeIndex()
            throws IOException {
        PaddleOcrDictionary dictionary =
                PaddleOcrDictionary.fromUtf8(
                        "A\n".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> dictionary.get(-1)
        );
    }

    @Test
    public void get_rejectsIndexOutsideDictionary()
            throws IOException {
        PaddleOcrDictionary dictionary =
                PaddleOcrDictionary.fromUtf8(
                        "A\n".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> dictionary.get(1)
        );
    }

    @Test
    public void asList_isImmutable() throws IOException {
        PaddleOcrDictionary dictionary =
                PaddleOcrDictionary.fromUtf8(
                        "A\nB\n".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> dictionary.asList().add("C")
        );
    }
}
