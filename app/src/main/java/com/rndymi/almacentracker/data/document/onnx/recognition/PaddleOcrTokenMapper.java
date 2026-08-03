package com.rndymi.almacentracker.data.document.onnx.recognition;

import com.rndymi.almacentracker.data.document.onnx.PaddleOcrDictionary;

import java.util.Objects;

final class PaddleOcrTokenMapper {

    private final PaddleOcrDictionary dictionary;
    private final int blankIndex;
    private final int classCount;
    private final int additionalSpecialTokenCount;

    PaddleOcrTokenMapper(
            PaddleOcrDictionary dictionary,
            int blankIndex,
            int classCount,
            int additionalSpecialTokenCount
    ) {
        this.dictionary = Objects.requireNonNull(
                dictionary,
                "dictionary"
        );

        if (blankIndex < 0
                || blankIndex >= classCount) {
            throw new IllegalArgumentException(
                    "blankIndex is invalid"
            );
        }

        if (classCount
                != dictionary.size()
                + 1
                + additionalSpecialTokenCount) {
            throw new IllegalArgumentException(
                    "Dictionary and recognition classes "
                            + "are incompatible"
            );
        }

        this.blankIndex = blankIndex;
        this.classCount = classCount;
        this.additionalSpecialTokenCount =
                additionalSpecialTokenCount;
    }

    boolean isBlank(
            int classIndex
    ) {
        return classIndex == blankIndex;
    }

    boolean isSpecialToken(
            int classIndex
    ) {
        validateClassIndex(classIndex);

        if (isBlank(classIndex)) {
            return false;
        }

        return dictionaryIndex(classIndex)
                >= dictionary.size();
    }

    String map(
            int classIndex
    ) throws TextRecognitionException {
        validateClassIndex(classIndex);

        if (isBlank(classIndex)) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error
                            .INVALID_CLASS_INDEX,
                    "Blank token cannot be mapped to text"
            );
        }

        int dictionaryIndex =
                dictionaryIndex(classIndex);

        if (dictionaryIndex >= dictionary.size()) {
            return "";
        }

        return dictionary.get(dictionaryIndex);
    }

    private int dictionaryIndex(
            int classIndex
    ) {
        return classIndex > blankIndex
                ? classIndex - 1
                : classIndex;
    }

    private void validateClassIndex(
            int classIndex
    ) {
        if (classIndex < 0
                || classIndex >= classCount) {
            throw new IllegalArgumentException(
                    "Class index is outside the model range"
            );
        }
    }
}
