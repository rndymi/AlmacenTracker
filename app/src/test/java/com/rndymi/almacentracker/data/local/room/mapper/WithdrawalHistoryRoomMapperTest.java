package com.rndymi.almacentracker.data.local.room.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntity;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntryEntity;
import com.rndymi.almacentracker.data.local.room.projection.WithdrawalHistorySummaryRow;
import com.rndymi.almacentracker.data.local.room.relation.WithdrawalHistoryWithEntries;
import com.rndymi.almacentracker.domain.history.WithdrawalHistory;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryEntry;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySummary;
import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class WithdrawalHistoryRoomMapperTest {

    private WithdrawalHistoryRoomMapper mapper;

    @Before
    public void setUp() {
        mapper = new WithdrawalHistoryRoomMapper();
    }

    @Test
    public void mapsHistoryToEntityAndBack() {
        WithdrawalHistory history =
                new WithdrawalHistory(
                        5L,
                        "Lista centro",
                        100L,
                        110L,
                        120L
                );

        WithdrawalHistoryEntity entity =
                mapper.toEntity(history);

        WithdrawalHistory result =
                mapper.toDomain(entity);

        assertEquals(5L, result.getId());
        assertEquals(
                "Lista centro",
                result.getTitle()
        );
        assertEquals(100L, result.getRegisteredAt());
        assertEquals(110L, result.getCreatedAt());
        assertEquals(120L, result.getUpdatedAt());
    }

    @Test
    public void mapsFoundEntryUsingTextStatus() {
        WithdrawalHistoryEntry entry =
                new WithdrawalHistoryEntry(
                        9L,
                        5L,
                        2,
                        "MR",
                        "001210A",
                        4,
                        "CAJAS",
                        7L,
                        "A1",
                        "Nivel 2",
                        WithdrawalLocationStatus.FOUND
                );

        WithdrawalHistoryEntryEntity entity =
                mapper.toEntity(entry);

        assertEquals("FOUND", entity.getLocationStatus());
        assertEquals("001210A", entity.getCode());

        WithdrawalHistoryEntry result =
                mapper.toDomain(entity);

        assertEquals(
                WithdrawalLocationStatus.FOUND,
                result.getLocationStatus()
        );
        assertEquals(
                Integer.valueOf(4),
                result.getQuantity()
        );
        assertEquals(
                "Nivel 2",
                result.getPositionSnapshot()
        );
    }

    @Test
    public void mapsNotFoundEntryWithNullSnapshots() {
        WithdrawalHistoryEntryEntity entity =
                new WithdrawalHistoryEntryEntity(
                        10L,
                        5L,
                        1,
                        "MZ",
                        "0900",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "NOT_FOUND"
                );

        WithdrawalHistoryEntry result =
                mapper.toDomain(entity);

        assertEquals(
                WithdrawalLocationStatus.NOT_FOUND,
                result.getLocationStatus()
        );
        assertNull(result.getQuantity());
        assertNull(result.getWarehouseItemIdSnapshot());
        assertNull(result.getSiteSnapshot());
    }

    @Test
    public void relationIsOrderedByOrderIndex() {
        WithdrawalHistoryEntity history =
                new WithdrawalHistoryEntity(
                        4L,
                        null,
                        100L,
                        100L,
                        100L
                );

        WithdrawalHistoryEntryEntity second =
                new WithdrawalHistoryEntryEntity(
                        2L,
                        4L,
                        1,
                        "MZ",
                        "1300C",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "NOT_FOUND"
                );

        WithdrawalHistoryEntryEntity first =
                new WithdrawalHistoryEntryEntity(
                        1L,
                        4L,
                        0,
                        "MR",
                        "001210A",
                        4,
                        "CAJAS",
                        null,
                        7L,
                        "A1",
                        null,
                        "FOUND"
                );

        WithdrawalHistoryWithEntries relation =
                mapper.toRelation(
                        history,
                        Arrays.asList(second, first)
                );

        WithdrawalHistoryRecord result =
                mapper.toDomain(relation);

        assertEquals(2, result.getEntries().size());
        assertEquals(
                "001210A",
                result.getEntries().get(0).getCode()
        );
        assertEquals(
                "1300C",
                result.getEntries().get(1).getCode()
        );
    }

    @Test
    public void unknownLocationStatusIsRejected() {
        WithdrawalHistoryEntryEntity entity =
                new WithdrawalHistoryEntryEntity(
                        1L,
                        2L,
                        0,
                        "MR",
                        "1210",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "UNKNOWN"
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toDomain(entity)
        );
    }

    @Test
    public void summaryRow_mapsToDomain() {
        WithdrawalHistorySummaryRow row =
                new WithdrawalHistorySummaryRow(
                        7L,
                        "Reposición",
                        100L,
                        110L,
                        120L,
                        4,
                        3,
                        1
                );

        WithdrawalHistorySummary summary =
                mapper.toDomain(row);

        assertEquals(7L, summary.getId());
        assertEquals("Reposición", summary.getTitle());
        assertEquals(4, summary.getEntryCount());
        assertEquals(3, summary.getFoundCount());
        assertEquals(1, summary.getNotFoundCount());
    }

    @Test
    public void nullSummaryRow_isRejected() {
        assertThrows(
                NullPointerException.class,
                () -> mapper.toDomain(
                        (WithdrawalHistorySummaryRow) null
                )
        );
    }

    @Test
    public void inconsistentSummaryCounters_areRejected() {
        WithdrawalHistorySummaryRow row =
                new WithdrawalHistorySummaryRow(
                        7L,
                        null,
                        100L,
                        110L,
                        120L,
                        4,
                        2,
                        1
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toDomain(row)
        );
    }
}
