package com.rndymi.almacentracker.feature.withdrawal_history.create;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.databinding.ItemWithdrawalHistoryDraftEntryBinding;
import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;
import com.rndymi.almacentracker.feature.inventory.common.SimpleTextWatcher;

public final class WithdrawalHistoryCreateAdapter
        extends ListAdapter<
        WithdrawalHistoryDraftEntryUiModel,
        WithdrawalHistoryCreateAdapter.ViewHolder> {

    public interface Listener {

        void onQuantityChanged(
                long stableId,
                String value
        );

        void onUnitChanged(
                long stableId,
                String value
        );

        void onStoresChanged(long stableId, String value);
    }

    private final Listener listener;

    public WithdrawalHistoryCreateAdapter(
            Listener listener
    ) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(
            int position
    ) {
        return getItem(position).getStableId();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        return new ViewHolder(
                ItemWithdrawalHistoryDraftEntryBinding
                        .inflate(
                                LayoutInflater.from(
                                        parent.getContext()
                                ),
                                parent,
                                false
                        ),
                listener
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        holder.bind(getItem(position));
    }

    private static final DiffUtil.ItemCallback<
            WithdrawalHistoryDraftEntryUiModel>
            DIFF_CALLBACK =
            new DiffUtil.ItemCallback<
                    WithdrawalHistoryDraftEntryUiModel>() {

                @Override
                public boolean areItemsTheSame(
                        @NonNull
                        WithdrawalHistoryDraftEntryUiModel oldItem,
                        @NonNull
                        WithdrawalHistoryDraftEntryUiModel newItem
                ) {
                    return oldItem.getStableId()
                            == newItem.getStableId();
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull
                        WithdrawalHistoryDraftEntryUiModel oldItem,
                        @NonNull
                        WithdrawalHistoryDraftEntryUiModel newItem
                ) {
                    return equalsNullable(
                            oldItem.getQuantityText(),
                            newItem.getQuantityText()
                    ) && equalsNullable(
                            oldItem.getUnitText(),
                            newItem.getUnitText()
                    ) && equalsNullable(
                            oldItem.getQuantityError(),
                            newItem.getQuantityError()
                    )
                            && equalsNullable(
                            oldItem.getUnitError(),
                            newItem.getUnitError()
                    ) && equalsNullable(
                            oldItem.getStoresText(),
                            newItem.getStoresText()
                    ) && equalsNullable(
                            oldItem.getStoresError(),
                            newItem.getStoresError()
                    )
                            && oldItem.getDestinations()
                            .equals(
                                    newItem.getDestinations()
                            );
                }
            };

    private static boolean equalsNullable(
            Object first,
            Object second
    ) {
        return first == null
                ? second == null
                : first.equals(second);
    }

    static final class ViewHolder
            extends RecyclerView.ViewHolder {

        private final
        ItemWithdrawalHistoryDraftEntryBinding binding;

        private final Listener listener;

        private long stableId;
        private boolean bindingValues;

        ViewHolder(
                ItemWithdrawalHistoryDraftEntryBinding binding,
                Listener listener
        ) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;

            binding.quantityEditText
                    .addTextChangedListener(
                            SimpleTextWatcher.afterTextChanged(
                                    value -> {
                                        if (!bindingValues) {
                                            listener.onQuantityChanged(
                                                    stableId,
                                                    value
                                            );
                                        }
                                    }
                            )
                    );

            binding.unitEditText
                    .addTextChangedListener(
                            SimpleTextWatcher.afterTextChanged(
                                    value -> {
                                        if (!bindingValues) {
                                            listener.onUnitChanged(
                                                    stableId,
                                                    value
                                            );
                                        }
                                    }
                            )
                    );

            binding.storesEditText.addTextChangedListener(
                    SimpleTextWatcher.afterTextChanged(
                            value -> {
                                if (!bindingValues) {
                                    listener.onStoresChanged(stableId, value);
                                }
                            }
                    )
            );
        }

        void bind(
                WithdrawalHistoryDraftEntryUiModel model
        ) {
            stableId = model.getStableId();
            bindingValues = true;

            binding.referenceText.setText(
                    binding.getRoot()
                            .getContext()
                            .getString(
                                    R.string
                                            .withdrawal_history_reference_value,
                                    model.getCategory(),
                                    model.getCode()
                            )
            );

            boolean found =
                    model.getLocationStatus()
                            == WithdrawalLocationStatus.FOUND;

            binding.locationText.setText(
                    found
                            ? locationText(model)
                            : binding.getRoot()
                            .getContext()
                            .getString(
                                    R.string
                                    .withdrawal_history_not_found
                            )
            );

            if (!binding.quantityEditText.hasFocus()
                    && !binding.quantityEditText
                    .getText()
                    .toString()
                    .equals(
                            model.getQuantityText()
                    )) {
                binding.quantityEditText.setText(
                        model.getQuantityText()
                );
            }

            if (!binding.unitEditText.hasFocus()
                    && !binding.unitEditText
                    .getText()
                    .toString()
                    .equals(
                            model.getUnitText()
                    )) {
                binding.unitEditText.setText(
                        model.getUnitText()
                );
            }

            if (!binding.storesEditText.hasFocus()
                    && !binding.storesEditText.getText().toString()
                    .equals(model.getStoresText())) {
                binding.storesEditText.setText(model.getStoresText());
            }

            binding.quantityInputLayout.setError(
                    model.getQuantityError()
            );

            binding.unitInputLayout.setError(
                    model.getUnitError()
            );

            binding.storesInputLayout.setError(model.getStoresError());

            bindingValues = false;
        }

        private String locationText(
                WithdrawalHistoryDraftEntryUiModel model
        ) {
            if (model.getPositionSnapshot() == null
                    || model.getPositionSnapshot()
                    .trim()
                    .isEmpty()) {
                return binding.getRoot()
                        .getContext()
                        .getString(
                                R.string
                                        .withdrawal_history_site_only,
                                model.getSiteSnapshot()
                        );
            }

            return binding.getRoot()
                    .getContext()
                    .getString(
                            R.string
                                    .withdrawal_history_site_position,
                            model.getSiteSnapshot(),
                            model.getPositionSnapshot()
                    );
        }
    }
}
