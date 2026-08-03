package com.rndymi.almacentracker.domain.reference;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DocumentTitleParser {

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
                return null;
            }

            return normalized;
        }

        return null;
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
