package com.rndymi.almacentracker.core.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class RecognizedTextLine {

    private final int index;
    private final String rawText;
    private final String reconstructedText;
    private final Integer left;
    private final Integer top;
    private final Integer right;
    private final Integer bottom;
    private final List<RecognizedTextElement> elements;

    public RecognizedTextLine(
            int index,
            String rawText
    ) {
        this(
                index,
                rawText,
                rawText,
                null,
                null,
                null,
                null,
                Collections.emptyList()
        );
    }

    public RecognizedTextLine(
            int index,
            String rawText,
            Integer left,
            Integer top,
            Integer right,
            Integer bottom
    ) {
        this(
                index,
                rawText,
                rawText,
                left,
                top,
                right,
                bottom,
                Collections.emptyList()
        );
    }

    public RecognizedTextLine(
            int index,
            String rawText,
            String reconstructedText,
            Integer left,
            Integer top,
            Integer right,
            Integer bottom,
            List<RecognizedTextElement> elements
    ) {
        if (index < 0) {
            throw new IllegalArgumentException(
                    "Line index cannot be negative"
            );
        }

        this.index = index;
        this.rawText =
                Objects.requireNonNull(
                        rawText,
                        "rawText"
                );

        this.reconstructedText =
                Objects.requireNonNull(
                        reconstructedText,
                        "reconstructedText"
                );

        Objects.requireNonNull(
                elements,
                "elements"
        );

        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;

        this.elements =
                Collections.unmodifiableList(
                        new ArrayList<>(elements)
                );
    }

    public int getIndex() {
        return index;
    }

    public String getRawText() {
        return rawText;
    }

    public String getReconstructedText() {
        return reconstructedText;
    }

    /**
     * Texto que debe utilizar la interfaz y la revisión.
     */
    public String getDisplayText() {
        if (!reconstructedText.trim().isEmpty()) {
            return reconstructedText;
        }

        return rawText;
    }

    public Integer getLeft() {
        return left;
    }

    public Integer getTop() {
        return top;
    }

    public Integer getRight() {
        return right;
    }

    public Integer getBottom() {
        return bottom;
    }

    public List<RecognizedTextElement> getElements() {
        return elements;
    }

    public boolean hasElements() {
        return !elements.isEmpty();
    }

    public boolean hasBoundingBox() {
        return left != null
                && top != null
                && right != null
                && bottom != null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof RecognizedTextLine)) {
            return false;
        }

        RecognizedTextLine that =
                (RecognizedTextLine) other;

        return index == that.index
                && rawText.equals(that.rawText)
                && reconstructedText.equals(
                that.reconstructedText
        )
                && Objects.equals(left, that.left)
                && Objects.equals(top, that.top)
                && Objects.equals(right, that.right)
                && Objects.equals(bottom, that.bottom)
                && elements.equals(that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                index,
                rawText,
                reconstructedText,
                left,
                top,
                right,
                bottom,
                elements
        );
    }
}
