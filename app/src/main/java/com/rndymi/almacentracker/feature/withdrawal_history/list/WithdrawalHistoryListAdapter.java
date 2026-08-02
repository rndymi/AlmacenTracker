package com.rndymi.almacentracker.feature.withdrawal_history.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.databinding.ItemWithdrawalHistorySummaryBinding;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySummary;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public final class WithdrawalHistoryListAdapter
        extends ListAdapter<
        WithdrawalHistorySummary,
        WithdrawalHistoryListAdapter
                .HistoryViewHolder
        > {

    private static final DiffUtil.ItemCallback<
            WithdrawalHistorySummary
            > DIFF_CALLBACK =
            new DiffUtil.ItemCallback<
                    WithdrawalHistorySummary
                    >() {

                @Override
                public boolean areItemsTheSame(
                        @NonNull
                        WithdrawalHistorySummary oldItem,
                        @NonNull
                        WithdrawalHistorySummary newItem
                ) {
                    return oldItem.getId()
                            == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull
                        WithdrawalHistorySummary oldItem,
                        @NonNull
                        WithdrawalHistorySummary newItem
                ) {
                    return oldItem.equals(newItem);
                }
            };

    public WithdrawalHistoryListAdapter() {
        super(DIFF_CALLBACK);

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemWithdrawalHistorySummaryBinding binding =
                ItemWithdrawalHistorySummaryBinding.inflate(
                        LayoutInflater.from(
                                parent.getContext()
                        ),
                        parent,
                        false
                );

        return new HistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull HistoryViewHolder holder,
            int position
    ) {
        holder.bind(getItem(position));
    }

    static final class HistoryViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemWithdrawalHistorySummaryBinding
                binding;

        HistoryViewHolder(
                ItemWithdrawalHistorySummaryBinding binding
        ) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(
                WithdrawalHistorySummary summary
        ) {
            Context context =
                    binding.getRoot().getContext();

            String title =
                    summary.hasTitle()
                            ? summary.getTitle()
                            : context.getString(
                            R.string
                            .withdrawal_history_untitled
                    );

            String date =
                    formatDate(
                            summary.getRegisteredAt(),
                            context
                    );

            String counters =
                    formatCounters(
                            context,
                            summary
                    );

            binding.historyTitleText.setText(title);
            binding.historyDateText.setText(date);
            binding.historySummaryText.setText(counters);

            binding.getRoot().setClickable(false);
            binding.getRoot().setFocusable(false);
            binding.getRoot().setOnClickListener(null);

            binding.getRoot().setContentDescription(
                    context.getString(
                            R.string
                                    .withdrawal_history_item_description,
                            title,
                            date,
                            counters
                    )
            );
        }

        private static String formatDate(
                long timestamp,
                Context context
        ) {
            Locale locale =
                    context.getResources()
                            .getConfiguration()
                            .getLocales()
                            .get(0);

            DateTimeFormatter formatter =
                    DateTimeFormatter
                            .ofLocalizedDateTime(
                                    FormatStyle.MEDIUM,
                                    FormatStyle.SHORT
                            )
                            .withLocale(locale)
                            .withZone(
                                    ZoneId.systemDefault()
                            );

            return formatter.format(
                    Instant.ofEpochMilli(timestamp)
            );
        }

        private static String formatCounters(
                Context context,
                WithdrawalHistorySummary summary
        ) {
            String total =
                    context.getResources()
                            .getQuantityString(
                                    R.plurals
                                            .withdrawal_history_reference_count,
                                    summary.getEntryCount(),
                                    summary.getEntryCount()
                            );

            if (summary.getNotFoundCount() == 0) {
                String found =
                        context.getResources()
                                .getQuantityString(
                                        R.plurals
                                                .withdrawal_history_found_count,
                                        summary.getFoundCount(),
                                        summary.getFoundCount()
                                );

                return context.getString(
                        R.string
                                .withdrawal_history_summary_without_missing,
                        total,
                        found
                );
            }

            if (summary.getFoundCount() == 0) {
                String notFound =
                        context.getResources()
                                .getQuantityString(
                                        R.plurals
                                                .withdrawal_history_not_found_count,
                                        summary.getNotFoundCount(),
                                        summary.getNotFoundCount()
                                );

                return context.getString(
                        R.string
                                .withdrawal_history_summary_without_found,
                        total,
                        notFound
                );
            }

            String found =
                    context.getResources()
                            .getQuantityString(
                                    R.plurals
                                            .withdrawal_history_found_count,
                                    summary.getFoundCount(),
                                    summary.getFoundCount()
                            );

            String notFound =
                    context.getResources()
                            .getQuantityString(
                                    R.plurals
                                            .withdrawal_history_not_found_count,
                                    summary.getNotFoundCount(),
                                    summary.getNotFoundCount()
                            );

            return context.getString(
                    R.string
                            .withdrawal_history_summary_complete,
                    total,
                    found,
                    notFound
            );
        }
    }
}
