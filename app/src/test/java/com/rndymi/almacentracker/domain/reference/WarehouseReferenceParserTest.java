package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class WarehouseReferenceParserTest {

    private WarehouseReferenceParser parser;

    @Before
    public void setUp() {
        parser =
                new WarehouseReferenceParser();
    }

    @Test
    public void parseLine_extractsCompactReference() {
        List<WarehouseReferenceMatch> matches =
                parser.parseLine(
                        0,
                        "MR1210"
                );

        assertEquals(
                1,
                matches.size()
        );

        assertEquals(
                "MR",
                matches.get(0)
                        .getReference()
                        .getCategory()
        );

        assertEquals(
                "1210",
                matches.get(0)
                        .getReference()
                        .getCode()
        );
    }

    @Test
    public void parseLine_extractsReferenceWithSpaces() {
        List<WarehouseReferenceMatch> matches =
                parser.parseLine(
                        0,
                        "MR 1210 A"
                );

        assertEquals(
                1,
                matches.size()
        );

        assertEquals(
                "1210 A",
                matches.get(0)
                        .getReference()
                        .getCode()
        );
    }

    @Test
    public void parseLine_preservesLeadingZeros() {
        List<WarehouseReferenceMatch> matches =
                parser.parseLine(
                        0,
                        "MR01210"
                );

        assertEquals(
                "01210",
                matches.get(0)
                        .getReference()
                        .getCode()
        );
    }

    @Test
    public void parseLine_extractsMultipleReferencesInOrder() {
        List<WarehouseReferenceMatch> matches =
                parser.parseLine(
                        0,
                        "MR1210A / MZ1300C"
                );

        assertEquals(
                2,
                matches.size()
        );

        assertEquals(
                "MR",
                matches.get(0)
                        .getReference()
                        .getCategory()
        );

        assertEquals(
                "MZ",
                matches.get(1)
                        .getReference()
                        .getCategory()
        );
    }

    @Test
    public void parseLine_ignoresSingleLetterPrefix() {
        assertTrue(
                parser.parseLine(
                        0,
                        "M1210"
                ).isEmpty()
        );
    }

    @Test
    public void parseLine_doesNotExtractPartialThreeLetterPrefix() {
        assertTrue(
                parser.parseLine(
                        0,
                        "MRA1210"
                ).isEmpty()
        );
    }

    @Test
    public void parseLine_ignoresReferenceEmbeddedInLargerValue() {
        assertTrue(
                parser.parseLine(
                        0,
                        "XMR1210AY"
                ).isEmpty()
        );
    }

    @Test
    public void parseLine_doesNotCorrectAmbiguousOcrCharacters() {
        assertTrue(
                parser.parseLine(
                        0,
                        "MR 1O50"
                ).isEmpty()
        );
    }

    @Test
    public void parseInput_normalizesCaseAndSpaces() {
        WarehouseReference reference =
                parser.parseInput(
                        " mr ",
                        " 01210 ab "
                );

        assertEquals(
                "MR",
                reference.getCategory()
        );

        assertEquals(
                "01210 AB",
                reference.getCode()
        );
    }

    @Test
    public void parseInput_acceptsFourOrFiveDigitsWithLetterSuffix() {
        assertTrue(
                parser.isValidCode("5008")
        );
        assertTrue(
                parser.isValidCode("21571")
        );
        assertTrue(
                parser.isValidCode("21571 ABC")
        );
    }

    @Test
    public void parseInputSeparatesAttachedSuffixAndSupportsWords() {
        assertEquals(
                "1010 YELLOW",
                parser.parseInput(
                        "md",
                        "1010Yellow"
                ).getCode()
        );

        assertEquals(
                "1010 DARK BLUE",
                parser.parseInput(
                        "MD",
                        "1010  dark blue"
                ).getCode()
        );

        assertEquals(
                "1010 MARRÓN",
                parser.parseInput(
                        "MD",
                        "1010marrón"
                ).getCode()
        );
    }

    @Test
    public void parseLineExtractsMultiWordSuffixWithCanonicalSpacing() {
        List<WarehouseReferenceMatch> matches =
                parser.parseLine(
                        0,
                        "MD 1010 Dark Blue"
                );

        assertEquals(1, matches.size());
        assertEquals(
                "1010 DARK BLUE",
                matches.get(0)
                        .getReference()
                        .getCode()
        );
    }

    @Test
    public void parseInput_rejectsCodesOutsideCompanyFormat() {
        assertFalse(
                parser.isValidCode("12")
        );
        assertFalse(
                parser.isValidCode("123456")
        );
        assertFalse(
                parser.isValidCode("12A45")
        );
    }

    @Test
    public void parseOcrLineResolvesInvalidCategoryAgainstKnownReference() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "M5 SOO8 - 3 pcs",
                        Collections.singletonList(
                                new WarehouseReference(
                                        "MS",
                                        "5008"
                                )
                        )
                );

        assertEquals(1, matches.size());

        assertEquals(
                "MS 5008",
                matches.get(0)
                        .getReference()
                        .displayValue()
        );

        assertTrue(
                parser.isValidCategory(
                        matches.get(0)
                                .getReference()
                                .getCategory()
                )
        );
    }

    @Test
    public void suggestReferencesTreatsLettersInNumericCodeAsUnknown() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "MR 215I1 - 1 pcs",
                        java.util.Arrays.asList(
                                new WarehouseReference(
                                        "MR",
                                        "21511"
                                ),
                                new WarehouseReference(
                                        "MR",
                                        "21571"
                                )
                        )
                );

        assertEquals(1, matches.size());
        assertEquals(
                "MR 215I1",
                matches.get(0)
                        .getReference()
                        .displayValue()
        );

        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        matches.get(0).getReference(),
                        java.util.Arrays.asList(
                                new WarehouseReference(
                                        "MR",
                                        "21511"
                                ),
                                new WarehouseReference(
                                        "MR",
                                        "21571"
                                )
                        ),
                        5
                );

        assertEquals(2, suggestions.size());
        assertEquals(
                "MR 21511",
                suggestions.get(0).displayValue()
        );
        assertEquals(
                "MR 21571",
                suggestions.get(1).displayValue()
        );
    }

    @Test
    public void suggestReferencesRejectsUnrelatedCategoryChanges() {
        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        new WarehouseReference(
                                "MP",
                                "21571"
                        ),
                        java.util.Collections.singletonList(
                                new WarehouseReference(
                                        "MR",
                                        "21571"
                                )
                        ),
                        5
                );

        assertTrue(suggestions.isEmpty());
    }

    @Test
    public void suggestReferencesIncludesCodeSuffixVariants() {
        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        new WarehouseReference(
                                "MD",
                                "1010"
                        ),
                        java.util.Arrays.asList(
                                new WarehouseReference(
                                        "MD",
                                        "1010 YELLOW"
                                ),
                                new WarehouseReference(
                                        "MD",
                                        "1010 BROWN"
                                )
                        ),
                        5
                );

        assertEquals(2, suggestions.size());
        assertEquals(
                "MD 1010 BROWN",
                suggestions.get(0).displayValue()
        );
        assertEquals(
                "MD 1010 YELLOW",
                suggestions.get(1).displayValue()
        );
    }

    @Test
    public void parseOcrLineDoesNotExposeNumericCategoryWithoutKnownReference() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "M2 215I1 - 1 pcs",
                        Collections.emptyList()
                );

        assertTrue(matches.isEmpty());
    }

    @Test
    public void parseOcrLineRejectsInvalidCategoryWithQualifierWithoutKnownReference() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "M2 215L1POS",
                        Collections.emptyList()
                );

        assertTrue(matches.isEmpty());
    }

    @Test
    public void parseOcrLineRejectsInvalidCategoryWithoutKnownReference() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "M5 SOO8POS",
                        Collections.emptyList()
                );

        assertTrue(matches.isEmpty());
    }

    @Test
    public void parseOcrLine_doesNotGloballyChangeNineToSeven() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "ML 3923 - 4 pcs",
                        java.util.Collections.emptyList()
                );

        assertEquals(1, matches.size());
        assertEquals(
                "ML 3923",
                matches.get(0)
                        .getReference()
                        .displayValue()
        );
    }

    @Test
    public void parseLineStopsAtHyphenBeforeQuantityDetails() {
        List<WarehouseReferenceMatch> matches =
                parser.parseLine(
                        0,
                        "MR 21570 - 5 pcs MS 5008"
                );

        assertEquals(1, matches.size());
        assertEquals(
                "MR 21570",
                matches.get(0)
                        .getReference()
                        .displayValue()
        );
    }

    @Test
    public void parseLinePreservesAlphabeticQualifierWithoutDocumentBoundary() {
        String[] qualifiers = {
                "PCS",
                "PQTS",
                "PZAS",
                "CAJAS",
                "PAQUETES"
        };

        for (String qualifier : qualifiers) {
            List<WarehouseReferenceMatch> matches =
                    parser.parseLine(
                            0,
                            "MR 21570 " + qualifier
                    );

            assertEquals(1, matches.size());

            assertEquals(
                    "MR 21570 " + qualifier,
                    matches.get(0)
                            .getReference()
                            .displayValue()
            );
        }
    }

    @Test
    public void parseOcrLinePreservesAttachedQualifierWhenQuantityIsMissing() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "MR21511pcs",
                        Collections.emptyList()
                );

        assertEquals(1, matches.size());

        assertEquals(
                "MR 21511 PCS",
                matches.get(0)
                        .getReference()
                        .displayValue()
        );
    }

    @Test
    public void parseInput_rejectsInvalidCategory() {
        assertNull(
                parser.parseInput(
                        "M",
                        "1210"
                )
        );

        assertFalse(
                parser.isValidCategory(
                        "M"
                )
        );
    }

    @Test
    public void parseInput_rejectsInvalidCode() {
        assertNull(
                parser.parseInput(
                        "MR",
                        "12AB"
                )
        );

        assertFalse(
                parser.isValidCode(
                        "12AB"
                )
        );
    }

    @Test
    public void suggestReferencesOnlyCorrectsCategoryWhenCodeIsReliable() {
        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        new WarehouseReference(
                                "ME",
                                "21570"
                        ),
                        java.util.Arrays.asList(
                                new WarehouseReference(
                                        "MR",
                                        "21570"
                                ),
                                new WarehouseReference(
                                        "MR",
                                        "21070"
                                ),
                                new WarehouseReference(
                                        "MR",
                                        "21370"
                                ),
                                new WarehouseReference(
                                        "MR",
                                        "21470"
                                ),
                                new WarehouseReference(
                                        "MR",
                                        "21510"
                                )
                        ),
                        5
                );

        assertEquals(1, suggestions.size());

        assertEquals(
                "MR 21570",
                suggestions.get(0)
                        .displayValue()
        );
    }

    @Test
    public void suggestReferencesRecoversSevenWithoutChangingReliableDigits() {
        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        new WarehouseReference(
                                "M2",
                                "21511"
                        ),
                        Arrays.asList(
                                new WarehouseReference(
                                        "MR",
                                        "21571"
                                ),
                                new WarehouseReference(
                                        "MR",
                                        "21511"
                                ),
                                new WarehouseReference(
                                        "MR",
                                        "20511"
                                ),
                                new WarehouseReference(
                                        "MR",
                                        "21011"
                                ),
                                new WarehouseReference(
                                        "MR",
                                        "21211"
                                )
                        ),
                        5
                );

        assertEquals(2, suggestions.size());

        assertEquals(
                "MR 21511",
                suggestions.get(0)
                        .displayValue()
        );

        assertEquals(
                "MR 21571",
                suggestions.get(1)
                        .displayValue()
        );
    }

    @Test
    public void suggestReferencesKeepsCodeWhenCategoryFiveMeansS() {
        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        new WarehouseReference(
                                "M5",
                                "5008"
                        ),
                        java.util.Arrays.asList(
                                new WarehouseReference(
                                        "MS",
                                        "5008"
                                ),
                                new WarehouseReference(
                                        "MS",
                                        "5038"
                                ),
                                new WarehouseReference(
                                        "MW",
                                        "0008"
                                ),
                                new WarehouseReference(
                                        "MH",
                                        "7508"
                                ),
                                new WarehouseReference(
                                        "MR",
                                        "9000"
                                )
                        ),
                        5
                );

        assertEquals(1, suggestions.size());

        assertEquals(
                "MS 5008",
                suggestions.get(0)
                        .displayValue()
        );
    }

    @Test
    public void suggestReferencesAllowsOnlyOnePlausibleCodeCorrection() {
        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        new WarehouseReference(
                                "ML",
                                "3923"
                        ),
                        java.util.Arrays.asList(
                                new WarehouseReference(
                                        "ML",
                                        "3723"
                                ),
                                new WarehouseReference(
                                        "ML",
                                        "3724"
                                ),
                                new WarehouseReference(
                                        "ML",
                                        "3902"
                                ),
                                new WarehouseReference(
                                        "ML",
                                        "4913"
                                ),
                                new WarehouseReference(
                                        "ML",
                                        "4925"
                                )
                        ),
                        5
                );

        assertEquals(1, suggestions.size());

        assertEquals(
                "ML 3723",
                suggestions.get(0)
                        .displayValue()
        );
    }

    @Test
    public void suggestReferencesDoesNotReplaceARecognizedSeven() {
        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        new WarehouseReference(
                                "MR",
                                "21570"
                        ),
                        java.util.Arrays.asList(
                                new WarehouseReference(
                                        "MR",
                                        "21510"
                                ),
                                new WarehouseReference(
                                        "MR",
                                        "21590"
                                )
                        ),
                        5
                );

        assertTrue(suggestions.isEmpty());
    }

    @Test
    public void suggestReferencesDoesNotFillTheMaximumWithUnrelatedItems() {
        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        new WarehouseReference(
                                "M5",
                                "5008"
                        ),
                        java.util.Arrays.asList(
                                new WarehouseReference(
                                        "MS",
                                        "5008"
                                ),
                                new WarehouseReference(
                                        "MS",
                                        "5038"
                                ),
                                new WarehouseReference(
                                        "MW",
                                        "0008"
                                ),
                                new WarehouseReference(
                                        "MH",
                                        "7508"
                                ),
                                new WarehouseReference(
                                        "MR",
                                        "9000"
                                )
                        ),
                        5
                );

        assertEquals(1, suggestions.size());
    }

    @Test
    public void parseLineAcceptsThreeDigitCodes() {
        List<WarehouseReferenceMatch> matches =
                parser.parseLine(
                        0,
                        "MA 710 - 4pcs"
                );

        assertEquals(1, matches.size());

        assertEquals(
                "MA 710",
                matches.get(0)
                        .getReference()
                        .displayValue()
        );
    }

    @Test
    public void parseLineAcceptsFourAndFiveDigitCodes() {
        List<WarehouseReferenceMatch> fourDigits =
                parser.parseLine(
                        0,
                        "MA 5008 - 3pcs"
                );

        List<WarehouseReferenceMatch> fiveDigits =
                parser.parseLine(
                        1,
                        "MR 21502 - 2pqts"
                );

        assertEquals(1, fourDigits.size());
        assertEquals(1, fiveDigits.size());

        assertEquals(
                "MA 5008",
                fourDigits.get(0)
                        .getReference()
                        .displayValue()
        );

        assertEquals(
                "MR 21502",
                fiveDigits.get(0)
                        .getReference()
                        .displayValue()
        );
    }

    @Test
    public void parseOcrLineIgnoresTitleWithoutNumericCode() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "UT OPYA",
                        java.util.Collections.emptyList()
                );

        assertTrue(matches.isEmpty());
    }

    @Test
    public void parseOcrLinePreservesAttachedAlphabeticQualifier() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "MA710PCS",
                        Collections.emptyList()
                );

        assertEquals(1, matches.size());

        assertEquals(
                "MA 710 PCS",
                matches.get(0)
                        .getReference()
                        .displayValue()
        );
    }

    @Test
    public void parseLineUsesDashAsDocumentBoundary() {
        List<WarehouseReferenceMatch> matches =
                parser.parseLine(
                        0,
                        "MR 21570 - 5 pcs"
                );

        assertEquals(1, matches.size());

        assertEquals(
                "MR 21570",
                matches.get(0)
                        .getReference()
                        .displayValue()
        );
    }

    @Test
    public void parseLinePreservesAttachedQualifier() {
        List<WarehouseReferenceMatch> matches =
                parser.parseLine(
                        0,
                        "MA 900A - 3 pcs"
                );

        assertEquals(1, matches.size());

        assertEquals(
                "MA 900 A",
                matches.get(0)
                        .getReference()
                        .displayValue()
        );
    }

    @Test
    public void parseLinePreservesWordQualifier() {
        List<WarehouseReferenceMatch> matches =
                parser.parseLine(
                        0,
                        "ML 4170 Yellow - 2 pqts"
                );

        assertEquals(1, matches.size());

        assertEquals(
                "ML 4170 YELLOW",
                matches.get(0)
                        .getReference()
                        .displayValue()
        );
    }

    @Test
    public void parseOcrLineResolvesM2AgainstRoom() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "M221570-5pc5",
                        Collections.singletonList(
                                new WarehouseReference(
                                        "MR",
                                        "21570"
                                )
                        )
                );

        assertEquals(1, matches.size());

        assertEquals(
                "MR 21570",
                matches.get(0)
                        .getReference()
                        .displayValue()
        );

        assertTrue(
                parser.isValidCategory(
                        matches.get(0)
                                .getReference()
                                .getCategory()
                )
        );
    }

    @Test
    public void parseOcrLineResolvesM5AgainstRoom() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "M55008-3pcs",
                        Collections.singletonList(
                                new WarehouseReference(
                                        "MS",
                                        "5008"
                                )
                        )
                );

        assertEquals(1, matches.size());

        assertEquals(
                "MS 5008",
                matches.get(0)
                        .getReference()
                        .displayValue()
        );
    }

    @Test
    public void parseOcrLineSelectsUniqueBestCategoryCandidate() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "M221570-5pcs",
                        Arrays.asList(
                                new WarehouseReference(
                                        "MR",
                                        "21570"
                                ),
                                new WarehouseReference(
                                        "MZ",
                                        "21570"
                                )
                        )
                );

        assertEquals(1, matches.size());

        assertEquals(
                "MZ 21570",
                matches.get(0)
                        .getReference()
                        .displayValue()
        );
    }

    @Test
    public void parseOcrLineRejectsEqualBestCategoryCandidates() {
        List<WarehouseReferenceMatch> matches =
                parser.parseOcrLine(
                        0,
                        "M121570-5pcs",
                        Arrays.asList(
                                new WarehouseReference(
                                        "MI",
                                        "21570"
                                ),
                                new WarehouseReference(
                                        "ML",
                                        "21570"
                                )
                        )
                );

        assertTrue(matches.isEmpty());
    }

    @Test
    public void suggestReferencesSupportsNReadInsteadOfR() {
        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        new WarehouseReference(
                                "MN",
                                "21571"
                        ),
                        Collections.singletonList(
                                new WarehouseReference(
                                        "MR",
                                        "21571"
                                )
                        ),
                        5
                );

        assertEquals(1, suggestions.size());

        assertEquals(
                "MR 21571",
                suggestions.get(0)
                        .displayValue()
        );
    }

    @Test
    public void suggestReferencesSupportsNineReadInsteadOfSeven() {
        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        new WarehouseReference(
                                "ML",
                                "3923"
                        ),
                        Collections.singletonList(
                                new WarehouseReference(
                                        "ML",
                                        "3723"
                                )
                        ),
                        5
                );

        assertEquals(1, suggestions.size());

        assertEquals(
                "ML 3723",
                suggestions.get(0)
                        .displayValue()
        );
    }

    @Test
    public void suggestReferencesRecoversJAsSeven() {
        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        new WarehouseReference(
                                "MA",
                                "JI0"
                        ),
                        Collections.singletonList(
                                new WarehouseReference(
                                        "MA",
                                        "710"
                                )
                        ),
                        5
                );

        assertEquals(1, suggestions.size());

        assertEquals(
                "MA 710",
                suggestions.get(0)
                        .displayValue()
        );
    }

    @Test
    public void suggestReferencesRecoversSeparatedFinalOne() {
        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        new WarehouseReference(
                                "MR",
                                "2111 I"
                        ),
                        Collections.singletonList(
                                new WarehouseReference(
                                        "MR",
                                        "21111"
                                )
                        ),
                        5
                );

        assertEquals(1, suggestions.size());

        assertEquals(
                "MR 21111",
                suggestions.get(0)
                        .displayValue()
        );
    }

    @Test
    public void suggestReferencesDoesNotMergeRealAlphabeticQualifier() {
        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        new WarehouseReference(
                                "MA",
                                "900 A"
                        ),
                        Collections.singletonList(
                                new WarehouseReference(
                                        "MA",
                                        "900"
                                )
                        ),
                        5
                );

        assertTrue(suggestions.isEmpty());
    }

    @Test
    public void suggestReferencesTreatsZeroAndSixAsOcrConfusion() {
        List<WarehouseReference> suggestions =
                parser.suggestReferences(
                        new WarehouseReference("MR", "20106"),
                        Collections.singletonList(
                                new WarehouseReference("MR", "20166")
                        ),
                        5
                );

        assertEquals(1, suggestions.size());
        assertEquals("MR 20166", suggestions.get(0).displayValue());
    }

    @Test
    public void suggestZeroSixAlternativesRejectsOtherDifferences() {
        List<WarehouseReference> suggestions =
                parser.suggestZeroSixAlternatives(
                        new WarehouseReference("MR", "20106"),
                        Arrays.asList(
                                new WarehouseReference("MR", "20170"),
                                new WarehouseReference("MR", "20166"),
                                new WarehouseReference("MA", "20166")
                        ),
                        5
                );

        assertEquals(1, suggestions.size());
        assertEquals("MR 20166", suggestions.get(0).displayValue());
    }
}
