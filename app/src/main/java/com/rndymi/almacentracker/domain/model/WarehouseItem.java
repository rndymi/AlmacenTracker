package com.rndymi.almacentracker.domain.model;

import java.util.Objects;

public final class WarehouseItem {
    private final long id;
    private final String category;
    private final String code;
    private final String site;
    private final String position;
    private final String observations;
    private final long createdAt;
    private final long updatedAt;

    public WarehouseItem(long id, String category, String code, String site, String position, String observations, long createdAt, long updatedAt) {
        this.id = id;
        this.category = Objects.requireNonNull(category);
        this.code = Objects.requireNonNull(code);
        this.site = Objects.requireNonNull(site);
        this.position = position;
        this.observations = observations;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getSite() {
        return site;
    }

    public String getPosition() {
        return position;
    }

    public String getObservations() {
        return observations;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public boolean hasPosition() {
        return position != null && !position.trim().isEmpty();
    }
}
