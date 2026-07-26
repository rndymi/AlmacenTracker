package com.rndymi.almacentracker.feature.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class ScannerActivityContractTest {

    @Test
    public void extractsScannerResultExtras() {
        Intent intent = new Intent();

        intent.putExtra(
                ScannerActivity.EXTRA_SCANNED_VALUE,
                "001050"
        );

        intent.putExtra(
                ScannerActivity.EXTRA_SCANNED_FORMAT,
                "CODE_128"
        );

        assertEquals(
                "001050",
                ScannerActivity.getScannedValue(intent)
        );

        assertEquals(
                "CODE_128",
                ScannerActivity.getScannedFormat(intent)
        );
    }

    @Test
    public void nullIntentProducesNoScannerResult() {
        assertNull(
                ScannerActivity.getScannedValue(null)
        );

        assertNull(
                ScannerActivity.getScannedFormat(null)
        );
    }

    @Test
    public void createIntentTargetsScannerActivity() {
        Context context =
                ApplicationProvider.getApplicationContext();

        Intent intent =
                ScannerActivity.createIntent(context);

        assertEquals(
                ScannerActivity.class.getName(),
                intent.getComponent().getClassName()
        );
    }

    @Test
    public void applicationSettingsIntentUsesExpectedAction() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        );

        assertEquals(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                intent.getAction()
        );
    }
}