package com.rndymi.almacentracker.feature.inventory.form;

import java.util.Objects;

public final class WarehouseItemFormUiState {

    private final WarehouseItemFormMode mode;
    private final long warehouseItemId;

    private final String category;
    private final String code;
    private final String site;
    private final String position;
    private final String observations;

    private final String categoryError;
    private final String codeError;
    private final String siteError;
    private final String generalError;

    private final boolean loading;
    private final boolean saving;
    private final boolean notFound;
    private final boolean invalidId;

    public WarehouseItemFormUiState(
            WarehouseItemFormMode mode,
            long warehouseItemId,
            String category,
            String code,
            String site,
            String position,
            String observations,
            String categoryError,
            String codeError,
            String siteError,
            String generalError,
            boolean loading,
            boolean saving,
            boolean notFound,
            boolean invalidId
    ) {
        this.mode = Objects.requireNonNull(mode);

        int activeLifecycleStates =
                (loading ? 1 : 0)
                        + (saving ? 1 : 0)
                        + (notFound ? 1 : 0)
                        + (invalidId ? 1 : 0);

        if (activeLifecycleStates > 1) {
            throw new IllegalArgumentException(
                    "Form lifecycle states are mutually exclusive"
            );
        }

        if (mode == WarehouseItemFormMode.CREATE) {
            if (warehouseItemId != 0L) {
                throw new IllegalArgumentException(
                        "Create mode cannot contain an item ID"
                );
            }

            if (loading || notFound || invalidId) {
                throw new IllegalArgumentException(
                        "Create mode cannot load an existing item"
                );
            }
        } else if (invalidId) {
            if (warehouseItemId > 0L) {
                throw new IllegalArgumentException(
                        "Invalid-ID state requires an invalid ID"
                );
            }
        } else if (warehouseItemId <= 0L) {
            throw new IllegalArgumentException(
                    "Edit mode requires a positive item ID"
            );
        }

        this.category = Objects.requireNonNull(category);
        this.code = Objects.requireNonNull(code);
        this.site = Objects.requireNonNull(site);
        this.warehouseItemId = warehouseItemId;
        this.position = position;
        this.observations = observations;
        this.categoryError = categoryError;
        this.codeError = codeError;
        this.siteError = siteError;
        this.generalError = generalError;
        this.loading = loading;
        this.saving = saving;
        this.notFound = notFound;
        this.invalidId = invalidId;
    }

    public static WarehouseItemFormUiState createMode() {
        return new WarehouseItemFormUiState(
                WarehouseItemFormMode.CREATE,
                0L,
                "",
                "",
                "",
                "",
                "",
                null,
                null,
                null,
                null,
                false,
                false,
                false,
                false
        );
    }

    public static WarehouseItemFormUiState editLoading(
            long warehouseItemId
    ) {
        return new WarehouseItemFormUiState(
                WarehouseItemFormMode.EDIT,
                warehouseItemId,
                "",
                "",
                "",
                "",
                "",
                null,
                null,
                null,
                null,
                true,
                false,
                false,
                false
        );
    }

    public WarehouseItemFormMode getMode() {
        return mode;
    }

    public long getWarehouseItemId() {
        return warehouseItemId;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getSite() {
        return site;
    }

    public String getPosition() {
        return position;
    }

    public String getObservations() {
        return observations;
    }

    public String getCategoryError() {
        return categoryError;
    }

    public String getCodeError() {
        return codeError;
    }

    public String getSiteError() {
        return siteError;
    }

    public String getGeneralError() {
        return generalError;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isSaving() {
        return saving;
    }

    public boolean isNotFound() {
        return notFound;
    }

    public boolean isInvalidId() {
        return invalidId;
    }

    public boolean isEditable() {
        return !loading
                && !saving
                && !notFound
                && !invalidId;
    }
}
