package com.rndymi.almacentracker.domain.reference;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DocumentReferenceDataParser {

    private static final Pattern QUANTITY_UNIT_PATTERN =
            Pattern.compile(
                    "(?:-|–|—|:)?" +
                            "\\s*" +
                            "([1-9][0-9]{0,8})" +
                            "\\s*" +
                            "([\\p{L}]{1,30})" +
                            "(?![\\p{L}0-9])"
            );

    public DocumentReferenceData parse(
            WarehouseReferenceMatch match
    ) {
        if (match == null) {
            return null;
        }

        String sourceText = match.getSourceRawText();

        if (sourceText == null
                || sourceText.trim().isEmpty()) {
            return withoutProposal(match);
        }

        String normalizedSource =
                sourceText
                        .replace('\u00A0', ' ')
                        .replaceAll("\\s+", " ")
                        .trim()
                        .toUpperCase(Locale.ROOT);

        Matcher matcher =
                QUANTITY_UNIT_PATTERN.matcher(
                        normalizedSource
                );

        DocumentReferenceData candidate = null;

        while (matcher.find()) {
            String quantityText = matcher.group(1);
            String unitText = matcher.group(2);

            if (!DocumentQuantityUnitVocabulary
                    .isKnownUnit(unitText)) {
                continue;
            }

            Integer quantity =
                    parsePositiveQuantity(
                            quantityText
                    );

            if (quantity == null) {
                continue;
            }

            if (candidate != null) {
                return withoutProposal(match);
            }

            candidate =
                    new DocumentReferenceData(
                            match.getReference(),
                            quantity,
                            DocumentQuantityUnitVocabulary
                                    .normalize(unitText),
                            match.getSourceLineIndex(),
                            sourceText
                    );
        }

        return candidate == null
                ? withoutProposal(match)
                : candidate;
    }

    private static DocumentReferenceData withoutProposal(
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

    private static Integer parsePositiveQuantity(
            String value
    ) {
        try {
            int quantity = Integer.parseInt(value);

            return quantity > 0
                    ? quantity
                    : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}