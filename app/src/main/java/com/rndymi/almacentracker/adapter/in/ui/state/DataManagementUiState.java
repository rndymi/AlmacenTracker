package com.rndymi.almacentracker.adapter.in.ui.state;

public final class DataManagementUiState {

    public enum Status {
        IDLE,
        SELECTING_DESTINATION,
        SELECTING_BACKUP_DESTINATION,
        SELECTING_SOURCE,
        SELECTING_BACKUP_SOURCE,
        EXPORTING,
        PREPARING_SHARE,
        IMPORTING,
        CREATING_BACKUP,
        VALIDATING_BACKUP,
        BACKUP_READY,
        RESTORING_BACKUP,
        EMPTY_DATABASE,
        ERROR
    }

    private final Status status;
    private final String message;
    private final int pendingRestoreCount;

    private DataManagementUiState(
            Status status,
            String message,
            int pendingRestoreCount
    ) {
        this.status = status;
        this.message = message;
        this.pendingRestoreCount =
                pendingRestoreCount;
    }

    public static DataManagementUiState idle() {
        return create(Status.IDLE);
    }

    public static DataManagementUiState
    selectingDestination() {
        return create(Status.SELECTING_DESTINATION);
    }

    public static DataManagementUiState
    selectingBackupDestination() {
        return create(
                Status.SELECTING_BACKUP_DESTINATION
        );
    }

    public static DataManagementUiState
    selectingSource() {
        return create(Status.SELECTING_SOURCE);
    }

    public static DataManagementUiState
    selectingBackupSource() {
        return create(
                Status.SELECTING_BACKUP_SOURCE
        );
    }

    public static DataManagementUiState exporting() {
        return create(Status.EXPORTING);
    }

    public static DataManagementUiState
    preparingShare() {
        return create(Status.PREPARING_SHARE);
    }

    public static DataManagementUiState importing() {
        return create(Status.IMPORTING);
    }

    public static DataManagementUiState
    creatingBackup() {
        return create(Status.CREATING_BACKUP);
    }

    public static DataManagementUiState
    validatingBackup() {
        return create(Status.VALIDATING_BACKUP);
    }

    public static DataManagementUiState backupReady(
            int restorableCount
    ) {
        return new DataManagementUiState(
                Status.BACKUP_READY,
                null,
                restorableCount
        );
    }

    public static DataManagementUiState
    restoringBackup() {
        return create(Status.RESTORING_BACKUP);
    }

    public static DataManagementUiState empty(
            String message
    ) {
        return new DataManagementUiState(
                Status.EMPTY_DATABASE,
                message,
                0
        );
    }

    public static DataManagementUiState error(
            String message
    ) {
        return new DataManagementUiState(
                Status.ERROR,
                message,
                0
        );
    }

    private static DataManagementUiState create(
            Status status
    ) {
        return new DataManagementUiState(
                status,
                null,
                0
        );
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getPendingRestoreCount() {
        return pendingRestoreCount;
    }
}