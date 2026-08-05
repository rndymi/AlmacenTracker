package com.rndymi.almacentracker.feature.withdrawal_history.detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.databinding.ItemWithdrawalHistoryDetailEntryBinding;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryEntry;
import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;

public final class WithdrawalHistoryDetailAdapter
        extends ListAdapter<
        WithdrawalHistoryEntry,
        WithdrawalHistoryDetailAdapter.EntryViewHolder
        > {

    private static final DiffUtil.ItemCallback<
            WithdrawalHistoryEntry
            > DIFF_CALLBACK =
            new DiffUtil.ItemCallback<
                    WithdrawalHistoryEntry
                    >() {

                @Override
                public boolean areItemsTheSame(
                        @NonNull WithdrawalHistoryEntry oldItem,
                        @NonNull WithdrawalHistoryEntry newItem
                ) {
                    if (oldItem.getId() > 0L
                            && newItem.getId() > 0L) {
                        return oldItem.getId()
                                == newItem.getId();
                    }

                    return oldItem.getOrderIndex()
                            == newItem.getOrderIndex();
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull WithdrawalHistoryEntry oldItem,
                        @NonNull WithdrawalHistoryEntry newItem
                ) {
                    return oldItem.getId()
                            == newItem.getId()
                            && oldItem.getHistoryId()
                            == newItem.getHistoryId()
                            && oldItem.getOrderIndex()
                            == newItem.getOrderIndex()
                            && valuesEqual(
                            oldItem.getCategory(),
                            newItem.getCategory()
                    )
                            && valuesEqual(
                            oldItem.getCode(),
                            newItem.getCode()
                    )
                            && valuesEqual(
                            oldItem.getQuantity(),
                            newItem.getQuantity()
                    )
                            && valuesEqual(
                            oldItem.getUnit(),
                            newItem.getUnit()
                    )
                            && valuesEqual(
                            oldItem.getDestinations(),
                            newItem.getDestinations()
                    )
                            && valuesEqual(
                            oldItem.getWarehouseItemIdSnapshot(),
                            newItem.getWarehouseItemIdSnapshot()
                    )
                            && valuesEqual(
                            oldItem.getSiteSnapshot(),
                            newItem.getSiteSnapshot()
                    )
                            && valuesEqual(
                            oldItem.getPositionSnapshot(),
                            newItem.getPositionSnapshot()
                    )
                            && valuesEqual(
                            oldItem.getLocationStatus(),
                            newItem.getLocationStatus()
                    );
                }

                private boolean valuesEqual(
                        Object first,
                        Object second
                ) {
                    return first == null
                            ? second == null
                            : first.equals(second);
                }
            };

    public WithdrawalHistoryDetailAdapter() {
        super(DIFF_CALLBACK);

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        WithdrawalHistoryEntry entry =
                getItem(position);

        return entry.getId() > 0L
                ? entry.getId()
                : entry.getOrderIndex();
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemWithdrawalHistoryDetailEntryBinding binding =
                ItemWithdrawalHistoryDetailEntryBinding
                        .inflate(
                                LayoutInflater.from(
                                        parent.getContext()
                                ),
                                parent,
                                false
                        );

        return new EntryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull EntryViewHolder holder,
            int position
    ) {
        holder.bind(getItem(position));
    }

    static final class EntryViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemWithdrawalHistoryDetailEntryBinding
                binding;

        EntryViewHolder(
                ItemWithdrawalHistoryDetailEntryBinding binding
        ) {
            super(binding.getRoot());

            this.binding = binding;
        }

        void bind(WithdrawalHistoryEntry entry) {
            Context context =
                    binding.getRoot().getContext();

            String reference =
                    context.getString(
                            R.string
                                    .withdrawal_history_detail_reference,
                            entry.getCategory(),
                            entry.getCode()
                    );

            binding.referenceText.setText(reference);

            bindDocumentData(context, entry);
            bindDestinations(context, entry);
            bindHistoricalLocation(context, entry);

            binding.getRoot().setContentDescription(
                    buildContentDescription(
                            context,
                            entry,
                            reference
                    )
            );
        }

        private void bindDestinations(
                Context context,
                WithdrawalHistoryEntry entry
        ) {
            boolean hasDestinations =
                    !entry.getDestinations().isEmpty();

            binding.destinationsText.setVisibility(
                    hasDestinations
                            ? View.VISIBLE
                            : View.GONE
            );
            binding.destinationsText.setText(
                    hasDestinations
                            ? context.getString(
                            R.string.withdrawal_history_destinations,
                            String.join(
                                    ", ",
                                    entry.getDestinations()
                            )
                    )
                            : ""
            );
        }

        private void bindDocumentData(
                Context context,
                WithdrawalHistoryEntry entry
        ) {
            binding.documentDataText.setText(null);
            binding.documentDataText.setVisibility(
                    View.GONE
            );

            if (!entry.hasQuantity()) {
                return;
            }

            String documentData;

            if (entry.hasUnit()) {
                documentData =
                        context.getString(
                                R.string
                                        .withdrawal_history_detail_quantity_with_unit,
                                entry.getQuantity(),
                                entry.getUnit()
                        );
            } else {
                documentData =
                        context.getString(
                                R.string
                                        .withdrawal_history_detail_quantity_only,
                                entry.getQuantity()
                        );
            }

            binding.documentDataText.setText(documentData);
            binding.documentDataText.setVisibility(
                    View.VISIBLE
            );
        }

        private void bindHistoricalLocation(
                Context context,
                WithdrawalHistoryEntry entry
        ) {
            binding.historicalLocationText.setText(null);

            if (entry.getLocationStatus()
                    == WithdrawalLocationStatus.NOT_FOUND) {
                binding.historicalLocationText.setText(
                        R.string
                                .withdrawal_history_detail_not_found_location
                );
                return;
            }

            if (entry.hasPositionSnapshot()) {
                binding.historicalLocationText.setText(
                        context.getString(
                                R.string
                                        .withdrawal_history_detail_site_and_position,
                                entry.getSiteSnapshot(),
                                entry.getPositionSnapshot()
                        )
                );
                return;
            }

            binding.historicalLocationText.setText(
                    context.getString(
                            R.string
                                    .withdrawal_history_detail_site_only,
                            entry.getSiteSnapshot()
                    )
            );
        }

        private String buildContentDescription(
                Context context,
                WithdrawalHistoryEntry entry,
                String reference
        ) {
            String documentData =
                    entry.hasQuantity()
                            ? binding.documentDataText
                            .getText()
                            .toString()
                            : context.getString(
                            R.string
                            .withdrawal_history_detail_without_quantity
                    );

            if (!entry.getDestinations().isEmpty()) {
                documentData = documentData
                        + ". "
                        + binding.destinationsText
                        .getText()
                        .toString();
            }

            String location =
                    binding.historicalLocationText
                            .getText()
                            .toString();

            return context.getString(
                    R.string
                            .withdrawal_history_detail_entry_description,
                    reference,
                    documentData,
                    location
            );
        }
    }
}
