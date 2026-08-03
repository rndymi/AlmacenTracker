package com.rndymi.almacentracker.data.document.onnx;

import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

public final class PaddleOcrModelManifest {

    private static final String PLACEHOLDER_PREFIX =
            "REPLACE_WITH_";

    private final String detectorSha256;
    private final String detectorInputName;
    private final int detectorInputRank;
    private final int detectorOutputCount;

    private final String recognizerSha256;
    private final String recognizerInputName;
    private final int recognizerInputRank;
    private final int recognizerOutputCount;
    private final int recognizerClassCount;
    private final int blankTokenCount;
    private final int additionalSpecialTokenCount;

    private final String recognizerOutputName;
    private final int recognizerOutputRank;
    private final int recognizerFixedHeight;
    private final int recognizerBlankIndex;

    private final int dictionaryEntryCount;
    private final String dictionaryCharset;

    private final String dictionarySha256;

    private PaddleOcrModelManifest(
            String detectorSha256,
            String detectorInputName,
            int detectorInputRank,
            int detectorOutputCount,
            String recognizerSha256,
            String recognizerInputName,
            int recognizerInputRank,
            String recognizerOutputName,
            int recognizerOutputRank,
            int recognizerOutputCount,
            int recognizerFixedHeight,
            int recognizerClassCount,
            int blankTokenCount,
            int additionalSpecialTokenCount,
            int recognizerBlankIndex,
            String dictionarySha256,
            int dictionaryEntryCount,
            String dictionaryCharset
    ) {
        this.detectorSha256 = detectorSha256;
        this.detectorInputName = detectorInputName;
        this.detectorInputRank = detectorInputRank;
        this.detectorOutputCount = detectorOutputCount;

        this.recognizerSha256 = recognizerSha256;
        this.recognizerInputName = recognizerInputName;
        this.recognizerInputRank = recognizerInputRank;
        this.recognizerOutputName = recognizerOutputName;
        this.recognizerOutputRank = recognizerOutputRank;
        this.recognizerOutputCount = recognizerOutputCount;
        this.recognizerFixedHeight = recognizerFixedHeight;
        this.recognizerClassCount = recognizerClassCount;
        this.blankTokenCount = blankTokenCount;
        this.additionalSpecialTokenCount =
                additionalSpecialTokenCount;
        this.recognizerBlankIndex = recognizerBlankIndex;

        this.dictionarySha256 = dictionarySha256;
        this.dictionaryEntryCount = dictionaryEntryCount;
        this.dictionaryCharset = dictionaryCharset;
    }

    public static PaddleOcrModelManifest from(
            Properties properties
    ) {
        Objects.requireNonNull(
                properties,
                "properties"
        );

        String detectorSha256 =
                requireHash(
                        properties,
                        "detector.sha256"
                );
        String detectorInputName =
                requireText(
                        properties,
                        "detector.input.name"
                );
        int detectorInputRank =
                requirePositiveInt(
                        properties,
                        "detector.input.rank"
                );
        int detectorOutputCount =
                requirePositiveInt(
                        properties,
                        "detector.output.count"
                );

        String recognizerSha256 =
                requireHash(
                        properties,
                        "recognizer.sha256"
                );
        String recognizerInputName =
                requireText(
                        properties,
                        "recognizer.input.name"
                );
        int recognizerInputRank =
                requirePositiveInt(
                        properties,
                        "recognizer.input.rank"
                );
        String recognizerOutputName =
                requireText(
                        properties,
                        "recognizer.output.name"
                );

        int recognizerOutputRank =
                requirePositiveInt(
                        properties,
                        "recognizer.output.rank"
                );

        int recognizerFixedHeight =
                requirePositiveInt(
                        properties,
                        "recognizer.input.fixed.height"
                );
        int recognizerOutputCount =
                requirePositiveInt(
                        properties,
                        "recognizer.output.count"
                );
        int recognizerClassCount =
                requirePositiveInt(
                        properties,
                        "recognizer.class.count"
                );
        int blankTokenCount =
                requireNonNegativeInt(
                        properties,
                        "recognizer.blank.token.count"
                );
        int additionalSpecialTokenCount =
                requireNonNegativeInt(
                        properties,
                        "recognizer.additional.special.token.count"
                );
        int recognizerBlankIndex =
                requireNonNegativeInt(
                        properties,
                        "recognizer.ctc.blank.index"
                );

        String dictionarySha256 =
                requireHash(
                        properties,
                        "dictionary.sha256"
                );

        int dictionaryEntryCount =
                requirePositiveInt(
                        properties,
                        "dictionary.entry.count"
                );

        String dictionaryCharset =
                requireText(
                        properties,
                        "dictionary.charset"
                );

        if (recognizerBlankIndex >= recognizerClassCount) {
            throw new PaddleOcrManifestException(
                    "recognizer.ctc.blank.index must be smaller "
                            + "than recognizer.class.count"
            );
        }

        int calculatedDictionarySize =
                recognizerClassCount
                        - blankTokenCount
                        - additionalSpecialTokenCount;

        if (calculatedDictionarySize != dictionaryEntryCount) {
            throw new PaddleOcrManifestException(
                    "dictionary.entry.count does not match "
                            + "the recognizer class relationship"
            );
        }

        if (!"UTF-8".equalsIgnoreCase(dictionaryCharset)) {
            throw new PaddleOcrManifestException(
                    "Only UTF-8 dictionaries are supported"
            );
        }

        return new PaddleOcrModelManifest(
                detectorSha256,
                detectorInputName,
                detectorInputRank,
                detectorOutputCount,
                recognizerSha256,
                recognizerInputName,
                recognizerInputRank,
                recognizerOutputName,
                recognizerOutputRank,
                recognizerOutputCount,
                recognizerFixedHeight,
                recognizerClassCount,
                blankTokenCount,
                additionalSpecialTokenCount,
                recognizerBlankIndex,
                dictionarySha256,
                dictionaryEntryCount,
                dictionaryCharset
        );
    }

