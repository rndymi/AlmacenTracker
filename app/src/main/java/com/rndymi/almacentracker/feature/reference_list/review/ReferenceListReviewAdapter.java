package com.rndymi.almacentracker.feature.reference_list.review;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.databinding
        .ItemReferenceProposalBinding;

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
                            == newItem.isManuallyAdded();
                }
            };
}