package com.rndymi.almacentracker.feature.reference_list.review;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.domain.reference.DocumentReferenceData;
import com.rndymi.almacentracker.domain.reference.DocumentReferenceDataParser;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceMatch;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceParser;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceSuggestion;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceSuggestionResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public final class ReferenceListReviewViewModel
        extends ViewModel {

    private static final int MAXIMUM_SUGGESTIONS = 5;

    private static final Pattern OCR_CATEGORY_FRAGMENT =
            Pattern.compile(
                    "^[A-Z0-9]{2}$",
                    Pattern.CASE_INSENSITIVE
            );

    private final WarehouseReferenceParser parser;
    private final WarehouseItemRepository repository;
    private final DocumentReferenceDataParser documentReferenceDataParser;
    private final WarehouseReferenceSuggestionResolver suggestionResolver;

    private final MutableLiveData
            <ReferenceListReviewUiState> uiState =
            new MutableLiveData<>(
                    ReferenceListReviewUiState
                            .initial()
            );

    private final MutableLiveData<
            UiEvent<List<DocumentReferenceData>>>
            confirmationEvent =
            new MutableLiveData<>();

    private final MutableLiveData
            <UiEvent<Integer>>
            duplicateConsolidatedEvent =
            new MutableLiveData<>();

    private long nextProposalId = 1L;
    private boolean initializationStarted;

    public ReferenceListReviewViewModel(
            WarehouseReferenceParser parser
    ) {
        this(
                parser,
                null,
                new DocumentReferenceDataParser()
        );
    }

    public ReferenceListReviewViewModel(
            WarehouseReferenceParser parser,
            WarehouseItemRepository repository
    ) {
        this(
                parser,
                repository,
                new DocumentReferenceDataParser()
        );
    }

    public ReferenceListReviewViewModel(
            WarehouseReferenceParser parser,
            WarehouseItemRepository repository,
            DocumentReferenceDataParser documentReferenceDataParser
    ) {
        this(
                parser,
                repository,
                documentReferenceDataParser,
                new WarehouseReferenceSuggestionResolver()
        );
    }

    ReferenceListReviewViewModel(
            WarehouseReferenceParser parser,
            WarehouseItemRepository repository,
            DocumentReferenceDataParser documentReferenceDataParser,
            WarehouseReferenceSuggestionResolver suggestionResolver
    ) {
        this.parser =
                Objects.requireNonNull(
                        parser,
                        "parser"
                );
        this.repository = repository;
        this.documentReferenceDataParser =
                Objects.requireNonNull(
                        documentReferenceDataParser,
                        "documentReferenceDataParser"
                );
        this.suggestionResolver =
                Objects.requireNonNull(
                        suggestionResolver,
                        "suggestionResolver"
                );
    }

    public LiveData<ReferenceListReviewUiState>
    getUiState() {
        return uiState;
    }

    public LiveData<
            UiEvent<List<DocumentReferenceData>>>
    getConfirmationEvent() {
        return confirmationEvent;
    }

    public LiveData<UiEvent<Integer>>
    getDuplicateConsolidatedEvent() {
        return duplicateConsolidatedEvent;
    }

    public void applyInitialLines(
            List<String> lines
    ) {
        ReferenceListReviewUiState current =
                uiState.getValue();

        if ((current != null
                && current.isInitialized())
                || initializationStarted) {
            return;
        }

        initializationStarted = true;

        List<String> copiedLines =
                lines == null
                        ? Collections.emptyList()
                        : new ArrayList<>(lines);

        if (repository == null) {
            resolveInitialLines(
                    copiedLines,
                    Collections.emptyList(),
                    false
            );
            return;
        }

        repository.findAll(
                new RepositoryCallback<List<WarehouseItem>>() {

                    @Override
                    public void onSuccess(
                            List<WarehouseItem> items
                    ) {
                        resolveInitialLines(
                                copiedLines,
                                referencesFrom(items),
                                true
                        );
                    }

                    @Override
                    public void onError(Throwable cause) {
                        resolveInitialLines(
                                copiedLines,
                                Collections.emptyList(),
                                false
                        );
                    }
                }
        );
    }

    private void resolveInitialLines(
            List<String> lines,
            List<WarehouseReference> knownReferences,
            boolean knownReferencesAvailable
    ) {
        Map<String, ReferenceProposal> unique =
                new LinkedHashMap<>();

        int duplicateCount = 0;

        if (lines != null) {
            for (
                    int lineIndex = 0;
                    lineIndex < lines.size();
                    lineIndex++
            ) {
                String rawLine = lines.get(lineIndex);

                List<WarehouseReferenceMatch> matches =
                        parser.parseOcrLine(
                                lineIndex,
                                rawLine,
                                knownReferences
                        );

                if (matches.isEmpty()
                        && isCategoryFragment(rawLine)
                        && lineIndex + 1 < lines.size()) {
                    String combinedLine =
                            rawLine + " "
                                    + lines.get(lineIndex + 1);

                    matches =
                            parser.parseOcrLine(
                                    lineIndex,
                                    combinedLine,
                                    knownReferences
                            );

                    if (!matches.isEmpty()) {
                        lineIndex++;
                    }
                }

                for (
                        WarehouseReferenceMatch match
                        : matches
                ) {
                    WarehouseReference reference =
                            match.getObservedReference();

                    if (unique.containsKey(
                            reference.identityKey()
                    )) {
                        duplicateCount++;
                        continue;
                    }

                    unique.put(
                            reference.identityKey(),
                            createRecognizedProposal(
                                    nextProposalId++,
                                    match,
                                    knownReferences,
                                    knownReferencesAvailable
                            )
                    );
                }
            }
        }

        uiState.postValue(
                new ReferenceListReviewUiState(
                        new ArrayList<>(
                                unique.values()
                        ),
                        true,
                        false
                )
        );

        if (duplicateCount > 0) {
            duplicateConsolidatedEvent
                    .postValue(
                            new UiEvent<>(
                                    duplicateCount
                            )
                    );
        }
    }

    private boolean isCategoryFragment(
            String rawLine
    ) {
        if (rawLine == null) {
            return false;
        }

        String compact =
                rawLine.replaceAll(
                                "[\\p{Z}\\s]+",
                                ""
                        )
                        .toUpperCase(Locale.ROOT);

        if (!OCR_CATEGORY_FRAGMENT
                .matcher(compact)
                .matches()) {
            return false;
        }

        int digitCount = 0;

        for (int index = 0;
             index < compact.length();
             index++) {

            if (Character.isDigit(
                    compact.charAt(index)
            )) {
                digitCount++;
            }
        }

        return digitCount <= 1;
    }

    private boolean isValidReference(
            WarehouseReference reference
    ) {
        return parser.isValidCategory(
                reference.getCategory()
        )
                && parser.isValidCode(
                reference.getCode()
        );
    }

    private ReferenceProposal createRecognizedProposal(
            long proposalId,
            WarehouseReferenceMatch match,
            List<WarehouseReference> knownReferences,
            boolean knownReferencesAvailable
    ) {
        WarehouseReference observedReference =
                match.getObservedReference();

        List<WarehouseReferenceSuggestion>
                contextualSuggestions =
                knownReferencesAvailable
                        ? suggestionResolver.resolve(
                        observedReference,
                        knownReferences,
                        MAXIMUM_SUGGESTIONS
                )
                        : Collections.emptyList();

        if (knownReferences.contains(observedReference)) {
            contextualSuggestions =
                    highConfidenceSuggestions(
                            contextualSuggestions
                    );
        }

        List<WarehouseReference> suggestions =
                referencesFromSuggestions(
                        contextualSuggestions
                );

        ReferenceProposal.MatchStatus matchStatus =
                contextualMatchStatusFor(
                        observedReference,
                        contextualSuggestions,
                        knownReferences,
                        knownReferencesAvailable
                );

        return new ReferenceProposal(
                proposalId,
                observedReference,
                observedReference,
                match.getSourceRawText(),
                false,
                matchStatus,
                suggestions,
                contextualSuggestions,
                documentReferenceDataParser.parse(match)
        );
    }

    private ReferenceProposal.MatchStatus matchStatusFor(
            WarehouseReference reference,
            List<WarehouseReference> suggestions,
            List<WarehouseReference> knownReferences,
            boolean knownReferencesAvailable
    ) {
        if (!knownReferencesAvailable) {
            return isValidReference(reference)
                    ? ReferenceProposal.MatchStatus.UNVERIFIED
                    : ReferenceProposal.MatchStatus.NO_MATCH;
        }

        if (isValidReference(reference)
                && knownReferences.contains(reference)) {
            return suggestions.isEmpty()
                    ? ReferenceProposal.MatchStatus.EXACT
                    : ReferenceProposal.MatchStatus.AMBIGUOUS;
        }

        if (suggestions.size() == 1) {
            return ReferenceProposal.MatchStatus
                    .UNIQUE_SUGGESTION;
        }

        if (suggestions.size() > 1) {
            return ReferenceProposal.MatchStatus.AMBIGUOUS;
        }

        return ReferenceProposal.MatchStatus.NO_MATCH;
    }

    private List<WarehouseReference> suggestionsFor(
            WarehouseReference reference,
            List<WarehouseReference> knownReferences,
            boolean knownReferencesAvailable
    ) {
        if (!knownReferencesAvailable) {
            return Collections.emptyList();
        }

        if (knownReferences.contains(reference)) {
            return parser.suggestZeroSixAlternatives(
                    reference,
                    knownReferences,
                    MAXIMUM_SUGGESTIONS
            );
        }

        return parser.suggestReferences(
                reference,
                knownReferences,
                MAXIMUM_SUGGESTIONS
        );
    }

    private List<WarehouseReference> referencesFrom(
            List<WarehouseItem> items
    ) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        List<WarehouseReference> references =
                new ArrayList<>(items.size());

        for (WarehouseItem item : items) {
            if (item == null) {
                continue;
            }

            WarehouseReference reference =
                    parser.parseInput(
                            item.getCategory(),
                            item.getCode()
                    );

            if (reference != null) {
                references.add(reference);
            }
        }

        return references;
    }

    public ReferenceInputResult addReference(
            String category,
            String code
    ) {
        WarehouseReference reference =
                parser.parseInput(
                        category,
                        code
                );

        if (reference == null) {
            return invalidResult(
                    category,
                    code
            );
        }

        List<ReferenceProposal> proposals =
                mutableProposals();

        if (containsReference(
                proposals,
                reference,
                -1L
        )) {
            return ReferenceInputResult
                    .duplicate();
        }

        proposals.add(
                new ReferenceProposal(
                        nextProposalId++,
                        reference,
                        null,
                        true,
                        ReferenceProposal.MatchStatus
                                .USER_CONFIRMED,
                        Collections.emptyList(),
                        new DocumentReferenceData(
                                reference,
                                null,
                                null,
                                proposals.size(),
                                null
                        )
                )
        );

        publish(proposals);

        return ReferenceInputResult.success();
    }

    public ReferenceInputResult editReference(
            long proposalId,
            String category,
            String code
    ) {
        WarehouseReference reference =
                parser.parseInput(
                        category,
                        code
                );

        if (reference == null) {
            return invalidResult(
                    category,
                    code
            );
        }

        List<ReferenceProposal> proposals =
                mutableProposals();

        int proposalIndex =
                findIndex(
                        proposals,
                        proposalId
                );

        if (proposalIndex < 0) {
            return ReferenceInputResult
                    .notFound();
        }

        if (containsReference(
                proposals,
                reference,
                proposalId
        )) {
            return ReferenceInputResult
                    .duplicate();
        }

        ReferenceProposal current =
                proposals.get(
                        proposalIndex
                );

        proposals.set(
                proposalIndex,
                current.withReference(
                        reference
                )
        );

        publish(proposals);

        return ReferenceInputResult.success();
    }

    public ReferenceInputResult applySuggestion(
            long proposalId,
            WarehouseReference suggestion
    ) {
        if (suggestion == null) {
            return ReferenceInputResult.notFound();
        }

        List<ReferenceProposal> proposals =
                mutableProposals();

        int proposalIndex =
                findIndex(
                        proposals,
                        proposalId
                );

        if (proposalIndex < 0) {
            return ReferenceInputResult.notFound();
        }

        ReferenceProposal current =
                proposals.get(proposalIndex);

        if (!current.getSuggestions()
                .contains(suggestion)) {
            return ReferenceInputResult.notFound();
        }

        if (containsReference(
                proposals,
                suggestion,
                proposalId
        )) {
            return ReferenceInputResult.duplicate();
        }

        proposals.set(
                proposalIndex,
                current.withReference(
                        suggestion
                )
        );

        publish(proposals);

        return ReferenceInputResult.success();
    }

    public ReferenceInputResult applyQuantitySuggestion(
            long proposalId,
            Integer quantity
    ) {
        if (quantity == null || quantity <= 0) {
            return ReferenceInputResult.notFound();
        }

        List<ReferenceProposal> proposals = mutableProposals();
        int proposalIndex = findIndex(proposals, proposalId);

        if (proposalIndex < 0) {
            return ReferenceInputResult.notFound();
        }

        ReferenceProposal current = proposals.get(proposalIndex);
        DocumentReferenceData documentData =
                current.getDocumentData();

        boolean isObservedQuantity =
                quantity.equals(documentData.getQuantity());

        if (!isObservedQuantity
                && !documentData.getQuantitySuggestions()
                .contains(quantity)) {
            return ReferenceInputResult.notFound();
        }

        proposals.set(
                proposalIndex,
                current.withQuantity(quantity)
        );

        publish(proposals);

        return ReferenceInputResult.success();
    }

    public void deleteReference(
            long proposalId
    ) {
        List<ReferenceProposal> proposals =
                mutableProposals();

        int proposalIndex =
                findIndex(
                        proposals,
                        proposalId
                );

        if (proposalIndex < 0) {
            return;
        }

        proposals.remove(
                proposalIndex
        );

        publish(proposals);
    }

    public void confirm() {
        ReferenceListReviewUiState current =
                uiState.getValue();

        if (current == null
                || !current.canConfirm()) {
            return;
        }

        List<DocumentReferenceData> confirmedValues =
                new ArrayList<>();

        for (
                ReferenceProposal proposal
                : current.getProposals()
        ) {
            confirmedValues.add(
                    proposal.getDocumentData()
            );
        }

        uiState.setValue(
                new ReferenceListReviewUiState(
                        current.getProposals(),
                        true,
                        true
                )
        );

        confirmationEvent.setValue(
                new UiEvent<>(
                        confirmedValues
                )
        );
    }

    private ReferenceInputResult invalidResult(
            String category,
            String code
    ) {
        String normalizedCategory =
                parser.normalizeCategory(
                        category
                );

        String normalizedCode =
                parser.normalizeCode(
                        code
                );

        return ReferenceInputResult.invalid(
                parser.isValidCategory(
                        normalizedCategory
                ),
                parser.isValidCode(
                        normalizedCode
                )
        );
    }

    private List<ReferenceProposal>
    mutableProposals() {
        ReferenceListReviewUiState current =
                uiState.getValue();

        if (current == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(
                current.getProposals()
        );
    }

    private void publish(
            List<ReferenceProposal> proposals
    ) {
        uiState.setValue(
                new ReferenceListReviewUiState(
                        proposals,
                        true,
                        false
                )
        );
    }

    private boolean containsReference(
            List<ReferenceProposal> proposals,
            WarehouseReference reference,
            long excludedProposalId
    ) {
        for (
                ReferenceProposal proposal
                : proposals
        ) {
            if (proposal.getId()
                    == excludedProposalId) {
                continue;
            }

            if (proposal
                    .getReference()
                    .equals(reference)) {
                return true;
            }
        }

        return false;
    }

    private int findIndex(
            List<ReferenceProposal> proposals,
            long proposalId
    ) {
        for (
                int index = 0;
                index < proposals.size();
                index++
        ) {
            if (proposals
                    .get(index)
                    .getId()
                    == proposalId) {
                return index;
            }
        }

        return -1;
    }

    private List<WarehouseReference>
    referencesFromSuggestions(
            List<WarehouseReferenceSuggestion> values
    ) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        List<WarehouseReference> result =
                new ArrayList<>(values.size());

        for (WarehouseReferenceSuggestion value : values) {
            if (value != null
                    && !result.contains(
                    value.getReference()
            )) {
                result.add(
                        value.getReference()
                );
            }
        }

        return result.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(result);
    }

    private ReferenceProposal.MatchStatus
    contextualMatchStatusFor(
            WarehouseReference observed,
            List<WarehouseReferenceSuggestion> suggestions,
            List<WarehouseReference> knownReferences,
            boolean knownReferencesAvailable
    ) {
        if (!knownReferencesAvailable) {
            return isValidReference(observed)
                    ? ReferenceProposal.MatchStatus.UNVERIFIED
                    : ReferenceProposal.MatchStatus.NO_MATCH;
        }

        if (knownReferences.contains(observed)) {
            return suggestions.isEmpty()
                    ? ReferenceProposal.MatchStatus.EXACT
                    : ReferenceProposal.MatchStatus.AMBIGUOUS;
        }

        if (suggestions.size() == 1) {
            return ReferenceProposal.MatchStatus
                    .UNIQUE_SUGGESTION;
        }

        if (suggestions.size() > 1) {
            int bestScore =
                    suggestions.get(0).getScore();

            int bestCount = 0;

            for (WarehouseReferenceSuggestion suggestion
                    : suggestions) {

                if (suggestion.getScore()
                        == bestScore) {
                    bestCount++;
                }
            }

            return bestCount == 1
                    ? ReferenceProposal.MatchStatus
                      .UNIQUE_SUGGESTION
                    : ReferenceProposal.MatchStatus.AMBIGUOUS;
        }

        return ReferenceProposal.MatchStatus.NO_MATCH;
    }

    private List<WarehouseReferenceSuggestion>
    highConfidenceSuggestions(
            List<WarehouseReferenceSuggestion> values
    ) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        List<WarehouseReferenceSuggestion> result =
                new ArrayList<>();

        for (WarehouseReferenceSuggestion value : values) {
            if (value.getScore() == 1) {
                result.add(value);
            }
        }

        return result.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(result);
    }
}
