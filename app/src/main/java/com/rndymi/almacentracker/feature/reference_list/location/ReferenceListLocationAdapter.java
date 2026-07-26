package com.rndymi.almacentracker.feature.reference_list.location;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.databinding.ItemReferenceLocationBinding;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceLocation;

public final class ReferenceListLocationAdapter
        extends ListAdapter<
        WarehouseReferenceLocation,
        ReferenceListLocationAdapter.LocationViewHolder
        > {

    public interface Listener {

        void onLocationSelected(
                WarehouseReferenceLocation location
        );
    }

    private static final DiffUtil.ItemCallback<
            WarehouseReferenceLocation
            > DIFF_CALLBACK =
            new DiffUtil.ItemCallback<
                    WarehouseReferenceLocation
                    >() {

                @Override
                public boolean areItemsTheSame(
                        @NonNull
                        WarehouseReferenceLocation oldItem,
                        @NonNull
                        WarehouseReferenceLocation newItem
                ) {
                    return oldItem
                            .getReference()
                            .identityKey()
                            .equals(
                                    newItem
                                            .getReference()
                                            .identityKey()
                            );
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull
                        WarehouseReferenceLocation oldItem,
                        @NonNull
                        WarehouseReferenceLocation newItem
                ) {
                    return oldItem.getStatus()
                            == newItem.getStatus()
                            && oldItem.getWarehouseItemId()
                            == newItem.getWarehouseItemId()
                            && equalValues(
                            oldItem.getSite(),
                            newItem.getSite()
                    )
                            && equalValues(
                            oldItem.getPosition(),
                            newItem.getPosition()
                    );
                }
            };

    private final Listener listener;

    public ReferenceListLocationAdapter(
            Listener listener
    ) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemReferenceLocationBinding binding =
                ItemReferenceLocationBinding.inflate(
                        LayoutInflater.from(
                                parent.getContext()
                        ),
                        parent,
                        false
                );

        return new LocationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull LocationViewHolder holder,
            int position
    ) {
        holder.bind(
                getItem(position)
        );
    }

    private static boolean equalValues(
            String first,
            String second
    ) {
        if (first == null) {
            return second == null;
        }

        return first.equals(second);
    }

    final class LocationViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemReferenceLocationBinding
                binding;

        LocationViewHolder(
                ItemReferenceLocationBinding binding
        ) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(
                WarehouseReferenceLocation location
        ) {
            String referenceValue =
                    location
                            .getReference()
                            .displayValue();

            binding.referenceText.setText(
                    referenceValue
            );

            if (location.isFound()) {
                binding.statusText.setText(
                        formatLocation(location)
                );

                binding.statusText.setTextAppearance(
                        R.style.TextAppearance_Material3_BodyMedium
                );

                binding.openDetailIcon.setVisibility(
                        View.VISIBLE
                );

                binding.getRoot().setClickable(true);
                binding.getRoot().setFocusable(true);

                binding.getRoot()
                        .setContentDescription(
                                binding.getRoot()
                                        .getContext()
                                        .getString(
                                                R.string
                                                        .reference_list_location_found_description,
                                                referenceValue,
                                                formatLocation(
                                                        location
                                                )
                                        )
                        );

                binding.getRoot()
                        .setOnClickListener(
                                ignored ->
                                        listener
                                                .onLocationSelected(
                                                        location
                                                )
                        );
            } else {
                binding.statusText.setText(
                        R.string
                                .reference_list_location_not_found
                );

                binding.openDetailIcon.setVisibility(
                        View.GONE
                );

                binding.getRoot().setClickable(false);
                binding.getRoot().setFocusable(false);
                binding.getRoot().setOnClickListener(null);

                binding.getRoot()
                        .setContentDescription(
                                binding.getRoot()
                                        .getContext()
                                        .getString(
                                                R.string
                                                        .reference_list_location_not_found_description,
                                                referenceValue
                                        )
                        );
            }
        }

        private String formatLocation(
                WarehouseReferenceLocation location
        ) {
            if (!location.hasPosition()) {
                return binding.getRoot()
                        .getContext()
                        .getString(
                                R.string
                                        .reference_list_location_site_format,
                                location.getSite()
                        );
            }

            return binding.getRoot()
                    .getContext()
                    .getString(
                            R.string
                                    .reference_list_location_site_position_format,
                            location.getSite(),
                            location.getPosition()
                    );
        }
    }
}