package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class WarehouseReferenceReviewRegressionTest {

    private final WarehouseReferenceParser parser =
            new WarehouseReferenceParser();
    private final WarehouseReferenceSuggestionResolver resolver =
            new WarehouseReferenceSuggestionResolver();
    private final DocumentReferenceDataParser documentParser =
            new DocumentReferenceDataParser();

    @Test
    public void reviewSuggestsReportedAlphabeticOcrConfusions() {
        assertSuggested("MR21S70", reference("MR", "21570"));
        assertSuggested("MK866S", reference("MR", "8665"));
        assertSuggested("MR2Ol06", reference("MR", "20106"));
        assertSuggested("mR2087o", reference("MR", "20870"));
        assertSuggested("mA20l", reference("MA", "1201"));
        assertSuggested("MR22S48", reference("MR", "22548"));
        assertSuggested("MR22S47", reference("MR", "22547"));
        assertSuggested("mA8To", reference("MA", "870"));
    }

    @Test
    public void reviewSuggestsParenthesesChineseGlyphAndMissingDigits() {
        assertSuggested("MR20(72", reference("MR", "20172"));
        assertSuggested("MR2(383", reference("MR", "21383"));
        assertSuggested("mA98王", reference("MA", "987"));
        assertSuggested("MR2854", reference("MR", "21854"));
        assertSuggested("mA87", reference("MA", "871"));

        assertSuggestions(
                "MR2214)",
                Arrays.asList(
                        reference("MR", "22141"),
                        reference("MR", "22147")
                )
        );
        assertSuggestions(
                "mR2087(",
                Arrays.asList(
                        reference("MR", "20871"),
                        reference("MR", "20877")
                )
        );
    }

    @Test
    public void reviewSuggestsCategoryAndNumericDigitConfusions() {
        assertSuggested("M221570", reference("MR", "21570"));
        assertSuggested("Mn21S7I", reference("MR", "21571"));
        assertSuggested("M55008", reference("MS", "5008"));
        assertSuggested("ML3923", reference("ML", "3723"));
        assertSuggested("m0803", reference("MD", "803"));
        assertSuggested("m0800", reference("MD", "801"));
        assertSuggested("MR22326", reference("MR", "22320"));
    }

    @Test
    public void parserKeepsConfusableLettersInsideNumericCode() {
        WarehouseReferenceMatch suffixS = parse("MK866S");
        WarehouseReferenceMatch suffixO = parse("mR2087o");

        assertEquals("866S", suffixS.getReference().getCode());
        assertEquals("2087O", suffixO.getReference().getCode());
    }

    @Test
    public void parserCorrectsOnlyLowercaseNInsideCategory() {
        WarehouseReferenceMatch lowercase = parse("Mn21S7I");
        WarehouseReferenceMatch uppercase = parse("MN21S7I");

        assertEquals(
                "MR",
                lowercase.getObservedReference().getCategory()
        );
        assertEquals(
                "MN",
                uppercase.getObservedReference().getCategory()
        );
        assertTrue(
                resolver.resolve(
                        uppercase.getObservedReference(),
                        Collections.singletonList(
                                reference("MR", "21571")
                        ),
                        5
                ).isEmpty()
        );
    }

    @Test
    public void crossedMarkerSeparatesReferenceFromQuantityAndUnit() {
        assertCrossedLine(
                "mR21388x40p",
                reference("MR", "21388"),
                40
        );
        assertCrossedLine(
                "mR2138fX4p",
                reference("MR", "21387"),
                4
        );
    }

    @Test
    public void crossedMarkerDoesNotBecomePartOfUnknownReferenceCode() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "mR2138fX4p",
                        Collections.emptyList()
                );

        assertEquals(1, matches.size());

        WarehouseReferenceMatch match =
                matches.get(0);

        assertEquals(
                "MR",
                match.getObservedReference().getCategory()
        );

        assertEquals(
                "2138F",
                match.getObservedReference().getCode()
        );

        DocumentReferenceData data =
                documentParser.parse(match);

        assertEquals(
                Integer.valueOf(4),
                data.getQuantity()
        );

        assertEquals(
                "P",
                data.getUnit()
        );

        assertEquals(
                "mR2138fX4p",
                data.getSourceText()
        );
    }

    @Test
    public void crossedMarkerNeverBecomesCodeEvenWithoutDocumentData() {
        WarehouseReferenceMatch match =
                singleMatch(
                        parser.parseOcrLine(
                                0,
                                "mR2138fX",
                                Collections.emptyList()
                        )
                );

        assertEquals(
                "MR",
                match.getObservedReference()
                        .getCategory()
        );
        assertEquals(
                "2138F",
                match.getObservedReference()
                        .getCode()
        );
        assertEquals(
                "mR2138fX",
                match.getSourceRawText()
        );
    }

    @Test
    public void xInsideCategoryIsNotTreatedAsCrossedMarker() {
        WarehouseReferenceMatch suffixMatch =
                singleMatch(
                        parser.parseOcrLine(
                                0,
                                "MX21387",
                                Collections.emptyList()
                        )
                );

        assertEquals(
                "MX",
                suffixMatch.getObservedReference()
                        .getCategory()
        );
        assertEquals(
                "21387",
                suffixMatch.getObservedReference()
                        .getCode()
        );

        WarehouseReferenceMatch prefixMatch =
                singleMatch(
                        parser.parseOcrLine(
                                1,
                                "XM21387",
                                Collections.emptyList()
                        )
                );

        assertEquals(
                "XM",
                prefixMatch.getObservedReference()
                        .getCategory()
        );
        assertEquals(
                "21387",
                prefixMatch.getObservedReference()
                        .getCode()
        );
    }

    @Test
    public void crossedMarkerIsRemovedBeforeUnknownOcrReferenceExtraction() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "mR2138fX4p",
                        Collections.emptyList()
                );

        assertEquals(1, matches.size());

        WarehouseReferenceMatch match =
                matches.get(0);

        assertEquals(
                "MR",
                match.getObservedReference()
                        .getCategory()
        );

        assertEquals(
                "2138F",
                match.getObservedReference()
                        .getCode()
        );

        DocumentReferenceData data =
                documentParser.parse(match);

        assertEquals(
                Integer.valueOf(4),
                data.getQuantity()
        );

        assertEquals(
                "P",
                data.getUnit()
        );
    }

    @Test
    public void crossedMarkerProducesEquivalentSegmentationForExactAndConfusedCodes() {
        WarehouseReferenceMatch exactMatch =
                singleMatch(
                        parser.parseOcrLine(
                                0,
                                "mR21388X40p",
                                Collections.emptyList()
                        )
                );

        WarehouseReferenceMatch confusedMatch =
                singleMatch(
                        parser.parseOcrLine(
                                1,
                                "mR2138fX4p",
                                Collections.emptyList()
                        )
                );

        assertEquals(
                "21388",
                exactMatch.getObservedReference()
                        .getCode()
        );

        assertEquals(
                "2138F",
                confusedMatch.getObservedReference()
                        .getCode()
        );

        assertFalse(
                exactMatch.getObservedReference()
                        .getCode()
                        .contains("X")
        );

        assertFalse(
                confusedMatch.getObservedReference()
                        .getCode()
                        .contains("X")
        );

        assertEquals(
                Integer.valueOf(40),
                documentParser.parse(exactMatch)
                        .getQuantity()
        );

        assertEquals(
                Integer.valueOf(4),
                documentParser.parse(confusedMatch)
                        .getQuantity()
        );
    }

    @Test
    public void crossedConfusedCodeRemainsObservedAndSuggestsKnownReference() {
        WarehouseReference expected =
                reference(
                        "MR",
                        "21387"
                );

        WarehouseReferenceMatch match =
                singleMatch(
                        parser.parseOcrLine(
                                0,
                                "mR2138fX4p",
                                Collections.singletonList(
                                        expected
                                )
                        )
                );

        assertEquals(
                "2138F",
                match.getObservedReference()
                        .getCode()
        );

        List<WarehouseReferenceSuggestion> suggestions =
                resolver.resolve(
                        match.getObservedReference(),
                        Collections.singletonList(
                                expected
                        ),
                        5
                );

        assertFalse(
                suggestions.isEmpty()
        );

        assertEquals(
                expected,
                suggestions.get(0)
                        .getReference()
        );

        DocumentReferenceData data =
                documentParser.parse(match);

        assertEquals(
                Integer.valueOf(4),
                data.getQuantity()
        );

        assertEquals(
                "P",
                data.getUnit()
        );
    }

    private void assertCrossedLine(
            String source,
            WarehouseReference expected,
            int expectedQuantity
    ) {
        WarehouseReferenceMatch match = parse(source);
        List<WarehouseReferenceSuggestion> suggestions =
                resolver.resolve(
                        match.getObservedReference(),
                        Collections.singletonList(expected),
                        5
                );

        if (!match.getObservedReference().equals(expected)) {
            assertFalse(suggestions.isEmpty());
            assertEquals(expected, suggestions.get(0).getReference());
        }

        DocumentReferenceData data = documentParser.parse(match);
        assertEquals(
                Integer.valueOf(expectedQuantity),
                data.getQuantity()
        );
        assertEquals("P", data.getUnit());
    }

    private void assertSuggested(
            String source,
            WarehouseReference expected
    ) {
        assertSuggestions(
                source,
                Collections.singletonList(expected)
        );
    }

    private void assertSuggestions(
            String source,
            List<WarehouseReference> expected
    ) {
        WarehouseReferenceMatch match = parse(source);
        List<WarehouseReferenceSuggestion> suggestions =
                resolver.resolve(
                        match.getObservedReference(),
                        expected,
                        5
                );

        assertEquals(expected.size(), suggestions.size());

        for (WarehouseReference value : expected) {
            assertTrue(
                    suggestions.stream().anyMatch(
                            suggestion -> suggestion
                                    .getReference()
                                    .equals(value)
                    )
            );
        }
    }

    private WarehouseReferenceMatch parse(String source) {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        source,
                        Collections.emptyList()
                );

        assertEquals(
                "The OCR candidate must reach review: " + source,
                1,
                matches.size()
        );

        return matches.get(0);
    }

    private WarehouseReference reference(
            String category,
            String code
    ) {
        return new WarehouseReference(category, code);
    }

    private WarehouseReferenceMatch singleMatch(
            List<WarehouseReferenceMatch> matches
    ) {
        assertEquals(1, matches.size());
        return matches.get(0);
    }
}
