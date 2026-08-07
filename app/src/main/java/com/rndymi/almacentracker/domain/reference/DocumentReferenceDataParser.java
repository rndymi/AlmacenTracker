package com.rndymi.almacentracker.domain.reference;

import java.util.Collections;
import java.util.Locale;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DocumentReferenceDataParser {

    private static final Pattern DOCUMENT_DATA_DELIMITER_PATTERN =
            Pattern.compile(
                    "[\\-\u2010\u2011\u2012\u2013\u2014]"
                            + "|X(?=[\\p{Z}\\s]*"
                            + "[0-9ILSZGJBOTF()王]{1,3}"
                            + "[\\p{Z}\\s]*"
                            + "P(?:CS|QT|QTS)?(?:\\b|$))",
                    Pattern.CASE_INSENSITIVE
                            | Pattern.UNICODE_CASE
            );
    private static final Pattern DOCUMENT_DATA_PATTERN =
            Pattern.compile(
                    "^[\\p{Z}\\s:;,.\\-/]*"
                            + "([0-9ILSZGJBOTF()王]{1,9})"
                            + "(?:[\\p{Z}\\s]*"
                            + "([\\p{L}0-9]+"
                            + "(?:[\\p{Z}\\s]+"
                            + "[\\p{L}0-9]+){0,2}))?"
                            + "[\\p{Z}\\s:;,.\\-/]*$"
            );
    private static final Pattern MISSING_QUANTITY_PATTERN =
            Pattern.compile(
                    "^[\\p{Z}\\s:;,.\\-/]*"
                            + "(P(?:CS|QT|QTS)?)"
                            + "[\\p{Z}\\s:;,.\\-/]*$"
            );
    private static final Pattern DOCUMENT_DESTINATION_PATTERN =
            Pattern.compile(
                    "[①-⑳㉑-㉟㊱-㊿]"
            );
    private static final Pattern MISPLACED_REFERENCE_DASH_PATTERN =
            Pattern.compile(
                    "^[\\p{Z}\\s]*([A-Z]{1,2})"
                            + "[\\p{Z}\\s]*([0-9]{1,2})"
                            + "[\\p{Z}\\s]*[-\\u2010-\\u2014]"
                            + "[\\p{Z}\\s]*([0-9]{2,4})"
                            + "[\\p{Z}\\s]*[-\\u2010-\\u2014]?"
                            + "[\\p{Z}\\s]*$"
            );

    private final DocumentUnitNormalizer unitNormalizer;
    private final DocumentQuantityNormalizer quantityNormalizer;
    private final DocumentDestinationParser destinationParser;

    public DocumentReferenceDataParser() {
        this(
                new DocumentQuantityNormalizer(),
                new DocumentUnitNormalizer(),
                new DocumentDestinationParser()
        );
    }

    public DocumentReferenceDataParser(
            DocumentQuantityNormalizer quantityNormalizer,
            DocumentUnitNormalizer unitNormalizer
    ) {
        this(
                quantityNormalizer,
                unitNormalizer,
                new DocumentDestinationParser()
        );
    }

    public DocumentReferenceDataParser(
            DocumentUnitNormalizer unitNormalizer
    ) {
        this(
                new DocumentQuantityNormalizer(),
                unitNormalizer,
                new DocumentDestinationParser()
        );
    }

    DocumentReferenceDataParser(
            DocumentQuantityNormalizer quantityNormalizer,
            DocumentUnitNormalizer unitNormalizer,
            DocumentDestinationParser destinationParser
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

        this.destinationParser =
                Objects.requireNonNull(
                        destinationParser,
                        "destinationParser"
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

        String referenceTail =
                findTailAfterReference(
                        normalizedSource,
                        match.getObservedReference()
                );

        if (referenceTail == null
                && !match.getReference().equals(
                match.getObservedReference()
        )) {
            referenceTail =
                    findTailAfterReference(
                            normalizedSource,
                            match.getReference()
                    );
        }

        if (referenceTail != null) {
            return parseTail(
                    match,
                    referenceTail,
                    true
            );
        }


        if (isMisplacedDashInsideReference(
                normalizedSource,
                match.getObservedReference()
        )) {
            return withoutProposal(match);
        }

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

        return withoutProposal(match);
    }

    private boolean isMisplacedDashInsideReference(
            String normalizedSource,
            WarehouseReference observedReference
    ) {
        if (observedReference == null) {
            return false;
        }

        Matcher matcher = MISPLACED_REFERENCE_DASH_PATTERN
                .matcher(normalizedSource);

        return matcher.matches()
                && matcher.group(1).equals(
                observedReference.getCategory()
        )
                && (matcher.group(2) + matcher.group(3))
                .equals(observedReference.getCode());
    }

    private DocumentReferenceData parseTail(
            WarehouseReferenceMatch match,
            String sourceTail,
            boolean requireUnit
    ) {
        String normalizedTail =
                normalize(
                        DOCUMENT_DESTINATION_PATTERN
                                .matcher(sourceTail)
                                .replaceAll(" ")
                );

        Matcher matcher =
                DOCUMENT_DATA_PATTERN.matcher(
                        normalizedTail
                );

        if (!matcher.matches()) {
            Matcher missingQuantityMatcher =
                    MISSING_QUANTITY_PATTERN.matcher(
                            normalizedTail
                    );

            if (missingQuantityMatcher.matches()) {
                return new DocumentReferenceData(
                        match.getReference(),
                        match.getObservedReference(),
                        1,
                        unitNormalizer.normalize(
                                missingQuantityMatcher.group(1)
                        ),
                        match.getSourceLineIndex(),
                        match.getSourceRawText(),
                        Collections.emptyList(),
                        destinationParser.parse(sourceTail)
                );
            }

            return withoutProposal(match);
        }

        String observedQuantity = matcher.group(1);
        String observedUnit = matcher.group(2);
        Integer quantity = quantityNormalizer.normalize(
                observedQuantity
        );

        List<Integer> quantitySuggestions =
                quantityNormalizer
                        .suggestZeroSixAlternatives(
                                observedQuantity
                        );

        if (quantity == null
                && quantitySuggestions.isEmpty()) {
            return withoutProposal(match);
        }

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

        List<String> destinations =
                destinationParser.parse(
                        sourceTail
                );

        return new DocumentReferenceData(
                match.getReference(),
                match.getObservedReference(),
                quantity,
                unit,
                match.getSourceLineIndex(),
                match.getSourceRawText(),
                quantitySuggestions,
                destinations
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
                match.getObservedReference(),
                null,
                null,
                match.getSourceLineIndex(),
                match.getSourceRawText(),
                Collections.emptyList(),
                destinationsAfterReference(match)
        );
    }

    private List<String> destinationsAfterReference(
            WarehouseReferenceMatch match
    ) {
        String sourceText = match.getSourceRawText();

        if (sourceText == null) {
            return Collections.emptyList();
        }

        String normalized = normalize(sourceText);
        String tail = findTailAfterReference(
                normalized,
                match.getObservedReference()
        );

        return tail == null
                ? Collections.emptyList()
                : destinationParser.parse(tail);
    }
}
