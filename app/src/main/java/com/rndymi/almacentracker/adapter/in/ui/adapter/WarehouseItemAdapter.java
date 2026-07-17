package com.rndymi.almacentracker.adapter.in.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.databinding.ItemWarehouseBinding;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class WarehouseItemAdapter
        extends ListAdapter<
        WarehouseItem,
        WarehouseItemAdapter.WarehouseItemViewHolder
        > {

    public interface WarehouseItemInteractionListener {

        void onWarehouseItemClick(
                long warehouseItemId
        );

        void onWarehouseItemLongClick(
                long warehouseItemId
        );
    }

    private static final DiffUtil.ItemCallback<WarehouseItem>
            DIFF_CALLBACK =
            new DiffUtil.ItemCallback<WarehouseItem>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull WarehouseItem oldItem,
                        @NonNull WarehouseItem newItem
                ) {
                    return oldItem.getId()
                            == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull WarehouseItem oldItem,
                        @NonNull WarehouseItem newItem
                ) {
                    return oldItem.getId()
                            == newItem.getId()
                            && oldItem.getCategory().equals(
                            newItem.getCategory()
                    )
                            && oldItem.getCode().equals(
                            newItem.getCode()
                    )
                            && oldItem.getSite().equals(
                            newItem.getSite()
                    )
                            && equalsNullable(
                            oldItem.getPosition(),
                            newItem.getPosition()
                    )
                            && oldItem.getUpdatedAt()
                            == newItem.getUpdatedAt();
                }
            };

    private final WarehouseItemInteractionListener
            interactionListener;

    private Set<Long> selectedIds =
            Collections.emptySet();

    public WarehouseItemAdapter(
            WarehouseItemInteractionListener
                    interactionListener
    ) {
        super(DIFF_CALLBACK);

        this.interactionListener =
                interactionListener;
    }

    public void setSelectedIds(
            Set<Long> selectedIds
    ) {
        this.selectedIds =
                selectedIds == null
                        ? Collections.emptySet()
                        : Collections.unmodifiableSet(
                        new LinkedHashSet<>(
                                selectedIds
                        )
                );

        notifyItemRangeChanged(
                0,
                getItemCount()
        );
    }

    @NonNull
    @Override
    public WarehouseItemViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemWarehouseBinding binding =
                ItemWarehouseBinding.inflate(
                        LayoutInflater.from(
                                parent.getContext()
                        ),
                        parent,
                        false
                );

        return new WarehouseItemViewHolder(
                binding
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull WarehouseItemViewHolder holder,
            int position
    ) {
        WarehouseItem warehouseItem =
                getItem(position);

        holder.bind(
                warehouseItem,
                selectedIds.contains(
                        warehouseItem.getId()
                ),
                interactionListener
        );
    }

    private static boolean equalsNullable(
            String first,
            String second
    ) {
        if (first == null) {
            return second == null;
        }

        return first.equals(second);
    }

    static final class WarehouseItemViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemWarehouseBinding binding;

        WarehouseItemViewHolder(
                ItemWarehouseBinding binding
        ) {
            super(binding.getRoot());

            this.binding = binding;
        }

        void bind(
                WarehouseItem warehouseItem,
                boolean selected,
                WarehouseItemInteractionListener listener
        ) {
            binding.getRoot().setChecked(selected);
            binding.selectionIndicator.setVisibility(
                    selected
                            ? View.VISIBLE
                            : View.GONE
            );

            binding.getRoot().setContentDescription(
                    binding.getRoot()
                            .getContext()
                            .getString(
                                    selected
                                            ? R.string
                                              .warehouse_item_selected_description
                                            : R.string
                                              .warehouse_item_not_selected_description,
                                    warehouseItem.getCategory(),
                                    warehouseItem.getCode()
                            )
            );

            binding.getRoot().setOnClickListener(
                    ignored ->
                            listener.onWarehouseItemClick(
                                    warehouseItem.getId()
                            )
            );

            binding.getRoot().setOnLongClickListener(
                    ignored -> {
                        listener.onWarehouseItemLongClick(
                                warehouseItem.getId()
                        );
                        return true;
                    }
            );

            binding.identityText.setText(
                    binding.getRoot()
                            .getContext()
                            .getString(
                                    R.string
                                            .warehouse_identity_format,
                                    warehouseItem.getCategory(),
                                    warehouseItem.getCode()
                            )
            );

            if (warehouseItem.hasPosition()) {
                binding.locationText.setText(
                        binding.getRoot()
                                .getContext()
                                .getString(
                                        R.string
                                                .warehouse_site_position_format,
                                        warehouseItem.getSite(),
                                        warehouseItem.getPosition()
                                )
                );
            } else {
                binding.locationText.setText(
                        binding.getRoot()
                                .getContext()
                                .getString(
                                        R.string
                                                .warehouse_site_format,
                                        warehouseItem.getSite()
                                )
                );
            }
        }
    }
}