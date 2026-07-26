package com.rndymi.almacentracker.data.scanner;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

public final class CameraPermissionHistory {

    private static final String PREFERENCES_NAME =
            "scanner_permission_preferences";

    private static final String KEY_CAMERA_PERMISSION_REQUESTED =
            "camera_permission_requested_before";

    private final SharedPreferences preferences;

    public CameraPermissionHistory(Context context) {
        Objects.requireNonNull(context);

        preferences = context
                .getApplicationContext()
                .getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                );
    }

    public boolean wasRequestedBefore() {
        return preferences.getBoolean(
                KEY_CAMERA_PERMISSION_REQUESTED,
                false
        );
    }

    public void markAsRequested() {
        preferences
                .edit()
                .putBoolean(
                        KEY_CAMERA_PERMISSION_REQUESTED,
                        true
                )
                .apply();
    }
}