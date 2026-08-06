package com.rndymi.almacentracker.evaluation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class OcrExpectedResult {

    private final String caseId;
    private final List<OcrExpectedRegion> regions;
    private final List<OcrExpectedLine> lines;
    private final List<OcrExpectedReference> references;
    private final String expectedTitle;
    private final String expectedBuyerOrStore;
    private final String expectedGlobalDestination;

    public OcrExpectedResult(
            String caseId,
            List<OcrExpectedRegion> regions,
            List<OcrExpectedLine> lines,
            List<OcrExpectedReference> references,
            String expectedTitle,
            String expectedBuyerOrStore,
            String expectedGlobalDestination
    ) {
        this.caseId =
                requireText(caseId, "caseId");

        this.regions = immutableCopy(
                regions,
                "regions"
        );

        this.lines = immutableCopy(
                lines,
                "lines"
        );

        this.references = immutableCopy(
                references,
                "references"
        );

        this.expectedTitle =
                normalizeNullable(expectedTitle);

        this.expectedBuyerOrStore =
                normalizeNullable(
                        expectedBuyerOrStore
                );

        this.expectedGlobalDestination =
                normalizeNullable(
                        expectedGlobalDestination
                );
    }

    public String getCaseId() {
        return caseId;
    }

    public List<OcrExpectedRegion> getRegions() {
        return regions;
    }

    public List<OcrExpectedLine> getLines() {
        return lines;
    }

    public List<OcrExpectedReference> getReferences() {
        return references;
    }

    public String getExpectedTitle() {
        return expectedTitle;
    }

    public String getExpectedBuyerOrStore() {
        return expectedBuyerOrStore;
    }

    public String getExpectedGlobalDestination() {
        return expectedGlobalDestination;
    }

    private static <T> List<T> immutableCopy(
            List<T> source,
            String fieldName
    ) {
        Objects.requireNonNull(source, fieldName);

        List<T> result =
                new ArrayList<>(source.size());

        for (T value : source) {
            result.add(
                    Objects.requireNonNull(
                            value,
                            fieldName
                                    + " cannot contain null"
                    )
            );
        }

        return Collections.unmodifiableList(result);
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

    private static String normalizeNullable(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}
