package com.rndymi.almacentracker.feature.withdrawal_history.create;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;
import com.rndymi.almacentracker.feature.withdrawal_history.common.WithdrawalHistoryCreateInput;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public final class WithdrawalHistoryTitleIntentTest {

    @Test
    public void createIntentCarriesMultilingualTitle() {
        Context context =
                ApplicationProvider
                        .getApplicationContext();

        WithdrawalHistoryCreateInput input =
                new WithdrawalHistoryCreateInput(
                        0,
                        "MR",
                        "21570",
                        5,
                        "PCS",
                        1L,
                        "A1",
                        null,
                        WithdrawalLocationStatus.FOUND
                );

        Intent intent =
                WithdrawalHistoryCreateActivity
                        .createIntent(
                                context,
                                Collections.singletonList(
                                        input
                                ),
                                "王小明"
                        );

        assertEquals(
                "王小明",
                intent.getStringExtra(
                        "com.rndymi.almacentracker.extra."
                                + "WITHDRAWAL_HISTORY_INITIAL_TITLE"
                )
        );
    }
}
