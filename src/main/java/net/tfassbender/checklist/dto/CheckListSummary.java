package net.tfassbender.checklist.dto;

import net.tfassbender.checklist.model.CheckList;

import java.time.Instant;

public class CheckListSummary {
    private String id;
    private String name;
    private int itemCount;
    private int checkedCount;
    private Instant updatedAt;

    public CheckListSummary() {
    }

    public static CheckListSummary fromCheckList(CheckList list) {
        CheckListSummary summary = new CheckListSummary();
        summary.setId(list.getId());
        summary.setName(list.getName());
        summary.setItemCount(list.getItems().size());
        summary.setCheckedCount((int) list.getItems().stream().filter(item -> item.isChecked()).count());
        summary.setUpdatedAt(list.getUpdatedAt());
        return summary;
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

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getCheckedCount() {
        return checkedCount;
    }

    public void setCheckedCount(int checkedCount) {
        this.checkedCount = checkedCount;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
