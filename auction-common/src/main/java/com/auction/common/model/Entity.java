package com.auction.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public abstract class Entity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private LocalDateTime createdAt;

    protected Entity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    protected Entity(String id) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.createdAt = LocalDateTime.now();
    }

    public abstract String printInfo();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Entity)) return false;
        Entity other = (Entity) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}