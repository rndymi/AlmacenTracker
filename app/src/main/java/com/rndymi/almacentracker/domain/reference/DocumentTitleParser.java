package com.rndymi.almacentracker.domain.reference;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class DocumentTitleParser {

    private static final Pattern SINGLE_LETTER_REFERENCE =
            Pattern.compile(
                    "^[A-Z][\\p{Z}\\s]*[0-9]{3,5}"
                            + "(?:[\\p{Z}\\s]*"
                            + "[-\\u2010-\\u2014]"
                            + "[\\p{Z}\\s]*[0-9]{1,3})?"
                            + "(?:[\\p{Z}\\s]*"
                            + "[-\\u2010-\\u2014]"
                            + "[\\p{Z}\\s]*[0-9]{1,3}"
                            + "[\\p{Z}\\s]*P)?$",
                    Pattern.CASE_INSENSITIVE
            );

    private final WarehouseReferenceParser
            referenceParser;

    public DocumentTitleParser(
            WarehouseReferenceParser referenceParser
    ) {
        this.referenceParser =
                Objects.requireNonNull(
                        referenceParser,
                        "referenceParser"
                );
    }

    public String parse(
            List<String> reconstructedLines
    ) {
        if (reconstructedLines == null
                || reconstructedLines.isEmpty()) {
            return null;
        }

        List<String> contextLines = new ArrayList<>();

        for (String line : reconstructedLines) {
            String normalized =
                    normalize(line);

            if (normalized.isEmpty()) {
                continue;
            }

            List<WarehouseReferenceMatch> matches =
                    referenceParser.parseOcrLine(
                            0,
                            normalized,
                            Collections.emptyList()
                    );

            if (!matches.isEmpty()) {
                break;
            }

            if (SINGLE_LETTER_REFERENCE
                    .matcher(normalized)
                    .matches()) {
                break;
            }

            if (!contextLines.contains(normalized)) {
                contextLines.add(normalized);
            }
        }

        return contextLines.isEmpty()
                ? null
                : String.join(
                        " · ",
                        contextLines
                );
    }

    private String normalize(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value
                .replace('\u00A0', ' ')
                .replaceAll(
                        "[\\p{Z}\\s]+",
                        " "
                )
                .trim();
    }
}
