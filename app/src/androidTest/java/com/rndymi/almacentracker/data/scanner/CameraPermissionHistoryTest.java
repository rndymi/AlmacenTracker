package com.rndymi.almacentracker.data.scanner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class CameraPermissionHistoryTest {

    private static final String PREFERENCES_NAME =
            "scanner_permission_preferences";

    private Context context;

    @Before
    public void setUp() {
        context =
                ApplicationProvider.getApplicationContext();

        context
                .getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                )
                .edit()
                .clear()
                .commit();
    }

    @Test
    public void permissionIsInitiallyNotRequested() {
        CameraPermissionHistory history =
                new CameraPermissionHistory(context);

        assertFalse(history.wasRequestedBefore());
    }

    @Test
    public void markAsRequestedPersistsValue() {
        CameraPermissionHistory history =
                new CameraPermissionHistory(context);

        history.markAsRequested();

        assertTrue(history.wasRequestedBefore());
    }

    @Test
    public void newInstanceReadsPersistedValue() {
        CameraPermissionHistory firstInstance =
                new CameraPermissionHistory(context);

        firstInstance.markAsRequested();

        CameraPermissionHistory secondInstance =
                new CameraPermissionHistory(context);

        assertTrue(
                secondInstance.wasRequestedBefore()
        );
    }
}