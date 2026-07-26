package com.rndymi.almacentracker.feature.scanner;

import androidx.annotation.Nullable;

import java.util.Objects;

public final class ScannerUiState {

    public enum Status {
        INITIALIZING,
        REQUESTING_PERMISSION,
        SCANNING,
        CODE_DETECTED,
        PERMISSION_DENIED,
        PERMISSION_DENIED_PERMANENTLY,
        CAMERA_UNAVAILABLE,
        ERROR
    }

    private final Status status;

    @Nullable
    private final String message;

    private ScannerUiState(
            Status status,
            @Nullable String message
    ) {
        this.status = Objects.requireNonNull(status);
        this.message = message;
    }

    public static ScannerUiState initializing() {
        return new ScannerUiState(
                Status.INITIALIZING,
                null
        );
    }

    public static ScannerUiState requestingPermission() {
        return new ScannerUiState(
                Status.REQUESTING_PERMISSION,
                null
        );
    }

    public static ScannerUiState scanning() {
        return new ScannerUiState(
                Status.SCANNING,
                null
        );
    }

    public static ScannerUiState codeDetected() {
        return new ScannerUiState(
                Status.CODE_DETECTED,
                null
        );
    }

    public static ScannerUiState permissionDenied() {
        return new ScannerUiState(
                Status.PERMISSION_DENIED,
                null
        );
    }

    public static ScannerUiState permissionDeniedPermanently() {
        return new ScannerUiState(
                Status.PERMISSION_DENIED_PERMANENTLY,
                null
        );
    }

    public static ScannerUiState cameraUnavailable() {
        return new ScannerUiState(
                Status.CAMERA_UNAVAILABLE,
                null
        );
    }

    public static ScannerUiState error(String message) {
        return new ScannerUiState(
                Status.ERROR,
                Objects.requireNonNull(message)
        );
    }

    public Status getStatus() {
        return status;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    public boolean showsPreview() {
        return status == Status.INITIALIZING
                || status == Status.SCANNING;
    }

    public boolean showsProgress() {
        return status == Status.INITIALIZING
                || status == Status.REQUESTING_PERMISSION;
    }

    public boolean canRetryPermission() {
        return status == Status.PERMISSION_DENIED;
    }

    public boolean canOpenSettings() {
        return status
                == Status.PERMISSION_DENIED_PERMANENTLY;
    }

    public boolean canRetryScanner() {
        return status == Status.ERROR;
    }

    public boolean canContinueManually() {
        return status == Status.INITIALIZING
                || status == Status.SCANNING
                || status == Status.PERMISSION_DENIED
                || status
                == Status.PERMISSION_DENIED_PERMANENTLY
                || status == Status.CAMERA_UNAVAILABLE
                || status == Status.ERROR;
    }

    public boolean isBlockingError() {
        return status == Status.PERMISSION_DENIED
                || status
                == Status.PERMISSION_DENIED_PERMANENTLY
                || status == Status.CAMERA_UNAVAILABLE
                || status == Status.ERROR;
    }
}