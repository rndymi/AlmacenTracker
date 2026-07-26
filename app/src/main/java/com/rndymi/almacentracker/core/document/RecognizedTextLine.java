package com.rndymi.almacentracker.core.document;

import java.util.Objects;

public final class RecognizedTextLine {

    private final int index;
    private final String rawText;
    private final Integer left;
    private final Integer top;
    private final Integer right;
    private final Integer bottom;

    public RecognizedTextLine(
            int index,
            String rawText
    ) {
        this(
                index,
                rawText,
                null,
                null,
                null,
                null
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
        if (index < 0) {
            throw new IllegalArgumentException(
                    "Line index cannot be negative"
            );
        }

        this.index = index;
        this.rawText = Objects.requireNonNull(
                rawText,
                "rawText"
        );
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public int getIndex() {
        return index;
    }

    public String getRawText() {
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
                && Objects.equals(left, that.left)
                && Objects.equals(top, that.top)
                && Objects.equals(right, that.right)
                && Objects.equals(bottom, that.bottom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                index,
                rawText,
                left,
                top,
                right,
                bottom
        );
    }
}