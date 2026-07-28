package com.rndymi.almacentracker.core.document;

import java.util.Objects;

public final class RecognizedTextElement {

    private final String rawText;
    private final int left;
    private final int top;
    private final int right;
    private final int bottom;

    public RecognizedTextElement(
            String rawText,
            int left,
            int top,
            int right,
            int bottom
    ) {
        this.rawText =
                Objects.requireNonNull(
                        rawText,
                        "rawText"
                ).trim();

        if (right < left) {
            throw new IllegalArgumentException(
                    "right cannot be lower than left"
            );
        }

        if (bottom < top) {
            throw new IllegalArgumentException(
                    "bottom cannot be lower than top"
            );
        }

        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public String getRawText() {
        return rawText;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getRight() {
        return right;
    }

    public int getBottom() {
        return bottom;
    }

    public int getWidth() {
        return right - left;
    }

    public int getHeight() {
        return bottom - top;
    }

    public float getCenterY() {
        return top + getHeight() / 2.0f;
    }

    public boolean hasText() {
        return !rawText.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof RecognizedTextElement)) {
            return false;
        }

        RecognizedTextElement that =
                (RecognizedTextElement) other;

        return left == that.left
                && top == that.top
                && right == that.right
                && bottom == that.bottom
                && rawText.equals(that.rawText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                rawText,
                left,
                top,
                right,
                bottom
        );
    }
}
