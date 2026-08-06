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
                            + "([0-9]{3,5}(?:-[0-9]{1,3})?)"
                            + "(?:\\s*"
                            + "(\\p{L}+(?:\\s+\\p{L}+)*))?"
                            + "(?![A-Z0-9])"
            );
    private static final Pattern OCR_EXTRACTION_PATTERN =
            Pattern.compile(
                    "(?<![A-Z0-9])"
                            + "(?:X[\\p{Z}\\s]*)?"
                            + "([A-Z0-9()]{2})"
                            + "[\\p{Z}\\s:._-]*"
                            + "([A-Z0-9()王]{2,})"
                            + "(?:[\\p{Z}\\s]+"
                            + "(\\p{L}+(?:[\\p{Z}\\s]+"
                            + "\\p{L}+)*))?"
                            + "(?![A-Z0-9()王])"
            );
    private static final Pattern CATEGORY_PATTERN =
            Pattern.compile(
                    "^[A-Z]{1,2}$"
            );
    private static final Pattern CODE_PATTERN =
            Pattern.compile(
                    "^[0-9]{3,5}(?:-[0-9]{1,3})?"
                            + "(?: \\p{L}+)*$"
            );
    private static final Pattern SPECIAL_REFERENCE_PATTERN =
            Pattern.compile(
                    "(?<![\\p{L}0-9])"
                            + "([A-Z]{1,2})"
                            + "[\\p{Z}\\s]*"
                            + "([0-9]{3,5})"
                            + "[\\p{Z}\\s]*"
                            + "[-\\u2010-\\u2014]"
                            + "[\\p{Z}\\s]*"
                            + "([0-9]{1,3}|[①-⑳㉑-㉟㊱-㊿])"
                            + "(?=[\\p{Z}\\s]*"
                            + "(?:[-\\u2010-\\u2014]"
                            + "[\\p{Z}\\s]*"
                            + "[0-9ILSZGJBOTF()王]{0,3}"
                            + "[\\p{Z}\\s]*"
                            + "P(?:CS|QT|QTS)?(?:\\b|$)"
                            + "|$))",
                    Pattern.CASE_INSENSITIVE
                            | Pattern.UNICODE_CASE
            );
    private static final Pattern UNICODE_SPACES =
            Pattern.compile(
                    "[\\p{Z}\\s]+"
            );
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
    private static final Pattern OCR_SPACED_CATEGORY_PATTERN =
            Pattern.compile(
                    "(?<![A-Z0-9])"
                            + "([A-Z0-9])"
                            + "[\\p{Z}\\s]+"
                            + "([A-Z0-9])"
                            + "(?=[\\p{Z}\\s:._-]+"
                            + "[A-Z0-9]{3,})"
            );
    private static final Pattern OCR_SPACED_CODE_PATTERN =
            Pattern.compile(
                    "(?<![A-Z0-9])"
                            + "([A-Z0-9]{2})"
                            + "[\\p{Z}\\s:._-]+"
                            + "((?:[A-Z0-9][\\p{Z}\\s]+){2,4}"
                            + "[A-Z0-9])"
                            + "(?![A-Z0-9])"
            );
    private static final Pattern OCR_RAW_CATEGORY_PATTERN =
            Pattern.compile(
                    "(?<![\\p{L}0-9])"
                            + "(X[\\p{Z}\\s]*)?"
                            + "([\\p{L}0-9()])"
                            + "([\\p{Z}\\s]*)"
                            + "([\\p{L}0-9()])"
                            + "(?=[\\p{Z}\\s:._-]*"
                            + "[\\p{L}0-9()王]{2,})"
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
                normalizeSpaces(
                        referenceSegment(rawText)
                ).toUpperCase(Locale.ROOT);

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

    public List<WarehouseReferenceMatch> parseOcrLine(
            int lineIndex,
            String rawText,
            List<WarehouseReference> knownReferences
    ) {
        if (lineIndex < 0
                || rawText == null) {
            return Collections.emptyList();
        }

        String normalizedOcrText =
                normalizeOcrReferenceSpacing(
                        rawText
                );

        List<WarehouseReferenceMatch> specialMatches =
                extractSpecialReferenceCandidates(
                        lineIndex,
                        normalizedOcrText,
                        rawText
                );

        if (!specialMatches.isEmpty()) {
            return specialMatches;
        }

        WarehouseReferenceMatch shiftedCandidate =
                extractKnownSupportedSingleLetterCandidate(
                        lineIndex,
                        normalizedOcrText,
                        rawText,
                        knownReferences
                );

        if (shiftedCandidate != null) {
            return Collections.singletonList(
                    shiftedCandidate
            );
        }

        List<WarehouseReferenceMatch> knownMatches =
                findKnownReferencesInLine(
                        lineIndex,
                        normalizedOcrText,
                        rawText,
                        knownReferences
                );

        if (!knownMatches.isEmpty()) {
            return knownMatches;
        }

        List<WarehouseReferenceMatch> strictMatches =
                parseLine(
                        lineIndex,
                        normalizedOcrText
                );

        if (!strictMatches.isEmpty()) {
            return restoreOriginalSource(
                    strictMatches,
                    rawText
            );
        }

        return extractOcrCandidates(
                lineIndex,
                normalizedOcrText,
                rawText
        );
    }

    private WarehouseReferenceMatch
    extractKnownSupportedSingleLetterCandidate(
            int lineIndex,
            String searchableSource,
            String originalSource,
            List<WarehouseReference> knownReferences
    ) {
        if (knownReferences == null
                || knownReferences.isEmpty()) {
            return null;
        }

        Matcher matcher = Pattern.compile(
                "(?<![\\p{L}0-9])([A-Z])"
                        + "[\\p{Z}\\s]*([0-9]{3,5})"
                        + "(?=[\\p{Z}\\s]*"
                        + "(?:[-\\u2010-\\u2014]|$))",
                Pattern.CASE_INSENSITIVE
        ).matcher(searchableSource);

        if (!matcher.find()) {
            return null;
        }

        String observedCategory = matcher.group(1)
                .toUpperCase(Locale.ROOT);
        String observedCode = matcher.group(2);

        for (WarehouseReference known : knownReferences) {
            WarehouseReference normalizedKnown =
                    normalizeKnownReference(known);

            if (normalizedKnown != null
                    && normalizedKnown.getCode()
                    .equals(observedCode)
                    && normalizedKnown.getCategory()
                    .startsWith(observedCategory)) {
                return new WarehouseReferenceMatch(
                        new WarehouseReference(
                                observedCategory,
                                observedCode
                        ),
                        lineIndex,
                        originalSource,
                        0
                );
            }
        }

        return null;
    }

    private List<WarehouseReferenceMatch>
    extractSpecialReferenceCandidates(
            int lineIndex,
            String searchableSource,
            String originalSource
    ) {
        Matcher matcher =
                SPECIAL_REFERENCE_PATTERN.matcher(
                        searchableSource
                );

        List<WarehouseReferenceMatch> matches =
                new ArrayList<>();

        int occurrenceIndex = 0;

        while (matcher.find()) {
            String category = matcher.group(1)
                    .toUpperCase(Locale.ROOT);
            String extension = matcher.group(3);
            WarehouseReference observed =
                    new WarehouseReference(
                            category,
                            matcher.group(2) + "-" + extension
                    );
            String normalizedExtension =
                    extension.length() == 1
                            ? CircledNumberNormalizer.digitsFor(
                            extension.charAt(0)
                    )
                            : null;

            matches.add(
                    normalizedExtension == null
                            ? new WarehouseReferenceMatch(
                            observed,
                            lineIndex,
                            originalSource,
                            occurrenceIndex++
                    )
                            : new WarehouseReferenceMatch(
                            observed,
                            new WarehouseReference(
                                    category,
                                    matcher.group(2)
                                            + "-"
                                            + normalizedExtension
                            ),
                            lineIndex,
                            originalSource,
                            occurrenceIndex++
                    )
            );
        }

        return matches.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(matches);
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

    public List<WarehouseReference> suggestZeroSixAlternatives(
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

        List<WarehouseReference> suggestions = new ArrayList<>();

        for (WarehouseReference known : knownReferences) {
            if (known == null) {
                continue;
            }

            WarehouseReference normalizedKnown =
                    parseInput(
                            known.getCategory(),
                            known.getCode()
                    );

            if (normalizedKnown != null
                    && isZeroSixAlternative(
                    observed,
                    normalizedKnown
            )
                    && !suggestions.contains(normalizedKnown)) {
                suggestions.add(normalizedKnown);
            }
        }

        suggestions.sort(
                Comparator.comparing(
                        WarehouseReference::displayValue
                )
        );

        if (suggestions.size() > maximumSuggestions) {
            suggestions = new ArrayList<>(
                    suggestions.subList(0, maximumSuggestions)
            );
        }

        return suggestions.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(suggestions);
    }

    private List<WarehouseReferenceMatch> extractOcrCandidates(
            int lineIndex,
            String searchableSource,
            String originalSource
    ) {
        String searchableText =
                normalizeSpaces(
                        referenceSegment(
                                searchableSource
                        )
                ).toUpperCase(Locale.ROOT);

        Matcher matcher =
                OCR_EXTRACTION_PATTERN.matcher(
                        searchableText
                );

        List<WarehouseReferenceMatch> candidates =
                new ArrayList<>();

        int occurrenceIndex = 0;

        while (matcher.find()) {
            String observedCategory =
                    matcher.group(1);

            String observedCode =
                    matcher.group(2);

            String observedSuffix =
                    matcher.group(3);

            if (!hasMinimumNumericContent(
                    observedCode
            )) {
                continue;
            }

            candidates.add(
                    new WarehouseReferenceMatch(
                            new WarehouseReference(
                                    observedCategory,
                                    canonicalOcrCode(
                                            observedCode,
                                            observedSuffix
                                    )
                            ),
                            lineIndex,
                            originalSource,
                            occurrenceIndex++
                    )
            );
        }

        return candidates.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(
                candidates
        );
    }

    private List<WarehouseReferenceMatch>
    resolveInvalidCategories(
            List<WarehouseReferenceMatch> observedMatches,
            List<WarehouseReference> knownReferences
    ) {
        if (observedMatches == null
                || observedMatches.isEmpty()) {
            return Collections.emptyList();
        }

        List<WarehouseReferenceMatch> resolved =
                new ArrayList<>(
                        observedMatches.size()
                );

        for (WarehouseReferenceMatch match
                : observedMatches) {

            if (match == null) {
                continue;
            }

            WarehouseReference observed =
                    match.getReference();

            if (isValidCategory(
                    observed.getCategory()
            )) {
                resolved.add(match);
                continue;
            }

            WarehouseReference uniqueCandidate =
                    findUniqueCompatibleReference(
                            observed,
                            knownReferences
                    );

            if (uniqueCandidate == null) {
                continue;
            }

            resolved.add(
                    new WarehouseReferenceMatch(
                            uniqueCandidate,
                            match.getSourceLineIndex(),
                            match.getSourceRawText(),
                            match.getOccurrenceIndex()
                    )
            );
        }

        return resolved.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(
                resolved
        );
    }

    private WarehouseReference
    findUniqueCompatibleReference(
            WarehouseReference observed,
            List<WarehouseReference> knownReferences
    ) {
        if (observed == null
                || knownReferences == null
                || knownReferences.isEmpty()) {
            return null;
        }

        WarehouseReference uniqueCandidate = null;
        int bestScore = Integer.MAX_VALUE;

        for (WarehouseReference known
                : knownReferences) {

            if (known == null) {
                continue;
            }

            WarehouseReference normalizedKnown =
                    parseInput(
                            known.getCategory(),
                            known.getCode()
                    );

            if (normalizedKnown == null) {
                continue;
            }

            int score =
                    suggestionScore(
                            observed,
                            normalizedKnown
                    );

            if (score < 0) {
                continue;
            }

            if (score < bestScore) {
                uniqueCandidate =
                        normalizedKnown;

                bestScore = score;
                continue;
            }

            if (score == bestScore
                    && uniqueCandidate != null
                    && !uniqueCandidate.equals(
                    normalizedKnown
            )) {
                uniqueCandidate = null;
            }
        }

        return uniqueCandidate;
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

        if (normalizedSuffix.length() == 1
                && numericPart.length() >= 2
                && numericPart.length() < 5
                && isDigitConfusableSuffix(
                normalizedSuffix.charAt(0)
        )) {
            return numericPart + normalizedSuffix;
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
        String normalizedObservedCode =
                observedCode == null
                        ? ""
                        : observedCode
                        .toUpperCase(Locale.ROOT);

        String normalizedSeparatedSuffix =
                separatedSuffix == null
                        ? ""
                        : normalizeSpaces(
                        separatedSuffix
                ).toUpperCase(Locale.ROOT);

        int numericEnd = 0;

        while (numericEnd
                < normalizedObservedCode.length()
                && numericEnd < 5
                && Character.isDigit(
                normalizedObservedCode
                        .charAt(numericEnd)
        )) {
            numericEnd++;
        }

        if (numericEnd >= 3
                && numericEnd
                < normalizedObservedCode.length()) {

            String attachedQualifier =
                    normalizedObservedCode.substring(
                            numericEnd
                    );

            boolean alphabeticQualifier =
                    attachedQualifier
                            .codePoints()
                            .allMatch(
                                    Character::isLetter
                            );

            if (alphabeticQualifier
                    && (attachedQualifier.length() > 1
                    || !isDigitConfusableSuffix(
                    attachedQualifier.charAt(0)
            ))) {
                normalizedObservedCode =
                        normalizedObservedCode.substring(
                                0,
                                numericEnd
                        );

                normalizedSeparatedSuffix =
                        normalizedSeparatedSuffix.isEmpty()
                                ? attachedQualifier
                                : attachedQualifier
                                  + " "
                                  + normalizedSeparatedSuffix;
            }
        }

        return canonicalCode(
                normalizedObservedCode,
                normalizedSeparatedSuffix
        );
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
                && expected == 'R')
                || (observed == 'N'
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

        if (observed == 'J'
                && expected == '7') {
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
                && expected == '7')
                || (observed == '0'
                && expected == '6')
                || (observed == '6'
                && expected == '0')) {
            return 3;
        }


        return -1;
    }

    private boolean isZeroSixAlternative(
            WarehouseReference observed,
            WarehouseReference expected
    ) {
        if (!observed.getCategory().equals(expected.getCategory())) {
            return false;
        }

        CodeParts observedParts = codeParts(observed.getCode());
        CodeParts expectedParts = codeParts(expected.getCode());

        if (!normalizeOcrSuffix(observedParts.suffix)
                .equals(normalizeOcrSuffix(expectedParts.suffix))
                || observedParts.numeric.length()
                != expectedParts.numeric.length()) {
            return false;
        }

        boolean differs = false;

        for (int index = 0;
             index < observedParts.numeric.length();
             index++) {
            char observedCharacter =
                    observedParts.numeric.charAt(index);
            char expectedCharacter =
                    expectedParts.numeric.charAt(index);

            if (observedCharacter == expectedCharacter) {
                continue;
            }

            boolean zeroSixPair =
                    (observedCharacter == '0'
                            && expectedCharacter == '6')
                            || (observedCharacter == '6'
                            && expectedCharacter == '0');

            if (!zeroSixPair) {
                return false;
            }

            differs = true;
        }

        return differs;
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
        return suffix == null
                ? ""
                : normalizeSpaces(suffix)
                .toUpperCase(Locale.ROOT);
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

        String numeric =
                normalized.substring(
                        0,
                        separatorIndex
                );

        String suffix =
                normalized.substring(
                        separatorIndex + 1
                );

        if (suffix.length() == 1
                && numeric.length() >= 2
                && numeric.length() < 5
                && isDigitConfusableSuffix(
                suffix.charAt(0)
        )) {
            return new CodeParts(
                    numeric + suffix,
                    ""
            );
        }

        return new CodeParts(
                numeric,
                suffix
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

    private static final class KnownReferenceCandidate {

        private final WarehouseReference reference;
        private final String compactIdentity;

        private KnownReferenceCandidate(
                WarehouseReference reference,
                String compactIdentity
        ) {
            this.reference = reference;
            this.compactIdentity = compactIdentity;
        }
    }

    private boolean hasMinimumNumericContent(
            String observedCode
    ) {
        if (observedCode == null) {
            return false;
        }

        int digitCount = 0;

        for (int index = 0;
             index < observedCode.length();
             index++) {

            if (Character.isDigit(
                    observedCode.charAt(index)
            )) {
                digitCount++;
            }
        }

        return digitCount >= 1;
    }

    private boolean isDigitConfusableSuffix(
            char value
    ) {
        return value == 'I'
                || value == 'L'
                || value == 'O'
                || value == 'S'
                || value == 'Z'
                || value == 'G'
                || value == 'J'
                || value == 'B'
                || value == 'T'
                || value == 'F';
    }

    private String referenceSegment(
            String sourceText
    ) {
        Matcher matcher =
                DOCUMENT_DATA_DELIMITER_PATTERN
                        .matcher(sourceText);

        if (!matcher.find()) {
            return sourceText;
        }

        return sourceText.substring(
                0,
                matcher.start()
        );
    }

    private String normalizeOcrReferenceSpacing(
            String rawText
    ) {
        String normalized = normalizeSpaces(rawText);

        normalized = normalized.replaceAll(
                "(?i)(?<![\\p{L}0-9])"
                        + "([A-Z]{1,2})[\\p{Z}\\s]*"
                        + "([0-9]{1,2})[\\p{Z}\\s]*"
                        + "[-\\u2010-\\u2014][\\p{Z}\\s]*"
                        + "([0-9]{2,4})"
                        + "(?=[\\p{Z}\\s]*"
                        + "(?:[-\\u2010-\\u2014]|$))",
                "$1 $2$3"
        );

        Matcher rawCategoryMatcher =
                OCR_RAW_CATEGORY_PATTERN.matcher(
                        normalized
                );
        StringBuffer rawCategoryResult =
                new StringBuffer();

        while (rawCategoryMatcher.find()) {
            String marker =
                    rawCategoryMatcher.group(1) == null
                            ? ""
                            : rawCategoryMatcher.group(1);
            String first = rawCategoryMatcher.group(2);
            String separator = rawCategoryMatcher.group(3);
            String second = rawCategoryMatcher.group(4);

            if ("n".equals(first)) {
                first = "R";
            }

            if ("n".equals(second)) {
                second = "R";
            }

            rawCategoryMatcher.appendReplacement(
                    rawCategoryResult,
                    Matcher.quoteReplacement(
                            marker
                                    + first
                                    + separator
                                    + second
                    )
            );
        }

        rawCategoryMatcher.appendTail(
                rawCategoryResult
        );

        normalized = rawCategoryResult
                .toString()
                .toUpperCase(Locale.ROOT);

        Matcher categoryMatcher =
                OCR_SPACED_CATEGORY_PATTERN
                        .matcher(normalized);

        StringBuffer categoryResult =
                new StringBuffer();

        while (categoryMatcher.find()) {
            categoryMatcher.appendReplacement(
                    categoryResult,
                    Matcher.quoteReplacement(
                            categoryMatcher.group(1)
                                    + categoryMatcher.group(2)
                    )
            );
        }

        categoryMatcher.appendTail(
                categoryResult
        );

        Matcher codeMatcher =
                OCR_SPACED_CODE_PATTERN.matcher(
                        categoryResult.toString()
                );

        StringBuffer codeResult =
                new StringBuffer();

        while (codeMatcher.find()) {
            String compactCode =
                    codeMatcher.group(2)
                            .replaceAll(
                                    "[\\p{Z}\\s]+",
                                    ""
                            );

            codeMatcher.appendReplacement(
                    codeResult,
                    Matcher.quoteReplacement(
                            codeMatcher.group(1)
                                    + " "
                                    + compactCode
                    )
            );
        }

        codeMatcher.appendTail(codeResult);

        return normalizeSpaces(
                codeResult.toString()
        );
    }

    private List<WarehouseReferenceMatch>
    restoreOriginalSource(
            List<WarehouseReferenceMatch> matches,
            String originalSource
    ) {
        if (matches.isEmpty()) {
            return matches;
        }

        List<WarehouseReferenceMatch> restored =
                new ArrayList<>(matches.size());

        for (WarehouseReferenceMatch match : matches) {
            restored.add(
                    new WarehouseReferenceMatch(
                            match.getReference(),
                            match.getSourceLineIndex(),
                            originalSource,
                            match.getOccurrenceIndex()
                    )
            );
        }

        return Collections.unmodifiableList(
                restored
        );
    }

    private List<WarehouseReferenceMatch>
    findKnownReferencesInLine(
            int lineIndex,
            String normalizedOcrText,
            String originalSource,
            List<WarehouseReference> knownReferences
    ) {
        if (knownReferences == null
                || knownReferences.isEmpty()) {
            return Collections.emptyList();
        }

        String compactSource =
                compactForKnownReferenceSearch(
                        normalizedOcrText
                );

        List<KnownReferenceCandidate> matchingKnown =
                new ArrayList<>();

        for (WarehouseReference known : knownReferences) {
            if (known == null) {
                continue;
            }

            WarehouseReference normalizedKnown =
                    normalizeKnownReference(known);

            if (normalizedKnown == null) {
                continue;
            }

            String compactIdentity =
                    compactReferenceIdentity(
                            normalizedKnown
                    );

            if (!compactIdentity.isEmpty()
                    && compactSource.contains(
                    compactIdentity
            )
                    && !containsKnownCandidate(
                    matchingKnown,
                    normalizedKnown
            )) {
                matchingKnown.add(
                        new KnownReferenceCandidate(
                                normalizedKnown,
                                compactIdentity
                        )
                );
            }
        }

        matchingKnown.sort(
                Comparator
                        .comparingInt(
                                (KnownReferenceCandidate value) ->
                                        value.compactIdentity.length()
                        )
                        .reversed()
                        .thenComparing(
                                value -> value.reference.displayValue()
                        )
        );

        List<WarehouseReferenceMatch> result =
                new ArrayList<>();

        int occurrenceIndex = 0;

        for (KnownReferenceCandidate candidate
                : matchingKnown) {
            int start =
                    compactSource.indexOf(
                            candidate.compactIdentity
                    );

            if (start < 0) {
                continue;
            }

            result.add(
                    new WarehouseReferenceMatch(
                            candidate.reference,
                            lineIndex,
                            originalSource,
                            occurrenceIndex++
                    )
            );

            compactSource =
                    compactSource.substring(
                            0,
                            start
                    )
                            + repeatSpaces(
                            candidate.compactIdentity.length()
                    )
                            + compactSource.substring(
                            start
                                    + candidate.compactIdentity.length()
                    );
        }

        return result.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(result);
    }

    private boolean containsKnownCandidate(
            List<KnownReferenceCandidate> values,
            WarehouseReference reference
    ) {
        for (KnownReferenceCandidate value : values) {
            if (value.reference.equals(reference)) {
                return true;
            }
        }

        return false;
    }

    private WarehouseReference normalizeKnownReference(
            WarehouseReference value
    ) {
        if (value == null) {
            return null;
        }

        String category =
                normalizeCategory(
                        value.getCategory()
                );

        String code =
                normalizeCode(
                        value.getCode()
                );

        if (category.isEmpty()
                || code.isEmpty()) {
            return null;
        }

        return new WarehouseReference(
                category,
                code
        );
    }

    private String compactReferenceIdentity(
            WarehouseReference reference
    ) {
        return compactForKnownReferenceSearch(
                reference.getCategory()
                        + reference.getCode()
        );
    }

    private String compactForKnownReferenceSearch(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value
                .toUpperCase(Locale.ROOT)
                .replaceAll(
                        "[\\p{Z}\\s:;,.]+",
                        ""
                );
    }

    private String repeatSpaces(int length) {
        StringBuilder result =
                new StringBuilder(length);

        for (int index = 0;
             index < length;
             index++) {
            result.append(' ');
        }

        return result.toString();
    }
}
