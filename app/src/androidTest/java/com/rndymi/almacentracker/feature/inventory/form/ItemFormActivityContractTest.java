package com.rndymi.almacentracker.feature.inventory.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class ItemFormActivityContractTest {

    @Test
    public void createIntentIncludesInitialCode() {
        Context context =
                ApplicationProvider
                        .getApplicationContext();

        Intent intent =
                ItemFormActivity.createIntent(
                        context,
                        "001050"
                );

        assertTrue(
                intent.hasExtra(
                        ItemFormActivity.EXTRA_INITIAL_CODE
                )
        );

        assertEquals(
                "001050",
                intent.getStringExtra(
                        ItemFormActivity.EXTRA_INITIAL_CODE
                )
        );

        assertFalse(
                intent.hasExtra(
                        ItemFormActivity
                                .EXTRA_WAREHOUSE_ITEM_ID
                )
        );
    }

    @Test
    public void createIntentWithoutCodeOmitsInitialCode() {
        Context context =
                ApplicationProvider
                        .getApplicationContext();

        Intent intent =
                ItemFormActivity.createIntent(
                        context,
                        null
                );

        assertFalse(
                intent.hasExtra(
                        ItemFormActivity.EXTRA_INITIAL_CODE
                )
        );

        assertFalse(
                intent.hasExtra(
                        ItemFormActivity
                                .EXTRA_WAREHOUSE_ITEM_ID
                )
        );
    }

    @Test
    public void editIntentIncludesOnlyWarehouseItemId() {
        Context context =
                ApplicationProvider
                        .getApplicationContext();

        Intent intent =
                ItemFormActivity.createEditIntent(
                        context,
                        17L
                );

        assertEquals(
                17L,
                intent.getLongExtra(
                        ItemFormActivity
                                .EXTRA_WAREHOUSE_ITEM_ID,
                        0L
                )
        );

        assertFalse(
                intent.hasExtra(
                        ItemFormActivity.EXTRA_INITIAL_CODE
                )
        );
    }
}