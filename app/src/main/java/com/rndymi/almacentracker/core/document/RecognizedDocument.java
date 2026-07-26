package com.rndymi.almacentracker.core.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class RecognizedDocument {

    private final DocumentImageSource sourceType;
    private final List<RecognizedTextLine> lines;
    private final long recognizedAt;

    public RecognizedDocument(
            DocumentImageSource sourceType,
            List<RecognizedTextLine> lines,
            long recognizedAt
    ) {
        this.sourceType = Objects.requireNonNull(
                sourceType,
                "sourceType"
        );

        Objects.requireNonNull(lines, "lines");

        this.lines = Collections.unmodifiableList(
                new ArrayList<>(lines)
        );
        this.recognizedAt = recognizedAt;
    }

    public DocumentImageSource getSourceType() {
        return sourceType;
    }

    public List<RecognizedTextLine> getLines() {
        return lines;
    }

    public long getRecognizedAt() {
        return recognizedAt;
    }

    public boolean hasLines() {
        return !lines.isEmpty();
    }

    public int getLineCount() {
        return lines.size();
    }
}