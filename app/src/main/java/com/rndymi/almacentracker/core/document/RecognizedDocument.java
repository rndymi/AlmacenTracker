package com.rndymi.almacentracker.core.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class RecognizedDocument {

    private final DocumentImageSource sourceType;
    private final List<RecognizedTextLine> lines;
    private final List<String> rawLines;
    private final long recognizedAt;

    public RecognizedDocument(
            DocumentImageSource sourceType,
            List<RecognizedTextLine> lines,
            long recognizedAt
    ) {
        this.sourceType =
                Objects.requireNonNull(
                        sourceType,
                        "sourceType"
                );

        Objects.requireNonNull(
                lines,
                "lines"
        );

        this.lines =
                Collections.unmodifiableList(
                        new ArrayList<>(lines)
                );

        List<String> rawTextLines =
                new ArrayList<>();

        for (RecognizedTextLine line : lines) {
            rawTextLines.add(line.getRawText());
        }

        this.rawLines =
                Collections.unmodifiableList(
                        rawTextLines
                );

        this.recognizedAt = recognizedAt;
    }

    public DocumentImageSource getSourceType() {
        return sourceType;
    }

    public List<RecognizedTextLine> getLines() {
        return lines;
    }

    public List<String> getRawLines() {
        return rawLines;
    }

    public List<String> getReconstructedLines() {
        List<String> result =
                new ArrayList<>();

        for (RecognizedTextLine line : lines) {
            String text =
                    line.getDisplayText().trim();

            if (!text.isEmpty()) {
                result.add(text);
            }
        }

        return Collections.unmodifiableList(result);
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
