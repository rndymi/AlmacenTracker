package com.rndymi.almacentracker.adapter.in.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.databinding.ItemWarehouseBinding;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

public final class WarehouseItemAdapter
        extends ListAdapter<
        WarehouseItem,
        WarehouseItemAdapter.WarehouseItemViewHolder
        > {
    public interface OnWarehouseItemClickListener {
        void onWarehouseItemClick(long warehouseItemId);
    }
    private static final DiffUtil.ItemCallback<WarehouseItem>
            DIFF_CALLBACK =
            new DiffUtil.ItemCallback<WarehouseItem>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull WarehouseItem oldItem,
                        @NonNull WarehouseItem newItem
                ) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull WarehouseItem oldItem,
                        @NonNull WarehouseItem newItem
                ) {
                    return oldItem.getId() == newItem.getId()
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
    private final OnWarehouseItemClickListener clickListener;
    public WarehouseItemAdapter(
            OnWarehouseItemClickListener clickListener
    ) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public WarehouseItemViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemWarehouseBinding binding =
                ItemWarehouseBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );
        return new WarehouseItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull WarehouseItemViewHolder holder,
            int position
    ) {
        holder.bind(
                getItem(position),
                clickListener
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
                OnWarehouseItemClickListener clickListener
        ) {
            binding.getRoot().setOnClickListener(
                    ignored -> clickListener.onWarehouseItemClick(
                            warehouseItem.getId()
                    )
            );

            binding.identityText.setText(
                    binding.getRoot().getContext().getString(
                            R.string.warehouse_identity_format,
                            warehouseItem.getCategory(),
                            warehouseItem.getCode()
                    )
            );

            if (warehouseItem.hasPosition()) {
                binding.locationText.setText(
                        binding.getRoot().getContext().getString(
                                R.string.warehouse_site_position_format,
                                warehouseItem.getSite(),
                                warehouseItem.getPosition()
                        )
                );
            } else {
                binding.locationText.setText(
                        binding.getRoot().getContext().getString(
                                R.string.warehouse_site_format,
                                warehouseItem.getSite()
                        )
                );
            }
        }
    }
}