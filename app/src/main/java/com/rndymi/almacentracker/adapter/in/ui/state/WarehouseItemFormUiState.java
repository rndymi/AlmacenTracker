package com.rndymi.almacentracker.adapter.in.ui.state;

public final class WarehouseItemFormUiState {

    private final String category;
    private final String code;
    private final String site;
    private final String position;
    private final String observations;

    private final String categoryError;
    private final String codeError;
    private final String siteError;
    private final String generalError;

    private final boolean saving;

    public WarehouseItemFormUiState(
            String category,
            String code,
            String site,
            String position,
            String observations,
            String categoryError,
            String codeError,
            String siteError,
            String generalError,
            boolean saving
    ) {
        this.category = category;
        this.code = code;
        this.site = site;
        this.position = position;
        this.observations = observations;
        this.categoryError = categoryError;
        this.codeError = codeError;
        this.siteError = siteError;
        this.generalError = generalError;
        this.saving = saving;
    }

    public static WarehouseItemFormUiState initial() {
        return new WarehouseItemFormUiState(
                "",
                "",
                "",
                "",
                "",
                null,
                null,
                null,
                null,
                false
        );
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

    public boolean isSaving() {
        return saving;
    }
}