package com.rndymi.almacentracker.domain.reference;

import com.rndymi.almacentracker.domain.rule.WarehouseItemNormalizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WarehouseReferenceParser {

    private static final WarehouseItemNormalizer NORMALIZER =
            new WarehouseItemNormalizer();

    private static final Pattern EXTRACTION_PATTERN =
            Pattern.compile(
                    "(?<![A-Z0-9])"
                            + "([A-Z]{2})"
                            + "\\s*"
                            + "([0-9]{4,5})"
                            + "(?:\\s*"
                            + "(\\p{L}+(?:\\s+\\p{L}+)*))?"
                            + "(?![A-Z0-9])"
            );

    private static final Pattern OCR_EXTRACTION_PATTERN =
            Pattern.compile(
                    "(?<![A-Z0-9])"
                            + "([A-Z0-9]{2})"
                            + "[\\p{Z}\\s:._-]*"
                            + "([A-Z0-9]{4,})"
                            + "(?:[\\p{Z}\\s]+"
                            + "(\\p{L}+(?:[\\p{Z}\\s]+"
                            + "\\p{L}+)*))?"
                            + "(?![A-Z0-9])"
            );

    private static final Pattern CATEGORY_PATTERN =
            Pattern.compile(
                    "^[A-Z]{2}$"
            );

    private static final Pattern CODE_PATTERN =
            Pattern.compile(
                    "^[0-9]{4,5}(?: \\p{L}+)*$"
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
                            canonicalCode(
                                    matcher.group(2),
                                    matcher.group(3)
                            )
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

            if (hasQuantityDelimiterAfter(
                    searchableText,
                    matcher.end()
            )) {
                break;
            }
        }

        return Collections.unmodifiableList(
                matches
        );
    }

    public List<WarehouseReferenceMatch>
    parseOcrLine(
            int lineIndex,
            String rawText,
            List<WarehouseReference> knownReferences
    ) {
        List<WarehouseReferenceMatch> exactMatches =
                parseLine(lineIndex, rawText);

        if (rawText == null
                || !exactMatches.isEmpty()) {
            return exactMatches;
        }

        return extractOcrCandidates(
                        lineIndex,
                        rawText
                );
    }

    public List<WarehouseReference> suggestReferences(
            WarehouseReference observed,
            List<WarehouseReference> knownReferences,
            int maximumSuggestions
    ) {
        if (observed == null
                || knownReferences == null
                || knownReferences.isEmpty()
                || maximumSuggestions <= 0) {
            return Collections.emptyList();
        }

        List<ScoredReference> scored =
                new ArrayList<>();

        for (WarehouseReference known : knownReferences) {
            if (known == null) {
                continue;
            }

            WarehouseReference normalizedKnown =
                    parseInput(
                            known.getCategory(),
                            known.getCode()
                    );

            if (normalizedKnown == null
                    || normalizedKnown.equals(observed)) {
                continue;
            }

            int score =
                    suggestionScore(
                            observed,
                            normalizedKnown
                    );

            if (score >= 0) {
                scored.add(
                        new ScoredReference(
                                normalizedKnown,
                                score
                        )
                );
            }
        }

        scored.sort(
                Comparator
                        .comparingInt(
                                ScoredReference::getScore
                        )
                        .thenComparing(
                                value -> value.reference
                                        .displayValue()
                        )
        );

        List<WarehouseReference> suggestions =
                new ArrayList<>();

        for (ScoredReference candidate : scored) {
            if (suggestions.contains(
                    candidate.reference
            )) {
                continue;
            }

            suggestions.add(candidate.reference);

            if (suggestions.size()
                    == maximumSuggestions) {
                break;
            }
        }

        return suggestions.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(
                        suggestions
                );
    }

    private List<WarehouseReferenceMatch>
    extractOcrCandidates(
            int lineIndex,
            String rawText
    ) {
        String searchableText =
                normalizeSpaces(rawText)
                        .toUpperCase(Locale.ROOT);

        Matcher matcher =
                OCR_EXTRACTION_PATTERN.matcher(
                        searchableText
                );

        List<WarehouseReferenceMatch> candidates =
                new ArrayList<>();

        int occurrenceIndex = 0;

        while (matcher.find()) {
            candidates.add(
                    new WarehouseReferenceMatch(
                            new WarehouseReference(
                                    matcher.group(1),
                                    canonicalOcrCode(
                                            matcher.group(2),
                                            matcher.group(3)
                                    )
                            ),
                            lineIndex,
                            rawText,
                            occurrenceIndex++
                    )
            );

            if (hasQuantityDelimiterAfter(
                    searchableText,
                    matcher.end()
            )) {
                break;
            }
        }

        return candidates.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(candidates);
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
        return NORMALIZER.normalizeCode(value);
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

    private String canonicalCode(
            String numericPart,
            String suffix
    ) {
        if (suffix == null
                || suffix.trim().isEmpty()) {
            return numericPart;
        }

        String normalizedSuffix =
                normalizeSpaces(suffix);

        if (isQuantityUnit(
                normalizedSuffix
        )) {
            return numericPart;
        }

        return numericPart
                + " "
                + normalizedSuffix;
    }

    private boolean hasQuantityDelimiterAfter(
            String text,
            int matchEnd
    ) {
        for (int index = matchEnd;
             index < text.length();
             index++) {
            char character = text.charAt(index);

            if (Character.isWhitespace(character)
                    || Character.isSpaceChar(character)) {
                continue;
            }

            return character == '-'
                    || character == '\u2010'
                    || character == '\u2011'
                    || character == '\u2012'
                    || character == '\u2013'
                    || character == '\u2014';
        }

        return false;
    }

    private String canonicalOcrCode(
            String observedCode,
            String separatedSuffix
    ) {
        String code = observedCode;
        String suffix = separatedSuffix;

        if (observedCode.length() > 5) {
            int codeLength =
                    Character.isDigit(
                            observedCode.charAt(4)
                    )
                            ? 5
                            : 4;

            String attachedSuffix =
                    observedCode.substring(codeLength);

            if (attachedSuffix
                    .codePoints()
                    .allMatch(Character::isLetter)) {
                code =
                        observedCode.substring(
                                0,
                                codeLength
                        );

                suffix =
                        suffix == null
                                || suffix.trim().isEmpty()
                                ? attachedSuffix
                                : attachedSuffix
                                + " "
                                + suffix;
            }
        }

        return canonicalCode(code, suffix);
    }

    private int suggestionScore(
            WarehouseReference observed,
            WarehouseReference expected
    ) {
        int categoryScore =
                categorySuggestionScore(
                        observed.getCategory(),
                        expected.getCategory()
                );

        if (categoryScore < 0) {
            return -1;
        }

        int codeScore =
                codeSuggestionScore(
                        observed.getCode(),
                        expected.getCode()
                );

        if (codeScore < 0) {
            return -1;
        }

        return categoryScore + codeScore;
    }

    private int categorySuggestionScore(
            String observed,
            String expected
    ) {
        if (observed.length() != expected.length()) {
            return -1;
        }

        int differences = 0;
        int score = 0;

        for (int index = 0;
             index < expected.length();
             index++) {

            char observedCharacter =
                    observed.charAt(index);

            char expectedCharacter =
                    expected.charAt(index);

            if (observedCharacter
                    == expectedCharacter) {
                continue;
            }

            differences++;

            if (differences > 1) {
                return -1;
            }

            int confusionScore =
                    categoryConfusionScore(
                            observedCharacter,
                            expectedCharacter
                    );

            if (confusionScore < 0) {
                return -1;
            }

            score += confusionScore;
        }

        return score;
    }

    private int categoryConfusionScore(
            char observed,
            char expected
    ) {

        if ((observed == '5'
                && expected == 'S')
                || (observed == '2'
                && expected == 'Z')
                || (observed == '0'
                && expected == 'O')
                || (observed == '8'
                && expected == 'B')
                || (observed == '1'
                && (
                expected == 'I'
                        || expected == 'L'
        ))) {
            return 1;
        }

        if ((observed == 'E'
                && expected == 'R')
                || (observed == '2'
                && expected == 'R')) {
            return 3;
        }

        return -1;
    }

    private int codeSuggestionScore(
            String observed,
            String expected
    ) {
        CodeParts observedParts =
                codeParts(observed);

        CodeParts expectedParts =
                codeParts(expected);

        if (observedParts.numeric.length()
                != expectedParts.numeric.length()) {
            return -1;
        }

        int digitDifferences = 0;
        int score = 0;

        for (int index = 0;
             index < expectedParts.numeric.length();
             index++) {

            char observedCharacter =
                    observedParts.numeric.charAt(index);

            char expectedCharacter =
                    expectedParts.numeric.charAt(index);

            if (observedCharacter
                    == expectedCharacter) {
                continue;
            }

            int confusionScore =
                    codeConfusionScore(
                            observedCharacter,
                            expectedCharacter
                    );

            if (confusionScore < 0) {
                return -1;
            }

            if (Character.isDigit(
                    observedCharacter
            )) {
                digitDifferences++;

                if (digitDifferences > 1) {
                    return -1;
                }
            }

            score += confusionScore;
        }

        int suffixScore =
                suffixSuggestionScore(
                        observedParts.suffix,
                        expectedParts.suffix
                );

        if (suffixScore < 0) {
            return -1;
        }

        return score + suffixScore;
    }

    private int codeConfusionScore(
            char observed,
            char expected
    ) {

        if ((
                observed == 'I'
                        || observed == 'L'
        ) && (
                expected == '1'
                        || expected == '7'
        )) {
            return 1;
        }

        if ((observed == 'O'
                && expected == '0')
                || (observed == 'S'
                && expected == '5')
                || (observed == 'Z'
                && expected == '2')
                || (observed == 'B'
                && expected == '8')
                || (observed == 'G'
                && expected == '6')) {
            return 1;
        }


        if ((observed == '1'
                && expected == '7')
                || (observed == '9'
                && expected == '7')) {
            return 3;
        }


        return -1;
    }

    private int suffixSuggestionScore(
            String observed,
            String expected
    ) {
        String normalizedObserved =
                normalizeOcrSuffix(observed);

        String normalizedExpected =
                normalizeOcrSuffix(expected);

        if (normalizedObserved.equals(
                normalizedExpected
        )) {
            return 0;
        }

        if (normalizedObserved.isEmpty()
                && !normalizedExpected.isEmpty()) {
            return 2;
        }

        return -1;
    }

    private String normalizeOcrSuffix(
            String suffix
    ) {
        String normalized =
                suffix == null
                        ? ""
                        : normalizeSpaces(suffix)
                        .toUpperCase(Locale.ROOT);

        return isQuantityUnit(normalized)
                ? ""
                : normalized;
    }

    private boolean isQuantityUnit(
            String value
    ) {
        String normalized =
                value == null
                        ? ""
                        : normalizeSpaces(value)
                        .toUpperCase(Locale.ROOT);

        switch (normalized) {
            case "PC":
            case "PCS":
            case "PES":
            case "PZ":
            case "PZS":
            case "PZA":
            case "PZAS":
            case "PIEZA":
            case "PIEZAS":
            case "PQT":
            case "PQTS":
            case "PAT":
            case "PATS":
            case "PAQ":
            case "PAQS":
            case "PAQUETE":
            case "PAQUETES":
            case "UD":
            case "UDS":
            case "UN":
            case "UNS":
            case "UND":
            case "UNDS":
            case "UNIDAD":
            case "UNIDADES":
            case "CJ":
            case "CJS":
            case "CJA":
            case "CJAS":
            case "CAJA":
            case "CAJAS":
            case "BTO":
            case "BTOS":
            case "BULTO":
            case "BULTOS":
            case "PACK":
            case "PACKS":
            case "BOX":
            case "BOXES":
            case "CTN":
            case "CTNS":
                return true;

            default:
                return false;
        }
    }

    private CodeParts codeParts(
            String code
    ) {
        String normalized =
                code == null
                        ? ""
                        : normalizeSpaces(code)
                        .toUpperCase(Locale.ROOT);

        int separatorIndex =
                normalized.indexOf(' ');

        if (separatorIndex < 0) {
            return new CodeParts(
                    normalized,
                    ""
            );
        }

        return new CodeParts(
                normalized.substring(
                        0,
                        separatorIndex
                ),
                normalized.substring(
                        separatorIndex + 1
                )
        );
    }

    private static final class CodeParts {

        private final String numeric;
        private final String suffix;

        private CodeParts(
                String numeric,
                String suffix
        ) {
            this.numeric = numeric;
            this.suffix = suffix;
        }
    }

    private static final class ScoredReference {

        private final WarehouseReference reference;
        private final int score;

        private ScoredReference(
                WarehouseReference reference,
                int score
        ) {
            this.reference = reference;
            this.score = score;
        }

        private int getScore() {
            return score;
        }
    }
}
