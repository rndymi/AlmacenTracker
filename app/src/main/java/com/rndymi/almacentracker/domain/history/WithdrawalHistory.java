package com.rndymi.almacentracker.domain.history;

public final class WithdrawalHistory {

    private final long id;
    private final String title;
    private final String destination;
    private final long registeredAt;
    private final long createdAt;
    private final long updatedAt;

    public WithdrawalHistory(
            long id,
            String title,
            long registeredAt,
            long createdAt,
            long updatedAt
    ) {
        this(id, title, null, registeredAt, createdAt, updatedAt);
    }

    public WithdrawalHistory(
            long id,
            String title,
            String destination,
            long registeredAt,
            long createdAt,
            long updatedAt
    ) {
        if (id < 0L) {
            throw new IllegalArgumentException(
                    "History id cannot be negative"
            );
        }

        if (registeredAt <= 0L) {
            throw new IllegalArgumentException(
                    "Registered timestamp must be positive"
            );
        }

        if (createdAt <= 0L) {
            throw new IllegalArgumentException(
                    "Created timestamp must be positive"
            );
        }

        if (updatedAt <= 0L) {
            throw new IllegalArgumentException(
                    "Updated timestamp must be positive"
            );
        }

        this.id = id;
        this.title = normalizeOptional(title);
        this.destination = normalizeOptional(destination);
        this.registeredAt = registeredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDestination() {
        return destination;
    }

    public boolean hasDestination() {
        return destination != null;
    }

    public long getRegisteredAt() {
        return registeredAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public boolean hasTitle() {
        return title != null;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}
