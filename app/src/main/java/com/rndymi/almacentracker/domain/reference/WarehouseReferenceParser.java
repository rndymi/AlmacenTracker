package com.rndymi.almacentracker.domain.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WarehouseReferenceParser {

    private static final Pattern EXTRACTION_PATTERN =
            Pattern.compile(
                    "(?<![A-Z0-9])"
                            + "([A-Z]{2})"
                            + "\\s*"
                            + "([0-9]+)"
                            + "\\s*"
                            + "([A-Z]?)"
                            + "(?![A-Z0-9])"
            );

    private static final Pattern CATEGORY_PATTERN =
            Pattern.compile(
                    "^[A-Z]{2}$"
            );

    private static final Pattern CODE_PATTERN =
            Pattern.compile(
                    "^[0-9]+[A-Z]?$"
            );

    private static final Pattern UNICODE_SPACES =
            Pattern.compile(
                    "[\\p{Z}\\s]+"
            );

    public List<WarehouseReferenceMatch> parseLine(
            int lineIndex,
            String rawText
    ) {
        if (lineIndex < 0
                || rawText == null) {
            return Collections.emptyList();
        }

        String searchableText =
                normalizeSpaces(rawText)
                        .toUpperCase(Locale.ROOT);

        Matcher matcher =
                EXTRACTION_PATTERN.matcher(
                        searchableText
                );

        List<WarehouseReferenceMatch> matches =
                new ArrayList<>();

        int occurrenceIndex = 0;

        while (matcher.find()) {
            WarehouseReference reference =
                    new WarehouseReference(
                            matcher.group(1),
                            matcher.group(2)
                                    + matcher.group(3)
                    );

            matches.add(
                    new WarehouseReferenceMatch(
                            reference,
                            lineIndex,
                            rawText,
                            occurrenceIndex
                    )
            );

            occurrenceIndex++;
        }

        return Collections.unmodifiableList(
                matches
        );
    }

    public WarehouseReference parseInput(
            String category,
            String code
    ) {
        String normalizedCategory =
                normalizeCategory(category);

        String normalizedCode =
                normalizeCode(code);

        if (!isValidCategory(
                normalizedCategory
        )) {
            return null;
        }

        if (!isValidCode(
                normalizedCode
        )) {
            return null;
        }

        return new WarehouseReference(
                normalizedCategory,
                normalizedCode
        );
    }

    public String normalizeCategory(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return normalizeSpaces(value)
                .replace(" ", "")
                .toUpperCase(Locale.ROOT);
    }

    public String normalizeCode(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return normalizeSpaces(value)
                .replace(" ", "")
                .toUpperCase(Locale.ROOT);
    }

    public boolean isValidCategory(
            String category
    ) {
        return category != null
                && CATEGORY_PATTERN
                .matcher(category)
                .matches();
    }

    public boolean isValidCode(
            String code
    ) {
        return code != null
                && CODE_PATTERN
                .matcher(code)
                .matches();
    }

    private String normalizeSpaces(
            String value
    ) {
        return UNICODE_SPACES
                .matcher(value.trim())
                .replaceAll(" ");
    }
}