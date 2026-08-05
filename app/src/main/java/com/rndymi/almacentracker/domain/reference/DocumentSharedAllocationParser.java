package com.rndymi.almacentracker.domain.reference;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DocumentSharedAllocationParser {

    private static final Pattern ALLOCATION_PATTERN =
            Pattern.compile(
                    "^[\\p{Z}\\s:;,.>]*"
                            + "([0-9ILSZGJBOTF()王]{0,9})"
                            + "[\\p{Z}\\s]*"
                            + "(P(?:CS|QT|QTS)?)"
                            + "[\\p{Z}\\s]*"
                            + "[-\\u2010-\\u2014:]"
                            + "[\\p{Z}\\s]*"
                            + "(TIENDA[\\p{Z}\\s]*"
                            + "(?:[0-9]{1,3}|[①-⑳㉑-㉟㊱-㊿])"
                            + "|[①-⑳㉑-㉟㊱-㊿]+)"
                            + "[\\p{Z}\\s:;,.]*$",
                    Pattern.CASE_INSENSITIVE
                            | Pattern.UNICODE_CASE
            );

    private final DocumentQuantityNormalizer quantityNormalizer;
    private final DocumentUnitNormalizer unitNormalizer;

    public DocumentSharedAllocationParser() {
        this(
                new DocumentQuantityNormalizer(),
                new DocumentUnitNormalizer()
        );
    }

    DocumentSharedAllocationParser(
            DocumentQuantityNormalizer quantityNormalizer,
            DocumentUnitNormalizer unitNormalizer
    ) {
        this.quantityNormalizer = quantityNormalizer;
        this.unitNormalizer = unitNormalizer;
    }

    public DocumentReferenceAllocation parse(
            String sourceText
    ) {
        if (sourceText == null) {
            return null;
        }

        Matcher matcher = ALLOCATION_PATTERN.matcher(
                sourceText.trim()
        );

        if (!matcher.matches()) {
            return null;
        }

        Integer quantity = matcher.group(1).isEmpty()
                ? 1
                : quantityNormalizer.normalize(
                        matcher.group(1)
                );
        String unit = unitNormalizer.normalize(
                matcher.group(2)
        );

        if (quantity == null || unit == null) {
            return null;
        }

        String destination = matcher.group(3)
                .replaceAll(
                        "[\\p{Z}\\s]+",
                        " "
                )
                .trim();

        if (destination.toUpperCase(Locale.ROOT)
                .startsWith("TIENDA")) {
            destination = "Tienda"
                    + destination.substring(6);
        }

        return new DocumentReferenceAllocation(
                quantity,
                unit,
                destination
        );
    }
}
