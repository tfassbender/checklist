package net.tfassbender.checklist.dto;

import java.util.List;

public record ReorderItemsRequest(List<String> itemIds) {
}
