package net.tfassbender.checklist.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import net.tfassbender.checklist.model.CheckList;
import net.tfassbender.checklist.model.User;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ApplicationScoped
public class FileStorageService {

    @ConfigProperty(name = "checklist.data.dir", defaultValue = "data")
    String dataDir;

    private final ObjectMapper objectMapper;

    public FileStorageService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(Path.of(dataDir, "users"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data directory", e);
        }
    }

    private Path getUserDir(String username) {
        return Path.of(dataDir, "users", sanitizeUsername(username));
    }

    private Path getUserFile(String username) {
        return getUserDir(username).resolve("user.json");
    }

    private Path getListsDir(String username) {
        return getUserDir(username).resolve("lists");
    }

    private Path getListFile(String username, String listId) {
        return getListsDir(username).resolve(listId + ".json");
    }

    private String sanitizeUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, and underscores");
        }
        return username.toLowerCase();
    }

    public boolean userExists(String username) {
        return Files.exists(getUserFile(username));
    }

    public void saveUser(User user) throws IOException {
        Path userDir = getUserDir(user.getUsername());
        Files.createDirectories(userDir);
        Files.createDirectories(getListsDir(user.getUsername()));
        objectMapper.writeValue(getUserFile(user.getUsername()).toFile(), user);
    }

    public Optional<User> loadUser(String username) {
        try {
            Path userFile = getUserFile(username);
            if (Files.exists(userFile)) {
                return Optional.of(objectMapper.readValue(userFile.toFile(), User.class));
            }
        } catch (IOException e) {
            // Log error but return empty
        }
        return Optional.empty();
    }

    public void saveList(String username, CheckList list) throws IOException {
        Path listsDir = getListsDir(username);
        Files.createDirectories(listsDir);
        objectMapper.writeValue(getListFile(username, list.getId()).toFile(), list);
    }

    public Optional<CheckList> loadList(String username, String listId) {
        try {
            Path listFile = getListFile(username, listId);
            if (Files.exists(listFile)) {
                return Optional.of(objectMapper.readValue(listFile.toFile(), CheckList.class));
            }
        } catch (IOException e) {
            // Log error but return empty
        }
        return Optional.empty();
    }

    public List<CheckList> loadAllLists(String username) {
        List<CheckList> lists = new ArrayList<>();
        Path listsDir = getListsDir(username);

        if (!Files.exists(listsDir)) {
            return lists;
        }

        try (Stream<Path> files = Files.list(listsDir)) {
            files.filter(path -> path.toString().endsWith(".json"))
                 .forEach(path -> {
                     try {
                         CheckList list = objectMapper.readValue(path.toFile(), CheckList.class);
                         lists.add(list);
                     } catch (IOException e) {
                         // Skip invalid files
                     }
                 });
        } catch (IOException e) {
            // Log error but return what we have
        }

        return lists;
    }

    public boolean deleteList(String username, String listId) {
        try {
            Path listFile = getListFile(username, listId);
            return Files.deleteIfExists(listFile);
        } catch (IOException e) {
            return false;
        }
    }
}