    public int expectedDictionarySize() {
        int calculatedSize =
                recognizerClassCount
                        - blankTokenCount
                        - additionalSpecialTokenCount;

        if (calculatedSize != dictionaryEntryCount) {
            throw new PaddleOcrManifestException(
                    "The declared dictionary size is inconsistent"
            );
        }

        return calculatedSize;
    }

    public String getDetectorSha256() {
        return detectorSha256;
    }

    public String getDetectorInputName() {
        return detectorInputName;
    }

    public int getDetectorInputRank() {
        return detectorInputRank;
    }

    public int getDetectorOutputCount() {
        return detectorOutputCount;
    }

    public String getRecognizerSha256() {
        return recognizerSha256;
    }

    public String getRecognizerInputName() {
        return recognizerInputName;
    }

    public int getRecognizerInputRank() {
        return recognizerInputRank;
    }

    public int getRecognizerOutputCount() {
        return recognizerOutputCount;
    }

    public int getRecognizerClassCount() {
        return recognizerClassCount;
    }

    public int getBlankTokenCount() {
        return blankTokenCount;
    }

    public int getAdditionalSpecialTokenCount() {
        return additionalSpecialTokenCount;
    }

    public String getDictionarySha256() {
        return dictionarySha256;
    }

    public String getRecognizerOutputName() {
        return recognizerOutputName;
    }

    public int getRecognizerOutputRank() {
        return recognizerOutputRank;
    }

    public int getRecognizerFixedHeight() {
        return recognizerFixedHeight;
    }

    public int getRecognizerBlankIndex() {
        return recognizerBlankIndex;
    }

    public int getDictionaryEntryCount() {
        return dictionaryEntryCount;
    }

    public String getDictionaryCharset() {
        return dictionaryCharset;
    }

    private static String requireHash(
            Properties properties,
            String key
    ) {
        String value = requireText(properties, key)
                .toLowerCase(Locale.ROOT);

        if (!value.matches("[0-9a-f]{64}")) {
            throw new PaddleOcrManifestException(
                    key + " must contain a SHA-256 hash"
            );
        }

        return value;
    }

    private static String requireText(
            Properties properties,
            String key
    ) {
        String configuredValue =
                properties.getProperty(key);

        if (configuredValue == null) {
            throw new PaddleOcrManifestException(
                    key + " is missing"
            );
        }

        String value = configuredValue.trim();

        if (value.isEmpty()
                || value.startsWith(
                PLACEHOLDER_PREFIX
        )) {
            throw new PaddleOcrManifestException(
                    key + " has not been configured"
            );
        }

        return value;
    }

    private static int requirePositiveInt(
            Properties properties,
            String key
    ) {
        int value = parseInt(properties, key);

        if (value <= 0) {
            throw new PaddleOcrManifestException(
                    key + " must be greater than zero"
            );
        }

        return value;
    }

    private static int requireNonNegativeInt(
            Properties properties,
            String key
    ) {
        int value = parseInt(properties, key);

        if (value < 0) {
            throw new PaddleOcrManifestException(
                    key + " cannot be negative"
            );
        }

        return value;
    }

    private static int parseInt(
            Properties properties,
            String key
    ) {
        String value = requireText(properties, key);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new PaddleOcrManifestException(
                    key + " must contain an integer",
                    exception
            );
        }
    }
}
