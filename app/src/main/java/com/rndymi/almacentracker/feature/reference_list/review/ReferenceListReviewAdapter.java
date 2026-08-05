package com.rndymi.almacentracker.feature.reference_list.review;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.databinding.ItemReferenceProposalBinding;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;
import com.rndymi.almacentracker.domain.reference.DocumentReferenceAllocation;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ReferenceListReviewAdapter
        extends ListAdapter<
        ReferenceProposal,
        ReferenceListReviewAdapter.ViewHolder
        > {

    public interface Listener {

        void onEdit(
                ReferenceProposal proposal
        );

        void onDelete(
                ReferenceProposal proposal
        );

        void onSuggestion(
                ReferenceProposal proposal,
                WarehouseReference suggestion
        );

        void onQuantitySuggestion(
                ReferenceProposal proposal,
                int quantity
        );

        void onReviewToggle(
                ReferenceProposal proposal
        );
    }

    private final Listener listener;

    public ReferenceListReviewAdapter(
            Listener listener
    ) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemReferenceProposalBinding binding =
                ItemReferenceProposalBinding
                        .inflate(
                                LayoutInflater.from(
                                        parent.getContext()
                                ),
                                parent,
                                false
                        );

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        holder.bind(
                getItem(position)
        );
    }

    final class ViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemReferenceProposalBinding
                binding;

        ViewHolder(
                ItemReferenceProposalBinding binding
        ) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(
                ReferenceProposal proposal
        ) {
            String displayValue =
                    proposal
                            .getReference()
                            .displayValue();

            binding.referenceValue.setText(
                    displayValue
            );

            bindMatchStatus(proposal);
            bindReviewStateButton(proposal, displayValue);

            boolean requiresCorrection =
                    proposal.requiresCorrection();

            binding.referenceValue.setTextColor(
                    MaterialColors.getColor(
                            binding.referenceValue,
                            requiresCorrection
                                    ? com.google.android.material.R.attr.colorError
                                    : com.google.android.material.R.attr.colorOnSurface
                    )
            );

            String sourceRawText =
                    proposal.getSourceRawText();

            boolean hasSource =
                    sourceRawText != null
                            && !sourceRawText
                            .trim()
                            .isEmpty();

            binding.referenceSource.setVisibility(
                    hasSource
                            ? View.VISIBLE
                            : View.GONE
            );

            if (hasSource) {
                binding.referenceSource.setTextColor(
                        MaterialColors.getColor(
                                binding.referenceSource,
                                com.google.android.material.R.attr
                                        .colorOnSurfaceVariant
                        )
                );

                binding.referenceSource.setText(
                        itemView.getContext()
                                .getString(
                                        R.string
                                                .reference_list_review_source_format,
                                        sourceRawText
                                )
                );
            } else {
                binding.referenceSource.setText("");
            }

            bindSuggestions(proposal);
            bindQuantitySuggestions(proposal);
            bindDocumentData(proposal);

            binding.editReferenceButton
                    .setContentDescription(
                            itemView.getContext()
                                    .getString(
                                            R.string
                                                    .reference_list_review_edit_description,
                                            displayValue
                                    )
                    );

            binding.deleteReferenceButton
                    .setContentDescription(
                            itemView.getContext()
                                    .getString(
                                            R.string
                                                    .reference_list_review_delete_description,
                                            displayValue
                                    )
                    );

            binding.editReferenceButton
                    .setOnClickListener(
                            ignored ->
                                    listener.onEdit(
                                            proposal
                                    )
                    );

            binding.deleteReferenceButton
                    .setOnClickListener(
                            ignored ->
                                    listener.onDelete(
                                            proposal
                                    )
                    );

            binding.reviewStateButton.setOnClickListener(
                    ignored -> listener.onReviewToggle(proposal)
            );
        }

        private void bindReviewStateButton(
                ReferenceProposal proposal,
                String displayValue
        ) {
            boolean accepted = proposal.isCodeAccepted();

            binding.reviewStateButton.setImageResource(
                    accepted
                            ? R.drawable.ic_check_circle_filled
                            : R.drawable.ic_check_circle_outline
            );
            int color = accepted
                    ? ContextCompat.getColor(
                    itemView.getContext(),
                    R.color.reference_review_ready
            )
                    : MaterialColors.getColor(
                    binding.reviewStateButton,
                    com.google.android.material.R.attr
                            .colorOnSurfaceVariant
            );
            binding.reviewStateButton.setImageTintList(
                    ColorStateList.valueOf(color)
            );
            binding.reviewStateButton.setContentDescription(
                    itemView.getContext().getString(
                            accepted
                                    ? R.string.reference_list_review_mark_pending
                                    : R.string.reference_list_review_mark_ready,
                            displayValue
                    )
            );
        }

        private void bindDocumentData(
                ReferenceProposal proposal
        ) {
            List<String> descriptions =
                    new java.util.ArrayList<>();

            Integer quantity = proposal
                    .getDocumentData()
                    .getQuantity();
            String unit = proposal
                    .getDocumentData()
                    .getUnit();

            if (quantity != null) {
                descriptions.add(
                        itemView.getContext().getString(
                                R.string.reference_list_review_quantity_format,
                                quantity,
                                unit == null ? "" : unit
                        ).trim()
                );
            }

            if (!proposal.getDocumentData()
                    .getDestinations().isEmpty()) {
                descriptions.add(
                        itemView.getContext().getString(
                                R.string.reference_list_review_destinations_format,
                                String.join(
                                        ", ",
                                        proposal.getDocumentData()
                                                .getDestinations()
                                )
                        )
                );
            }

            List<DocumentReferenceAllocation> allocations =
                    proposal.getDocumentData().getAllocations();

            if (!allocations.isEmpty()) {
                List<String> quantities = new ArrayList<>();
                List<String> stores = new ArrayList<>();

                for (DocumentReferenceAllocation allocation
                        : allocations) {
                    quantities.add(
                            allocation.getQuantity()
                                    + " "
                                    + allocation.getUnit()
                    );
                    stores.add(formatStore(allocation.getDestination()));
                }

                descriptions.add(
                        itemView.getContext().getString(
                                R.string.reference_list_review_allocation_quantities_format,
                                String.join("  |  ", quantities)
                        )
                );
                descriptions.add(
                        itemView.getContext().getString(
                                R.string.reference_list_review_allocation_stores_format,
                                String.join("  |  ", stores)
                        )
                );
            }

            boolean hasData = !descriptions.isEmpty();

            binding.documentDataText.setVisibility(
                    hasData ? View.VISIBLE : View.GONE
            );
            binding.documentDataText.setText(
                    hasData
                            ? String.join("\n", descriptions)
                            : ""
            );
        }

        private String formatStore(String destination) {
            if (destination == null) {
                return "";
            }

            java.util.regex.Matcher matcher =
                    java.util.regex.Pattern.compile(
                            "(?i)^TIENDA[\\p{Z}\\s]*([0-9]{1,2})$"
                    ).matcher(destination.trim());

            if (!matcher.matches()) {
                return destination;
            }

            int number = Integer.parseInt(matcher.group(1));

            if (number >= 1 && number <= 20) {
                return String.valueOf(
                        (char) ('①' + number - 1)
                );
            }

            if (number >= 21 && number <= 35) {
                return String.valueOf(
                        (char) ('㉑' + number - 21)
                );
            }

            if (number >= 36 && number <= 50) {
                return String.valueOf(
                        (char) ('㊱' + number - 36)
                );
            }

            return destination;
        }

        private void bindMatchStatus(
                ReferenceProposal proposal
        ) {
            int textResource;
            int colorAttribute;

            if (proposal.getReviewState()
                    == ReferenceProposal.ReviewState.APPROVED) {
                binding.matchStatusText.setText(
                        R.string.reference_list_review_match_user_confirmed
                );
                binding.matchStatusText.setTextColor(
                        ContextCompat.getColor(
                                itemView.getContext(),
                                R.color.reference_review_ready
                        )
                );
                return;
            }

            if (proposal.getReviewState()
                    == ReferenceProposal.ReviewState.NEEDS_REVIEW) {
                binding.matchStatusText.setText(
                        R.string.reference_list_review_match_marked_pending
                );
                binding.matchStatusText.setTextColor(
                        MaterialColors.getColor(
                                binding.matchStatusText,
                                com.google.android.material.R.attr
                                        .colorSecondary
                        )
                );
                return;
            }

            switch (proposal.getMatchStatus()) {
                case EXACT:
                    textResource =
                            R.string
                                    .reference_list_review_match_exact;
                    colorAttribute =
                            com.google.android.material.R.attr
                                    .colorPrimary;
                    break;

                case UNIQUE_SUGGESTION:
                    textResource =
                            R.string
                                    .reference_list_review_match_unique;
                    colorAttribute =
                            com.google.android.material.R.attr
                                    .colorSecondary;
                    break;

                case AMBIGUOUS:
                    textResource =
                            R.string
                                    .reference_list_review_match_ambiguous;
                    colorAttribute =
                            com.google.android.material.R.attr
                                    .colorError;
                    break;

                case NO_MATCH:
                    textResource =
                            R.string
                                    .reference_list_review_match_none;
                    colorAttribute =
                            com.google.android.material.R.attr
                                    .colorError;
                    break;

                case UNVERIFIED:
                    textResource =
                            R.string
                                    .reference_list_review_match_unverified;
                    colorAttribute =
                            com.google.android.material.R.attr
                                    .colorOnSurfaceVariant;
                    break;

                case USER_CONFIRMED:
                    textResource =
                            R.string
                                    .reference_list_review_match_user_confirmed;
                    colorAttribute =
                            com.google.android.material.R.attr
                                    .colorPrimary;
                    break;

                default:
                    throw new IllegalStateException(
                            "Unexpected match status: "
                                    + proposal.getMatchStatus()
                    );
            }

            binding.matchStatusText.setText(textResource);
            binding.matchStatusText.setTextColor(
                    MaterialColors.getColor(
                            binding.matchStatusText,
                            colorAttribute
                    )
            );
        }

        private void bindSuggestions(
                ReferenceProposal proposal
        ) {
            List<WarehouseReference> suggestions =
                    proposal.getSuggestions();

            boolean hasSuggestions =
                    proposal.shouldShowSuggestions()
                            && !suggestions.isEmpty();

            binding.suggestionsTitle.setVisibility(
                    hasSuggestions
                            ? View.VISIBLE
                            : View.GONE
            );
            binding.suggestionsGroup.setVisibility(
                    hasSuggestions
                            ? View.VISIBLE
                            : View.GONE
            );
            binding.suggestionsGroup.removeAllViews();

            if (!hasSuggestions) {
                return;
            }

            for (WarehouseReference suggestion
                    : suggestions) {
                Chip chip =
                        new Chip(
                                itemView.getContext()
                        );

                String displayValue =
                        suggestion.displayValue();

                chip.setText(displayValue);
                chip.setCheckable(false);
                chip.setClickable(true);
                chip.setContentDescription(
                        itemView.getContext()
                                .getString(
                                        R.string
                                                .reference_list_review_suggestion_description,
                                        displayValue
                                )
                );
                chip.setOnClickListener(
                        ignored ->
                                listener.onSuggestion(
                                        proposal,
                                        suggestion
                                )
                );

                binding.suggestionsGroup.addView(chip);
            }
        }

        private void bindQuantitySuggestions(
                ReferenceProposal proposal
        ) {
            Integer observedQuantity =
                    proposal.getDocumentData().getQuantity();
            List<Integer> suggestions =
                    proposal.getDocumentData()
                            .getQuantitySuggestions();
            boolean hasSuggestions =
                    !suggestions.isEmpty();

            binding.quantitySuggestionsTitle.setVisibility(
                    hasSuggestions ? View.VISIBLE : View.GONE
            );
            binding.quantitySuggestionsGroup.setVisibility(
                    hasSuggestions ? View.VISIBLE : View.GONE
            );
            binding.quantitySuggestionsGroup.removeAllViews();

            if (!hasSuggestions) {
                return;
            }

            if (observedQuantity == null) {
                binding.quantitySuggestionsTitle.setText(
                        R.string
                                .reference_list_review_quantity_unknown_title
                );
            } else {
                binding.quantitySuggestionsTitle.setText(
                        itemView.getContext().getString(
                                R.string
                                        .reference_list_review_quantity_suggestions_title,
                                observedQuantity
                        )
                );
            }

            if (observedQuantity != null) {
                addQuantitySuggestionChip(proposal, observedQuantity);
            }

            for (Integer suggestion : suggestions) {
                addQuantitySuggestionChip(proposal, suggestion);
            }
        }

        private void addQuantitySuggestionChip(
                ReferenceProposal proposal,
                int quantity
        ) {
            Chip chip = new Chip(itemView.getContext());

            chip.setText(String.valueOf(quantity));
            chip.setCheckable(false);
            chip.setClickable(true);
            chip.setContentDescription(
                    itemView.getContext().getString(
                            R.string
                                    .reference_list_review_quantity_suggestion_description,
                            quantity
                    )
            );
            chip.setOnClickListener(
                    ignored -> listener.onQuantitySuggestion(
                            proposal,
                            quantity
                    )
            );

            binding.quantitySuggestionsGroup.addView(chip);
        }
    }

    private static final DiffUtil
            .ItemCallback<ReferenceProposal>
            DIFF_CALLBACK =
            new DiffUtil
                    .ItemCallback<ReferenceProposal>() {

                @Override
                public boolean areItemsTheSame(
                        @NonNull ReferenceProposal oldItem,
                        @NonNull ReferenceProposal newItem
                ) {
                    return oldItem.getId()
                            == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull ReferenceProposal oldItem,
                        @NonNull ReferenceProposal newItem
                ) {
                    return oldItem
                            .getReference()
                            .equals(
                                    newItem.getReference()
                            )
                            && Objects.equals(
                            oldItem.getSourceRawText(),
                            newItem.getSourceRawText()
                    )
                            && oldItem.isManuallyAdded()
                            == newItem.isManuallyAdded()
                            && oldItem.requiresCorrection()
                            == newItem.requiresCorrection()
                            && oldItem.getMatchStatus()
                            .equals(
                                    newItem.getMatchStatus()
                            )
                            && oldItem.getReviewState()
                            .equals(newItem.getReviewState())
                            && oldItem.getSuggestions()
                            .equals(
                                    newItem.getSuggestions()
                            )
                            && Objects.equals(
                            oldItem.getDocumentData().getQuantity(),
                            newItem.getDocumentData().getQuantity()
                    )
                            && Objects.equals(
                            oldItem.getDocumentData().getUnit(),
                            newItem.getDocumentData().getUnit()
                    )
                            && oldItem.getDocumentData()
                            .getQuantitySuggestions()
                            .equals(
                                    newItem.getDocumentData()
                                            .getQuantitySuggestions()
                            )
                            && oldItem.getDocumentData()
                            .getDestinations()
                            .equals(
                                    newItem.getDocumentData()
                                            .getDestinations()
                            )
                            && oldItem.getDocumentData()
                            .getAllocations()
                            .equals(
                                    newItem.getDocumentData()
                                            .getAllocations()
                            );
                }
            };
}
