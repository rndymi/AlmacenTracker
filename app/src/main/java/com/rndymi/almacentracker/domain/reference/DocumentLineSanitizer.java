package com.rndymi.almacentracker.domain.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class DocumentLineSanitizer {

    private static final Pattern OCR_REFERENCE_LIKE =
            Pattern.compile(
                    "^[A-Z0-9()]{1,2}"
                            + "[\\p{Z}\\s:._-]*"
                            + "[0-9ILSZGJBOTF()王]{2,}.*$",
                    Pattern.CASE_INSENSITIVE
                            | Pattern.UNICODE_CASE
            );

    private final WarehouseReferenceParser referenceParser;

    public DocumentLineSanitizer(
            WarehouseReferenceParser referenceParser
    ) {
        this.referenceParser = referenceParser;
    }

    public List<String> sanitize(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }

        int firstReferenceIndex = firstReferenceIndex(lines);
        int headerStart = trailingHeaderStart(
                lines,
                firstReferenceIndex
        );
        List<String> result = new ArrayList<>();

        for (int index = headerStart;
             index < lines.size();
             index++) {
            String normalized = normalize(lines.get(index));

            if (normalized.isEmpty()
                    || normalized.matches("^[0-9]$")) {
                continue;
            }

            result.add(normalized);
        }

        return result.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(result);
    }

    private int firstReferenceIndex(List<String> lines) {
        for (int index = 0; index < lines.size(); index++) {
            String line = normalize(lines.get(index));

            for (WarehouseReferenceMatch match
                    : referenceParser.parseOcrLine(
                    index,
                    line,
                    Collections.emptyList()
            )) {
                WarehouseReference reference =
                        match.getObservedReference();

                if (referenceParser.isValidCategory(
                        reference.getCategory()
                )
                        && referenceParser.isValidCode(
                        reference.getCode()
                )) {
                    return index;
                }
            }
        }

        return lines.size();
    }

    private int trailingHeaderStart(
            List<String> lines,
            int firstReferenceIndex
    ) {
        int start = firstReferenceIndex;

        for (int index = firstReferenceIndex - 1;
             index >= 0;
             index--) {
            String line = normalize(lines.get(index));

            if (line.isEmpty()) {
                continue;
            }

            if (!isHeaderLine(line)
                    && !OCR_REFERENCE_LIKE
                    .matcher(line)
                    .matches()) {
                break;
            }

            start = index;
        }

        return start;
    }

    private boolean isHeaderLine(String line) {
        return line.matches(
                "^[\\p{L}]+(?:[\\p{Z}\\s]+\\p{L}+)*$"
        );
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.replace('\u00A0', ' ')
                .replaceAll("[\\p{Z}\\s]+", " ")
                .trim();
    }
}
