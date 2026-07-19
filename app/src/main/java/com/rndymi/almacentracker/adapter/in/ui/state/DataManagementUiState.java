package com.rndymi.almacentracker.adapter.in.ui.state;

public final class DataManagementUiState {

    public enum Status {
        IDLE,
        SELECTING_DESTINATION,
        SELECTING_BACKUP_DESTINATION,
        SELECTING_SOURCE,
        EXPORTING,
        PREPARING_SHARE,
        IMPORTING,
        CREATING_BACKUP,
        EMPTY_DATABASE,
        ERROR
    }

    private final Status status;
    private final String message;

    private DataManagementUiState(
            Status status,
            String message
    ) {
        this.status = status;
        this.message = message;
    }

    public static DataManagementUiState idle() {
        return new DataManagementUiState(
                Status.IDLE,
                null
        );
    }

    public static DataManagementUiState
    selectingDestination() {
        return new DataManagementUiState(
                Status.SELECTING_DESTINATION,
                null
        );
    }

    public static DataManagementUiState
    selectingSource() {
        return new DataManagementUiState(
                Status.SELECTING_SOURCE,
                null
        );
    }

    public static DataManagementUiState exporting() {
        return new DataManagementUiState(
                Status.EXPORTING,
                null
        );
    }

    public static DataManagementUiState
    preparingShare() {
        return new DataManagementUiState(
                Status.PREPARING_SHARE,
                null
        );
    }

    public static DataManagementUiState importing() {
        return new DataManagementUiState(
                Status.IMPORTING,
                null
        );
    }

    public static DataManagementUiState empty(
            String message
    ) {
        return new DataManagementUiState(
                Status.EMPTY_DATABASE,
                message
        );
    }

    public static DataManagementUiState error(
            String message
    ) {
        return new DataManagementUiState(
                Status.ERROR,
                message
        );
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public static DataManagementUiState
    selectingBackupDestination() {
        return new DataManagementUiState(
                Status.SELECTING_BACKUP_DESTINATION,
                null
        );
    }

    public static DataManagementUiState creatingBackup() {
        return new DataManagementUiState(
                Status.CREATING_BACKUP,
                null
        );
    }
}