package com.rndymi.almacentracker.feature.withdrawal_history.detail;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.domain.history.WithdrawalHistory;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;

import org.junit.Test;

import java.util.Collections;

public final class WithdrawalHistoryDetailUiStateTest {

    @Test
    public void loadingCanPreservePreviousContent() {
        WithdrawalHistoryRecord record =
                createRecord();

        WithdrawalHistoryDetailUiState state =
                WithdrawalHistoryDetailUiState
                        .loading(record);

        assertTrue(state.isLoading());
        assertTrue(state.hasContent());
        assertSame(record, state.getRecord());
    }

    @Test
    public void notFoundContainsNoRecord() {
        WithdrawalHistoryDetailUiState state =
                WithdrawalHistoryDetailUiState
                        .notFound();

        assertTrue(state.isNotFound());
        assertFalse(state.hasContent());
    }

    @Test
    public void errorCanPreservePreviousContent() {
        WithdrawalHistoryRecord record =
                createRecord();

        WithdrawalHistoryDetailUiState state =
                WithdrawalHistoryDetailUiState
                        .error(record);

        assertTrue(state.hasError());
        assertTrue(state.hasContent());
        assertSame(record, state.getRecord());
    }

    private WithdrawalHistoryRecord createRecord() {
        return new WithdrawalHistoryRecord(
                new WithdrawalHistory(
                        1L,
                        null,
                        1_700_000_000_000L,
                        1_700_000_000_100L,
                        1_700_000_000_100L
                ),
                Collections.emptyList()
        );
    }
}
