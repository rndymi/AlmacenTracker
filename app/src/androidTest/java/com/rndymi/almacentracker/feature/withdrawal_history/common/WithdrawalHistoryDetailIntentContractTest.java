package com.rndymi.almacentracker.feature.withdrawal_history.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rndymi.almacentracker.feature.withdrawal_history.detail.WithdrawalHistoryDetailActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class WithdrawalHistoryDetailIntentContractTest {

    @Test
    public void createIntentStoresHistoryId() {
        Context context =
                ApplicationProvider
                        .getApplicationContext();

        Intent intent =
                WithdrawalHistoryDetailIntentContract
                        .createIntent(
                                context,
                                17L
                        );

        assertEquals(
                17L,
                WithdrawalHistoryDetailIntentContract
                        .readHistoryId(intent)
        );

        assertTrue(
                intent.getComponent()
                        .getClassName()
                        .equals(
                                WithdrawalHistoryDetailActivity
                                        .class
                                        .getName()
                        )
        );
    }

    @Test
    public void createIntentRejectsZeroId() {
        Context context =
                ApplicationProvider
                        .getApplicationContext();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        WithdrawalHistoryDetailIntentContract
                                .createIntent(
                                        context,
                                        0L
                                )
        );
    }

    @Test
    public void createIntentRejectsNegativeId() {
        Context context =
                ApplicationProvider
                        .getApplicationContext();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        WithdrawalHistoryDetailIntentContract
                                .createIntent(
                                        context,
                                        -1L
                                )
        );
    }

    @Test
    public void readHistoryIdReturnsInvalidValueForNullIntent() {
        assertEquals(
                -1L,
                WithdrawalHistoryDetailIntentContract
                        .readHistoryId(null)
        );
    }

    @Test
    public void readHistoryIdReturnsInvalidValueWhenExtraIsMissing() {
        assertEquals(
                -1L,
                WithdrawalHistoryDetailIntentContract
                        .readHistoryId(
                                new Intent()
                        )
        );
    }
}
