package com.rndymi.almacentracker.domain.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class WarehouseReferenceSuggestionResolverTest {

    private final WarehouseReferenceSuggestionResolver resolver =
            new WarehouseReferenceSuggestionResolver();

    @Test
    public void resolveScoresKnownOcrConfusionsAndOrdersBestFirst() {
        WarehouseReference observed = reference("MK", "866S");

        List<WarehouseReferenceSuggestion> result =
                resolver.resolve(
                        observed,
                        Arrays.asList(
                                reference("MR", "8665"),
                                reference("MK", "8665"),
                                reference("MR", "866S")
                        ),
                        5
                );

        assertEquals(3, result.size());
        assertEquals(
                reference("MK", "8665"),
                result.get(0).getReference()
        );
        assertEquals(1, result.get(0).getScore());
        assertEquals(
                "S → 5 en código",
                result.get(0).getExplanation()
        );
        assertEquals(
                reference("MR", "866S"),
                result.get(1).getReference()
        );
        assertEquals(1, result.get(1).getScore());
        assertEquals(2, result.get(2).getScore());
    }

    @Test
    public void resolveSupportsMergedHAndMissingDigit() {
        List<WarehouseReferenceSuggestion> merged =
                resolver.resolve(
                        reference("MR", "2H5"),
                        Collections.singletonList(
                                reference("MR", "2115")
                        ),
                        5
                );

        assertEquals(1, merged.size());
        assertEquals(1, merged.get(0).getScore());
        assertEquals(
                "H → 11 en código",
                merged.get(0).getExplanation()
        );

        List<WarehouseReferenceSuggestion> omitted =
                resolver.resolve(
                        reference("MR", "2157"),
                        Collections.singletonList(
                                reference("MR", "21571")
                        ),
                        5
                );

        assertEquals(1, omitted.size());
        assertEquals(
                "dígito 1 omitido",
                omitted.get(0).getExplanation()
        );
    }

    @Test
    public void resolveExcludesExactAndUnrelatedReferencesAndHonorsLimit() {
        WarehouseReference observed = reference("MR", "1O5S");

        List<WarehouseReferenceSuggestion> result =
                resolver.resolve(
                        observed,
                        Arrays.asList(
                                observed,
                                reference("MR", "1055"),
                                reference("MR", "1051"),
                                reference("ZZ", "9999")
                        ),
                        1
                );

        assertEquals(1, result.size());
        assertEquals(
                reference("MR", "1055"),
                result.get(0).getReference()
        );
        assertTrue(
                resolver.resolve(
                        observed,
                        null,
                        5
                ).isEmpty()
        );
        assertTrue(
                resolver.resolve(
                        observed,
                        Collections.singletonList(
                                reference("MR", "1055")
                        ),
                        0
                ).isEmpty()
        );
    }

    @Test
    public void resolveUsesAlphabeticCategoryRulesAndNumericCodeRules() {
        assertSingleSuggestion(
                reference("M2", "21570"),
                reference("MR", "21570")
        );
        assertTrue(
                resolver.resolve(
                        reference("MN", "21S7I"),
                        Collections.singletonList(
                                reference("MR", "21571")
                        ),
                        5
                ).isEmpty()
        );
        assertSingleSuggestion(
                reference("M5", "5008"),
                reference("MS", "5008")
        );
        assertSingleSuggestion(
                reference("ML", "3923"),
                reference("ML", "3723")
        );

        List<WarehouseReferenceSuggestion> alternatives =
                resolver.resolve(
                        reference("MR", "21S70"),
                        Arrays.asList(
                                reference("MR", "21570"),
                                reference("MR", "21670")
                        ),
                        5
                );

        assertEquals(2, alternatives.size());
    }

    private void assertSingleSuggestion(
            WarehouseReference observed,
            WarehouseReference expected
    ) {
        List<WarehouseReferenceSuggestion> result =
                resolver.resolve(
                        observed,
                        Collections.singletonList(expected),
                        5
                );

        assertEquals(1, result.size());
        assertEquals(expected, result.get(0).getReference());
    }

    private WarehouseReference reference(
            String category,
            String code
    ) {
        return new WarehouseReference(category, code);
    }
}
