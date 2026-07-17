package com.rndymi.almacentracker.adapter.in.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.rndymi.almacentracker.AlmacenTrackerApplication;
import com.rndymi.almacentracker.R;
import com.rndymi.almacentracker.adapter.in.ui.formatter.WarehouseItemDateFormatter;
import com.rndymi.almacentracker.adapter.in.ui.state.WarehouseItemDetailUiState;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemDetailViewModel;
import com.rndymi.almacentracker.adapter.in.ui.viewmodel.WarehouseItemDetailViewModelFactory;
import com.rndymi.almacentracker.databinding.ActivityItemDetailBinding;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

public final class ItemDetailActivity
        extends AppCompatActivity {

    public static final String EXTRA_WAREHOUSE_ITEM_ID =
            "com.rndymi.almacentracker.extra.WAREHOUSE_ITEM_ID";

    private static final String TAG =
            "ItemDetailActivity";

    private static final long INVALID_WAREHOUSE_ITEM_ID =
            -1L;

    private ActivityItemDetailBinding binding;
    private long currentWarehouseItemId =
            INVALID_WAREHOUSE_ITEM_ID;

    private final WarehouseItemDateFormatter dateFormatter =
            new WarehouseItemDateFormatter();

    public static Intent createIntent(
            Context context,
            long warehouseItemId
    ) {
        Intent intent =
                new Intent(context, ItemDetailActivity.class);

        intent.putExtra(
                EXTRA_WAREHOUSE_ITEM_ID,
                warehouseItemId
        );

        return intent;
    }

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        binding = ActivityItemDetailBinding.inflate(
                getLayoutInflater()
        );

        setContentView(binding.getRoot());

        configureToolbar();

        currentWarehouseItemId = readWarehouseItemId();

        configureActions();
        observeUiState(currentWarehouseItemId);
    }

    private void configureToolbar() {
        setSupportActionBar(binding.toolbar);

        binding.toolbar.setNavigationOnClickListener(
                ignored -> getOnBackPressedDispatcher()
                        .onBackPressed()
        );
    }

    private long readWarehouseItemId() {
        return getIntent().getLongExtra(
                EXTRA_WAREHOUSE_ITEM_ID,
                INVALID_WAREHOUSE_ITEM_ID
        );
    }

    private void observeUiState(long warehouseItemId) {
        AlmacenTrackerApplication application =
                (AlmacenTrackerApplication) getApplication();

        WarehouseItemDetailViewModelFactory factory =
                application
                        .getAppContainer()
                        .provideWarehouseItemDetailViewModelFactory(
                                warehouseItemId
                        );

        WarehouseItemDetailViewModel viewModel =
                new ViewModelProvider(this, factory)
                        .get(
                                WarehouseItemDetailViewModel.class
                        );

        viewModel.getUiState().observe(
                this,
                this::render
        );
    }

    private void render(
            WarehouseItemDetailUiState state
    ) {
        hideAllStates();
        binding.editButton.setEnabled(false);

        switch (state.getStatus()) {
            case LOADING:
                binding.loadingProgress.setVisibility(
                        View.VISIBLE
                );
                break;

            case CONTENT:
                renderContent(state.getWarehouseItem());
                break;

            case NOT_FOUND:
                showStateMessage(
                        R.string.warehouse_detail_not_found
                );
                break;

            case INVALID_ID:
                showStateMessage(
                        R.string.warehouse_detail_invalid_id
                );
                break;

            case ERROR:
                binding.stateMessageText.setText(
                        state.getErrorMessage()
                );

                binding.stateMessageText.setVisibility(
                        View.VISIBLE
                );

                Log.e(
                        TAG,
                        "Warehouse item detail could not be loaded"
                );
                break;
        }
    }

    private void renderContent(
            WarehouseItem warehouseItem
    ) {
        binding.identityText.setText(
                getString(
                        R.string.warehouse_identity_format,
                        warehouseItem.getCategory(),
                        warehouseItem.getCode()
                )
        );

        binding.categoryText.setText(
                warehouseItem.getCategory()
        );

        binding.codeText.setText(
                warehouseItem.getCode()
        );

        binding.siteText.setText(
                warehouseItem.getSite()
        );

        renderOptionalSection(
                binding.positionSection,
                binding.positionText,
                warehouseItem.getPosition()
        );

        renderOptionalSection(
                binding.observationsSection,
                binding.observationsText,
                warehouseItem.getObservations()
        );

        binding.createdAtText.setText(
                dateFormatter.format(
                        warehouseItem.getCreatedAt()
                )
        );

        binding.updatedAtText.setText(
                dateFormatter.format(
                        warehouseItem.getUpdatedAt()
                )
        );

        binding.contentScroll.setVisibility(
                View.VISIBLE
        );

        binding.editButton.setEnabled(true);
    }

    private void renderOptionalSection(
            View section,
            TextView valueView,
            String value
    ) {
        boolean hasValue =
                value != null && !value.trim().isEmpty();

        section.setVisibility(
                hasValue ? View.VISIBLE : View.GONE
        );

        if (hasValue) {
            valueView.setText(value);
        } else {
            valueView.setText(null);
        }
    }

    private void showStateMessage(int messageResId) {
        binding.stateMessageText.setText(messageResId);

        binding.stateMessageText.setVisibility(
                View.VISIBLE
        );
    }

    private void hideAllStates() {
        binding.loadingProgress.setVisibility(View.GONE);
        binding.contentScroll.setVisibility(View.GONE);
        binding.stateMessageText.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void configureActions() {
        binding.editButton.setOnClickListener(
                ignored -> {
                    if (currentWarehouseItemId
                            <= INVALID_WAREHOUSE_ITEM_ID) {
                        return;
                    }

                    startActivity(
                            ItemFormActivity.createEditIntent(
                                    this,
                                    currentWarehouseItemId
                            )
                    );
                }
        );
    }
}
