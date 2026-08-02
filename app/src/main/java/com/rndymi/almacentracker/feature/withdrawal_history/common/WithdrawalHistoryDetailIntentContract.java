package com.rndymi.almacentracker.feature.withdrawal_history.common;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.rndymi.almacentracker.feature.withdrawal_history.detail.WithdrawalHistoryDetailActivity;

import java.util.Objects;

public final class WithdrawalHistoryDetailIntentContract {

    public static final String EXTRA_HISTORY_ID =
            "com.rndymi.almacentracker.extra.WITHDRAWAL_HISTORY_ID";

    public static final String EXTRA_DELETED_HISTORY_ID =
            "com.rndymi.almacentracker.extra.DELETED_WITHDRAWAL_HISTORY_ID";

    private WithdrawalHistoryDetailIntentContract() {
    }

    public static Intent createIntent(
            Context context,
            long historyId
    ) {
        Objects.requireNonNull(
                context,
                "context"
        );

        if (historyId <= 0L) {
            throw new IllegalArgumentException(
                    "History id must be positive"
            );
        }

        return new Intent(
                context,
                WithdrawalHistoryDetailActivity.class
        ).putExtra(
                EXTRA_HISTORY_ID,
                historyId
        );
    }

    public static Intent createDeleteResult(
            long deletedHistoryId
    ) {
        if (deletedHistoryId <= 0L) {
            throw new IllegalArgumentException(
                    "Deleted history id must be positive"
            );
        }

        return new Intent().putExtra(
                EXTRA_DELETED_HISTORY_ID,
                deletedHistoryId
        );
    }

    public static long readHistoryId(
            @Nullable Intent intent
    ) {
        if (intent == null) {
            return -1L;
        }

        return intent.getLongExtra(
                EXTRA_HISTORY_ID,
                -1L
        );
    }

    public static long readDeletedHistoryId(
            @Nullable Intent intent
    ) {
        if (intent == null) {
            return -1L;
        }

        return intent.getLongExtra(
                EXTRA_DELETED_HISTORY_ID,
                -1L
        );
    }
}