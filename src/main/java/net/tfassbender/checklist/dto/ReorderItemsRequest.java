package net.tfassbender.checklist.dto;

import java.util.List;

public class ReorderItemsRequest {
    private List<String> itemIds;

    public List<String> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds;
    }
}
