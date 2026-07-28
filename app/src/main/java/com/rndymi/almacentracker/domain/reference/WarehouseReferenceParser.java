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
                                    matcher.group(2)
                            ),
                            lineIndex,
                            rawText,
                            occurrenceIndex++
                    )
            );
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

        return numericPart
                + " "
                + normalizeSpaces(suffix);
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

        return codeScore < 0
                ? -1
                : categoryScore + codeScore;
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

            score += Character.isDigit(
                    observedCharacter
            ) ? 1 : 2;
        }

        return score;
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

        int differences = 0;
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

            differences++;

            if (differences > 3) {
                return -1;
            }

            score += Character.isLetter(
                    observedCharacter
            ) ? 1 : 3;
        }

        if (observedParts.suffix.equals(
                expectedParts.suffix
        )) {
            return score;
        }

        if (observedParts.suffix.isEmpty()
                || expectedParts.suffix.isEmpty()) {
            return score + 2;
        }

        return score
                + Math.min(
                4,
                levenshteinDistance(
                        observedParts.suffix,
                        expectedParts.suffix
                )
        );
    }

    private CodeParts codeParts(String code) {
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

    private int levenshteinDistance(
            String left,
            String right
    ) {
        int[] previous =
                new int[right.length() + 1];
        int[] current =
                new int[right.length() + 1];

        for (int column = 0;
             column <= right.length();
             column++) {
            previous[column] = column;
        }

        for (int row = 1;
             row <= left.length();
             row++) {
            current[0] = row;

            for (int column = 1;
                 column <= right.length();
                 column++) {
                int substitutionCost =
                        left.charAt(row - 1)
                                == right.charAt(column - 1)
                                ? 0
                                : 1;

                current[column] =
                        Math.min(
                                Math.min(
                                        current[column - 1] + 1,
                                        previous[column] + 1
                                ),
                                previous[column - 1]
                                        + substitutionCost
                        );
            }

            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[right.length()];
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
