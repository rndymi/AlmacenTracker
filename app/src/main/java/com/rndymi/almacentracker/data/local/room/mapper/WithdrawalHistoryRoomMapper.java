package com.rndymi.almacentracker.data.local.room.mapper;

import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntity;
import com.rndymi.almacentracker.data.local.room.entity.WithdrawalHistoryEntryEntity;
import com.rndymi.almacentracker.data.local.room.relation.WithdrawalHistoryWithEntries;
import com.rndymi.almacentracker.domain.history.WithdrawalHistory;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryEntry;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;
import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class WithdrawalHistoryRoomMapper {

    public WithdrawalHistoryEntity toEntity(
            WithdrawalHistory history
    ) {
        Objects.requireNonNull(
                history,
                "History cannot be null"
        );

        return new WithdrawalHistoryEntity(
                history.getId(),
                history.getTitle(),
                history.getRegisteredAt(),
                history.getCreatedAt(),
                history.getUpdatedAt()
        );
    }

    public WithdrawalHistory toDomain(
            WithdrawalHistoryEntity entity
    ) {
        Objects.requireNonNull(
                entity,
                "History entity cannot be null"
        );

        return new WithdrawalHistory(
                entity.getId(),
                entity.getTitle(),
                entity.getRegisteredAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public WithdrawalHistoryEntryEntity toEntity(
            WithdrawalHistoryEntry entry
    ) {
        Objects.requireNonNull(
                entry,
                "History entry cannot be null"
        );

        return new WithdrawalHistoryEntryEntity(
                entry.getId(),
                entry.getHistoryId(),
                entry.getOrderIndex(),
                entry.getCategory(),
                entry.getCode(),
                entry.getQuantity(),
                entry.getUnit(),
                entry.getWarehouseItemIdSnapshot(),
                entry.getSiteSnapshot(),
                entry.getPositionSnapshot(),
                entry.getLocationStatus().name()
        );
    }

    public WithdrawalHistoryEntry toDomain(
            WithdrawalHistoryEntryEntity entity
    ) {
        Objects.requireNonNull(
                entity,
                "History entry entity cannot be null"
        );

        return new WithdrawalHistoryEntry(
                entity.getId(),
                entity.getHistoryId(),
                entity.getOrderIndex(),
                entity.getCategory(),
                entity.getCode(),
                entity.getQuantity(),
                entity.getUnit(),
                entity.getWarehouseItemIdSnapshot(),
                entity.getSiteSnapshot(),
                entity.getPositionSnapshot(),
                parseLocationStatus(
                        entity.getLocationStatus()
                )
        );
    }

    public List<WithdrawalHistoryEntryEntity>
    toEntryEntities(
            List<WithdrawalHistoryEntry> entries
    ) {
        Objects.requireNonNull(
                entries,
                "History entries cannot be null"
        );

        List<WithdrawalHistoryEntryEntity> entities =
                new ArrayList<>(entries.size());

        for (WithdrawalHistoryEntry entry : entries) {
            entities.add(toEntity(entry));
        }

        return entities;
    }

    public WithdrawalHistoryRecord toDomain(
            WithdrawalHistoryWithEntries relation
    ) {
        Objects.requireNonNull(
                relation,
                "History relation cannot be null"
        );
        Objects.requireNonNull(
                relation.history,
                "History relation header cannot be null"
        );

        List<WithdrawalHistoryEntryEntity> entryEntities =
                relation.entries == null
                        ? new ArrayList<>()
                        : new ArrayList<>(relation.entries);

        entryEntities.sort(
                Comparator.comparingInt(
                        WithdrawalHistoryEntryEntity::getOrderIndex
                )
        );

        List<WithdrawalHistoryEntry> entries =
                new ArrayList<>(entryEntities.size());

        for (
                WithdrawalHistoryEntryEntity entryEntity
                : entryEntities
        ) {
            entries.add(toDomain(entryEntity));
        }

        return new WithdrawalHistoryRecord(
                toDomain(relation.history),
                entries
        );
    }

    public WithdrawalHistoryWithEntries toRelation(
            WithdrawalHistoryEntity history,
            List<WithdrawalHistoryEntryEntity> entries
    ) {
        WithdrawalHistoryWithEntries relation =
                new WithdrawalHistoryWithEntries();

        relation.history = Objects.requireNonNull(history);
        relation.entries = entries == null
                ? new ArrayList<>()
                : new ArrayList<>(entries);

        return relation;
    }

    private WithdrawalLocationStatus parseLocationStatus(
            String value
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "Location status cannot be null"
            );
        }

        try {
            return WithdrawalLocationStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unknown withdrawal location status: "
                            + value,
                    exception
            );
        }
    }
}