package com.rndymi.almacentracker.data.document.onnx;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public final class PaddleOcrModelManifestTest {

    private static final String HASH =
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                    + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    @Test
    public void from_buildsVerifiedManifest() {
        Properties properties =
                validProperties();

        PaddleOcrModelManifest manifest =
                PaddleOcrModelManifest.from(
                        properties
                );

        assertEquals(
                "detector_input",
                manifest.getDetectorInputName()
        );
        assertEquals(
                "recognizer_input",
                manifest.getRecognizerInputName()
        );
        assertEquals(
                100,
                manifest.getRecognizerClassCount()
        );
        assertEquals(
                99,
                manifest.expectedDictionarySize()
        );

        assertEquals(
                "fetch_name_0",
                manifest.getRecognizerOutputName()
        );
        assertEquals(
                3,
                manifest.getRecognizerOutputRank()
        );
        assertEquals(
                48,
                manifest.getRecognizerFixedHeight()
        );
        assertEquals(
                0,
                manifest.getRecognizerBlankIndex()
        );
        assertEquals(
                18383,
                manifest.getDictionaryEntryCount()
        );
        assertEquals(
                "UTF-8",
                manifest.getDictionaryCharset()
        );
    }

    @Test
    public void from_rejectsPlaceholder() {
        Properties properties =
                validProperties();

        properties.setProperty(
                "detector.input.name",
                "REPLACE_WITH_VERIFIED_INPUT_NAME"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> PaddleOcrModelManifest.from(
                        properties
                )
        );
    }

    @Test
    public void from_rejectsInvalidHash() {
        Properties properties =
                validProperties();

        properties.setProperty(
                "detector.sha256",
                "invalid"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> PaddleOcrModelManifest.from(
                        properties
                )
        );
    }

    private Properties validProperties() {
        Properties properties = new Properties();

        properties.setProperty(
                "detector.sha256",
                HASH
        );
        properties.setProperty(
                "detector.input.name",
                "detector_input"
        );
        properties.setProperty(
                "detector.input.rank",
                "4"
        );
        properties.setProperty(
                "detector.output.count",
                "1"
        );

        properties.setProperty(
                "recognizer.sha256",
                HASH
        );
        properties.setProperty(
                "recognizer.input.name",
                "recognizer_input"
        );
        properties.setProperty(
                "recognizer.input.rank",
                "4"
        );
        properties.setProperty(
                "recognizer.output.count",
                "1"
        );
        properties.setProperty(
                "recognizer.class.count",
                "100"
        );
        properties.setProperty(
                "recognizer.blank.token.count",
                "1"
        );
        properties.setProperty(
                "recognizer.additional.special.token.count",
                "0"
        );

        properties.setProperty(
                "dictionary.sha256",
                HASH
        );

        properties.setProperty(
                "recognizer.input.fixed.height",
                "48"
        );
        properties.setProperty(
                "recognizer.output.name",
                "fetch_name_0"
        );
        properties.setProperty(
                "recognizer.output.rank",
                "3"
        );
        properties.setProperty(
                "recognizer.ctc.blank.index",
                "0"
        );
        properties.setProperty(
                "dictionary.entry.count",
                "18383"
        );
        properties.setProperty(
                "dictionary.charset",
                "UTF-8"
        );

        return properties;
    }
}
