package com.rndymi.almacentracker.evaluation.model;

import com.rndymi.almacentracker.evaluation.metrics.NormalizedBox;

import java.util.Objects;

public final class OcrExpectedRegion {

    private final String id;
    private final String text;
    private final NormalizedBox box;
    private final boolean optional;

    public OcrExpectedRegion(
            String id,
            String text,
            NormalizedBox box,
            boolean optional
    ) {
        this.id = requireText(id, "id");
        this.text = requireText(text, "text");
        this.box = Objects.requireNonNull(
                box,
                "box"
        );
        this.optional = optional;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public NormalizedBox getBox() {
        return box;
    }

    public boolean isOptional() {
        return optional;
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        Objects.requireNonNull(value, fieldName);

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }

        return trimmed;
    }
}
