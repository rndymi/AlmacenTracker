package com.rndymi.almacentracker.feature.reference_list.review;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.databinding.ItemReferenceProposalBinding;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.chip.Chip;

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
        }

        private void bindMatchStatus(
                ReferenceProposal proposal
        ) {
            int textResource;
            int colorAttribute;

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
                    !suggestions.isEmpty();

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
                            );
                }
            };
}
