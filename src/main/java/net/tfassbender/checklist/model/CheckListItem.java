package net.tfassbender.checklist.model;

import java.util.UUID;

public class CheckListItem {
    private String id;
    private String description;
    private boolean checked;
    private int orderIndex;

    public CheckListItem() {
        this.id = UUID.randomUUID().toString();
        this.checked = false;
    }

    public CheckListItem(String description, int orderIndex) {
        this();
        this.description = description;
        this.orderIndex = orderIndex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
