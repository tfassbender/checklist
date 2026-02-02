package net.tfassbender.checklist.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.tfassbender.checklist.dto.CheckListSummary;
import net.tfassbender.checklist.model.CheckList;
import net.tfassbender.checklist.model.CheckListItem;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListService {

    @Inject
    FileStorageService storageService;

    public List<CheckListSummary> getAllLists(String username) {
        return storageService.loadAllLists(username).stream()
                .map(CheckListSummary::fromCheckList)
                .sorted(Comparator.comparing(CheckListSummary::updatedAt).reversed())
                .collect(Collectors.toList());
    }

    public CheckList createList(String username, String name) throws ListException {
        if (name == null || name.trim().isEmpty()) {
            throw new ListException("List name is required");
        }

        String trimmedName = name.trim();

        if (listNameExists(username, trimmedName, null)) {
            throw new ListException("A list with this name already exists");
        }

        CheckList list = new CheckList(trimmedName);

        try {
            storageService.saveList(username, list);
        } catch (IOException e) {
            throw new ListException("Failed to create list");
        }

        return list;
    }

    public Optional<CheckList> getList(String username, String listId) {
        return storageService.loadList(username, listId);
    }

    public CheckList updateList(String username, String listId, String name, String notes) throws ListException {
        CheckList list = storageService.loadList(username, listId)
                .orElseThrow(() -> new ListException("List not found"));

        if (name != null) {
            String trimmedName = name.trim();
            if (trimmedName.isEmpty()) {
                throw new ListException("List name cannot be empty");
            }
            if (listNameExists(username, trimmedName, listId)) {
                throw new ListException("A list with this name already exists");
            }
            list.setName(trimmedName);
        }

        if (notes != null) {
            list.setNotes(notes);
        }

        list.touch();

        try {
            storageService.saveList(username, list);
        } catch (IOException e) {
            throw new ListException("Failed to update list");
        }

        return list;
    }

    public void deleteList(String username, String listId) throws ListException {
        if (!storageService.deleteList(username, listId)) {
            throw new ListException("Failed to delete list");
        }
    }

    public CheckList resetList(String username, String listId) throws ListException {
        CheckList list = storageService.loadList(username, listId)
                .orElseThrow(() -> new ListException("List not found"));

        list.getItems().forEach(item -> item.setChecked(false));
        list.touch();

        try {
            storageService.saveList(username, list);
        } catch (IOException e) {
            throw new ListException("Failed to reset list");
        }

        return list;
    }

    public CheckListItem addItem(String username, String listId, String description) throws ListException {
        if (description == null || description.trim().isEmpty()) {
            throw new ListException("Item description is required");
        }

        CheckList list = storageService.loadList(username, listId)
                .orElseThrow(() -> new ListException("List not found"));

        int maxIndex = list.getItems().stream()
                .mapToInt(CheckListItem::getOrderIndex)
                .max()
                .orElse(-1);

        CheckListItem item = new CheckListItem(description.trim(), maxIndex + 1);
        list.getItems().add(item);
        list.touch();

        try {
            storageService.saveList(username, list);
        } catch (IOException e) {
            throw new ListException("Failed to add item");
        }

        return item;
    }

    public CheckListItem updateItem(String username, String listId, String itemId,
                                     String description, Boolean checked) throws ListException {
        CheckList list = storageService.loadList(username, listId)
                .orElseThrow(() -> new ListException("List not found"));

        CheckListItem item = list.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ListException("Item not found"));

        if (description != null) {
            String trimmedDescription = description.trim();
            if (trimmedDescription.isEmpty()) {
                throw new ListException("Item description cannot be empty");
            }
            item.setDescription(trimmedDescription);
        }

        if (checked != null) {
            item.setChecked(checked);
        }

        list.touch();

        try {
            storageService.saveList(username, list);
        } catch (IOException e) {
            throw new ListException("Failed to update item");
        }

        return item;
    }

    public void deleteItem(String username, String listId, String itemId) throws ListException {
        CheckList list = storageService.loadList(username, listId)
                .orElseThrow(() -> new ListException("List not found"));

        boolean removed = list.getItems().removeIf(item -> item.getId().equals(itemId));

        if (!removed) {
            throw new ListException("Item not found");
        }

        list.touch();

        try {
            storageService.saveList(username, list);
        } catch (IOException e) {
            throw new ListException("Failed to delete item");
        }
    }

    public CheckList reorderItems(String username, String listId, List<String> itemIds) throws ListException {
        CheckList list = storageService.loadList(username, listId)
                .orElseThrow(() -> new ListException("List not found"));

        Map<String, CheckListItem> itemMap = list.getItems().stream()
                .collect(Collectors.toMap(CheckListItem::getId, Function.identity()));

        for (int i = 0; i < itemIds.size(); i++) {
            CheckListItem item = itemMap.get(itemIds.get(i));
            if (item != null) {
                item.setOrderIndex(i);
            }
        }

        list.getItems().sort(Comparator.comparingInt(CheckListItem::getOrderIndex));
        list.touch();

        try {
            storageService.saveList(username, list);
        } catch (IOException e) {
            throw new ListException("Failed to reorder items");
        }

        return list;
    }

    private boolean listNameExists(String username, String name, String excludeId) {
        return storageService.loadAllLists(username).stream()
                .filter(list -> !list.getId().equals(excludeId))
                .anyMatch(list -> list.getName().equalsIgnoreCase(name));
    }

    public static class ListException extends Exception {
        public ListException(String message) {
            super(message);
        }
    }
}
