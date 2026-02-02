package net.tfassbender.checklist.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckList {
    private String id;
    private String name;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CheckListItem> items;

    public CheckList() {
        this.id = UUID.randomUUID().toString();
        this.notes = "";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.items = new ArrayList<>();
    }

    public CheckList(String name) {
        this();
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<CheckListItem> getItems() {
        return items;
    }

    public void setItems(List<CheckListItem> items) {
        this.items = items;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
