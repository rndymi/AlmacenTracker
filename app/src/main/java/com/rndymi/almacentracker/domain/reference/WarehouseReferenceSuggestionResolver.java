package com.rndymi.almacentracker.domain.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class WarehouseReferenceSuggestionResolver {

    private static final int MAXIMUM_SCORE = 3;

    public List<WarehouseReferenceSuggestion> resolve(
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

        List<WarehouseReferenceSuggestion> result =
                new ArrayList<>();

        for (WarehouseReference known : knownReferences) {
            if (known == null
                    || known.equals(observed)) {
                continue;
            }

            CandidateScore score =
                    score(
                            observed,
                            known
                    );

            if (score == null
                    || score.value > MAXIMUM_SCORE) {
                continue;
            }

            result.add(
                    new WarehouseReferenceSuggestion(
                            known,
                            score.value,
                            score.explanation
                    )
            );
        }

        result.sort(
                Comparator
                        .comparingInt(
                                WarehouseReferenceSuggestion::getScore
                        )
                        .thenComparing(
                                value -> value
                                        .getReference()
                                        .displayValue()
                        )
        );

        if (result.size() > maximumSuggestions) {
            result =
                    new ArrayList<>(
                            result.subList(
                                    0,
                                    maximumSuggestions
                            )
                    );
        }

        return result.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(result);
    }

    private CandidateScore score(
            WarehouseReference observed,
            WarehouseReference known
    ) {
        String observedCategory =
                compact(
                        observed.getCategory()
                );

        String knownCategory =
                compact(
                        known.getCategory()
                );

        String observedCode =
                compact(
                        observed.getCode()
                );

        String knownCode =
                compact(
                        known.getCode()
                );

        CharacterComparison categoryComparison =
                compareCategory(
                        observedCategory,
                        knownCategory
                );

        if (categoryComparison == null) {
            return null;
        }

        CharacterComparison codeComparison =
                compareCode(
                        observedCode,
                        knownCode
                );

        if (codeComparison == null) {
            return null;
        }

        int totalScore =
                categoryComparison.score
                        + codeComparison.score;

        String explanation =
                joinExplanation(
                        categoryComparison.explanation,
                        codeComparison.explanation
                );

        return new CandidateScore(
                totalScore,
                explanation
        );
    }

    private CharacterComparison compareCategory(
            String observed,
            String expected
    ) {
        if (observed.equals(expected)) {
            return CharacterComparison.exact();
        }

        if (observed.length()
                != expected.length()) {
            return null;
        }

        int score = 0;
        List<String> changes = new ArrayList<>();

        for (int index = 0;
             index < observed.length();
             index++) {

            char source =
                    observed.charAt(index);

            char target =
                    expected.charAt(index);

            if (source == target) {
                continue;
            }

            if (isCategoryConfusion(
                    source,
                    target
            )) {
                score++;

                changes.add(
                        source
                                + " → "
                                + target
                                + " en categoría"
                );

                continue;
            }

            return null;
        }

        return new CharacterComparison(
                score,
                String.join(
                        ", ",
                        changes
                )
        );
    }

    private CharacterComparison compareCode(
            String observed,
            String expected
    ) {
        if (observed.equals(expected)) {
            return CharacterComparison.exact();
        }

        int observedIndex = 0;
        int expectedIndex = 0;
        int score = 0;

        List<String> changes = new ArrayList<>();

        while (observedIndex < observed.length()
                && expectedIndex < expected.length()) {

            char source =
                    observed.charAt(
                            observedIndex
                    );

            char target =
                    expected.charAt(
                            expectedIndex
                    );

            if (source == target) {
                observedIndex++;
                expectedIndex++;
                continue;
            }

            if (isDigitConfusion(
                    source,
                    target
            )) {
                score++;

                changes.add(
                        source
                                + " → "
                                + target
                                + " en código"
                );

                observedIndex++;
                expectedIndex++;
                continue;
            }

            if (source == 'H'
                    && expectedIndex + 1
                    < expected.length()) {

                String expectedPair =
                        expected.substring(
                                expectedIndex,
                                expectedIndex + 2
                        );

                if ("11".equals(expectedPair)
                        || "71".equals(expectedPair)) {
                    score++;

                    changes.add(
                            "H → "
                                    + expectedPair
                                    + " en código"
                    );

                    observedIndex++;
                    expectedIndex += 2;
                    continue;
                }
            }

            if (expectedIndex + 1
                    < expected.length()
                    && source
                    == expected.charAt(
                    expectedIndex + 1
            )) {
                score++;

                changes.add(
                        "dígito "
                                + target
                                + " omitido"
                );

                expectedIndex++;
                continue;
            }

            return null;
        }

        while (expectedIndex
                < expected.length()) {
            char missing =
                    expected.charAt(
                            expectedIndex
                    );

            if (!Character.isDigit(missing)) {
                return null;
            }

            score++;

            changes.add(
                    "dígito "
                            + missing
                            + " omitido"
            );

            expectedIndex++;
        }

        if (observedIndex
                < observed.length()) {
            return null;
        }

        return new CharacterComparison(
                score,
                String.join(
                        ", ",
                        changes
                )
        );
    }

    private boolean isCategoryConfusion(
            char observed,
            char expected
    ) {
        return observed == 'K'
                && expected == 'R'
                || observed == 'R'
                && expected == 'K'
                || observed == 'O'
                && expected == 'D'
                || observed == 'D'
                && expected == 'O';
    }

    private boolean isDigitConfusion(
            char observed,
            char expected
    ) {
        if (!Character.isDigit(expected)) {
            return false;
        }

        if (observed == 'O'
                && expected == '0') {
            return true;
        }

        if ((observed == '('
                || observed == ')')
                && expected == '1') {
            return true;
        }

        if (observed == '王'
                && expected == '7') {
            return true;
        }

        if (observed == 'T'
                && (expected == '1'
                || expected == '7')) {
            return true;
        }

        return observed == 'S'
                && expected == '5';
    }

    private String compact(String value) {
        return value == null
                ? ""
                : value
                .toUpperCase(Locale.ROOT)
                .replaceAll(
                        "[\\p{Z}\\s:;,.]+",
                        ""
                );
    }

    private String joinExplanation(
            String categoryExplanation,
            String codeExplanation
    ) {
        if (categoryExplanation.isEmpty()) {
            return codeExplanation;
        }

        if (codeExplanation.isEmpty()) {
            return categoryExplanation;
        }

        return categoryExplanation
                + ", "
                + codeExplanation;
    }

    private static final class CandidateScore {

        private final int value;
        private final String explanation;

        private CandidateScore(
                int value,
                String explanation
        ) {
            this.value = value;
            this.explanation = explanation;
        }
    }

    private static final class CharacterComparison {

        private final int score;
        private final String explanation;

        private CharacterComparison(
                int score,
                String explanation
        ) {
            this.score = score;
            this.explanation = explanation;
        }

        private static CharacterComparison exact() {
            return new CharacterComparison(
                    0,
                    ""
            );
        }
    }
}
