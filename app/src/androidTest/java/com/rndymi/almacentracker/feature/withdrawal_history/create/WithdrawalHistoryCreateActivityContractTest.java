package com.rndymi.almacentracker.feature.withdrawal_history.create;

import static org.junit.Assert.assertEquals;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class WithdrawalHistoryCreateActivityContractTest {

    @Test
    public void getSavedHistoryId_returnsStoredId() {
        Intent result =
                new Intent()
                        .putExtra(
                                WithdrawalHistoryCreateActivity
                                        .EXTRA_SAVED_HISTORY_ID,
                                24L
                        );

        assertEquals(
                24L,
                WithdrawalHistoryCreateActivity
                        .getSavedHistoryId(result)
        );
    }

    @Test
    public void getSavedHistoryId_returnsZeroWithoutData() {
        assertEquals(
                0L,
                WithdrawalHistoryCreateActivity
                        .getSavedHistoryId(null)
        );
    }
}
