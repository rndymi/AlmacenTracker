package com.rndymi.almacentracker.feature.reference_list.review;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.domain.reference.DocumentReferenceData;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceSuggestion;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public final class ReferenceProposalTest {

    @Test
    public void withReferenceConfirmsSelectionAndPreservesDocumentContext() {
        WarehouseReference observed = reference("MK", "866S");
        WarehouseReference suggestion = reference("MR", "8665");
        WarehouseReferenceSuggestion contextual =
                new WarehouseReferenceSuggestion(
                        suggestion,
                        2,
                        "K → R en categoría, S → 5 en código"
                );
        DocumentReferenceData documentData =
                new DocumentReferenceData(
                        observed,
                        observed,
                        1,
                        "P",
                        4,
                        "MK866S-1P-①②",
                        Collections.singletonList(6),
                        Arrays.asList("①", "②")
                );
        ReferenceProposal proposal =
                new ReferenceProposal(
                        7L,
                        observed,
                        observed,
                        documentData.getSourceText(),
                        false,
                        ReferenceProposal.MatchStatus.UNIQUE_SUGGESTION,
                        Collections.singletonList(suggestion),
                        Collections.singletonList(contextual),
                        documentData
                );

        ReferenceProposal updated =
                proposal.withReference(suggestion);

        assertEquals(suggestion, updated.getReference());
        assertEquals(observed, updated.getObservedReference());
        assertEquals(
                ReferenceProposal.MatchStatus.USER_CONFIRMED,
                updated.getMatchStatus()
        );
        assertTrue(updated.getSuggestions().isEmpty());
        assertEquals(
                Collections.singletonList(contextual),
                updated.getContextualSuggestions()
        );
        assertEquals(
                observed,
                updated.getDocumentData()
                        .getObservedReference()
        );
        assertEquals(
                Arrays.asList("①", "②"),
                updated.getDocumentData().getDestinations()
        );
        assertEquals(
                Collections.singletonList(6),
                updated.getDocumentData()
                        .getQuantitySuggestions()
        );
    }

    @Test
    public void withQuantityClearsAmbiguityAndPreservesDocumentContext() {
        WarehouseReference reference = reference("MR", "21570");
        WarehouseReference observed = reference("MK", "2I57O");
        DocumentReferenceData documentData =
                new DocumentReferenceData(
                        reference,
                        observed,
                        40,
                        "PCS",
                        2,
                        "MK2I57O-40PCS-③",
                        Collections.singletonList(46),
                        Collections.singletonList("③")
                );
        ReferenceProposal proposal =
                new ReferenceProposal(
                        1L,
                        reference,
                        observed,
                        documentData.getSourceText(),
                        false,
                        ReferenceProposal.MatchStatus.EXACT,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        documentData
                );

        assertTrue(proposal.requiresCorrection());

        ReferenceProposal updated = proposal.withQuantity(46);

        assertEquals(
                Integer.valueOf(46),
                updated.getDocumentData().getQuantity()
        );
        assertEquals(
                observed,
                updated.getDocumentData().getObservedReference()
        );
        assertEquals(
                Collections.singletonList("③"),
                updated.getDocumentData().getDestinations()
        );
        assertTrue(
                updated.getDocumentData()
                        .getQuantitySuggestions().isEmpty()
        );
        assertFalse(updated.requiresCorrection());
    }

    private WarehouseReference reference(
            String category,
            String code
    ) {
        return new WarehouseReference(category, code);
    }
}
