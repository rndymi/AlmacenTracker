package com.rndymi.almacentracker.domain.reference;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DocumentReferenceDataParser {

    private static final Pattern DOCUMENT_DATA_DELIMITER_PATTERN =
            Pattern.compile(
                    "[\\-\u2010\u2011\u2012\u2013\u2014]"
            );
    private static final Pattern DOCUMENT_DATA_PATTERN =
            Pattern.compile(
                    "^[\\p{Z}\\s:;,.\\-/]*"
                            + "([0-9ILSZGJBO]{1,9})"
                            + "(?:[\\p{Z}\\s]*"
                            + "([\\p{L}0-9]+"
                            + "(?:[\\p{Z}\\s]+"
                            + "[\\p{L}0-9]+){0,2}))?"
                            + "[\\p{Z}\\s:;,.\\-/]*$"
            );

    private final DocumentUnitNormalizer unitNormalizer;
    private final DocumentQuantityNormalizer quantityNormalizer;

    public DocumentReferenceDataParser() {
        this(
                new DocumentQuantityNormalizer(),
                new DocumentUnitNormalizer()
        );
    }

    public DocumentReferenceDataParser(
            DocumentQuantityNormalizer quantityNormalizer,
            DocumentUnitNormalizer unitNormalizer
    ) {
        this.quantityNormalizer =
                Objects.requireNonNull(
                        quantityNormalizer,
                        "quantityNormalizer"
                );

        this.unitNormalizer =
                Objects.requireNonNull(
                        unitNormalizer,
                        "unitNormalizer"
                );
    }

    public DocumentReferenceDataParser(
            DocumentUnitNormalizer unitNormalizer
    ) {
        this(
                new DocumentQuantityNormalizer(),
                unitNormalizer
        );
    }

    public DocumentReferenceData parse(
            WarehouseReferenceMatch match
    ) {
        if (match == null) {
            return null;
        }

        String sourceText =
                match.getSourceRawText();

        if (sourceText == null
                || sourceText.trim().isEmpty()) {
            return withoutProposal(match);
        }

        String normalizedSource =
                normalize(sourceText);

        Matcher delimiterMatcher =
                DOCUMENT_DATA_DELIMITER_PATTERN
                        .matcher(normalizedSource);

        if (delimiterMatcher.find()) {
            return parseTail(
                    match,
                    normalizedSource.substring(
                            delimiterMatcher.end()
                    ),
                    false
            );
        }

        String fallbackTail =
                findTailAfterReference(
                        normalizedSource,
                        match.getReference()
                );

        if (fallbackTail == null) {
            return withoutProposal(match);
        }

        return parseTail(
                match,
                fallbackTail,
                true
        );
    }

    private DocumentReferenceData parseTail(
            WarehouseReferenceMatch match,
            String sourceTail,
            boolean requireUnit
    ) {
        String normalizedTail =
                normalize(sourceTail);

        Matcher matcher =
                DOCUMENT_DATA_PATTERN.matcher(
                        normalizedTail
                );

        if (!matcher.matches()) {
            return withoutProposal(match);
        }

        Integer quantity =
                quantityNormalizer.normalize(
                        matcher.group(1)
                );

        if (quantity == null) {
            return withoutProposal(match);
        }

        String observedUnit =
                matcher.group(2);

        String unit =
                unitNormalizer.normalize(
                        observedUnit
                );

        if (observedUnit != null
                && unit == null) {
            return withoutProposal(match);
        }

        if (requireUnit
                && unit == null) {
            return withoutProposal(match);
        }

        return new DocumentReferenceData(
                match.getReference(),
                quantity,
                unit,
                match.getSourceLineIndex(),
                match.getSourceRawText()
        );
    }

    private String findTailAfterReference(
            String normalizedSource,
            WarehouseReference reference
    ) {
        if (reference == null) {
            return null;
        }

        Matcher matcher =
                buildReferencePattern(reference)
                        .matcher(normalizedSource);

        if (!matcher.find()) {
            return null;
        }

        return normalizedSource.substring(
                matcher.end()
        );
    }

    private Pattern buildReferencePattern(
            WarehouseReference reference
    ) {
        return Pattern.compile(
                "(?<![A-Z0-9])"
                        + flexibleCharactersPattern(
                        reference.getCategory()
                )
                        + "[\\p{Z}\\s:._]*"
                        + flexibleCharactersPattern(
                        reference.getCode()
                )
                        + "(?![A-Z0-9])"
        );
    }

    private String flexibleCharactersPattern(
            String value
    ) {
        String normalized =
                normalize(value)
                        .replace(" ", "");

        StringBuilder pattern =
                new StringBuilder();

        for (int index = 0;
             index < normalized.length();
             index++) {

            if (index > 0) {
                pattern.append(
                        "[\\p{Z}\\s:._]*"
                );
            }

            pattern.append(
                    Pattern.quote(
                            String.valueOf(
                                    normalized.charAt(index)
                            )
                    )
            );
        }

        return pattern.toString();
    }

    private String normalize(
            String value
    ) {
        return value
                .replace('\u00A0', ' ')
                .replaceAll(
                        "[\\p{Z}\\s]+",
                        " "
                )
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private DocumentReferenceData withoutProposal(
            WarehouseReferenceMatch match
    ) {
        return new DocumentReferenceData(
                match.getReference(),
                null,
                null,
                match.getSourceLineIndex(),
                match.getSourceRawText()
        );
    }
}