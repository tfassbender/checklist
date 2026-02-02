package net.tfassbender.checklist.dto;

import net.tfassbender.checklist.model.CheckList;
import net.tfassbender.checklist.model.CheckListItem;

import java.time.Instant;

public record CheckListSummary(
        String id,
        String name,
        int itemCount,
        int checkedCount,
        boolean active,
        Instant updatedAt
) {
    public static CheckListSummary fromCheckList(CheckList list) {
        return new CheckListSummary(
                list.getId(),
                list.getName(),
                list.getItems().size(),
                (int) list.getItems().stream().filter(CheckListItem::isChecked).count(),
                list.isActive(),
                list.getUpdatedAt()
        );
    }
}
