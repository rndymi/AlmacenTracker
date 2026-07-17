package com.rndymi.almacentracker.adapter.in.ui.state;

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
        this.mode = mode;
        this.warehouseItemId = warehouseItemId;
        this.category = category;
        this.code = code;
        this.site = site;
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