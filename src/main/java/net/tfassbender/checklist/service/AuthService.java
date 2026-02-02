package net.tfassbender.checklist.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.tfassbender.checklist.model.User;
import net.tfassbender.checklist.security.JwtService;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

@ApplicationScoped
public class AuthService {

    @Inject
    FileStorageService storageService;

    @Inject
    JwtService jwtService;

    public record AuthResult(String token, String username) {
    }

    public AuthResult register(String username, String password) throws AuthException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthException("Username is required");
        }
        if (password == null || password.length() < 4) {
            throw new AuthException("Password must be at least 4 characters");
        }

        String trimmedUsername = username.trim();
        String normalizedUsername = trimmedUsername.toLowerCase();

        if (!normalizedUsername.matches("^[a-zA-Z0-9_]+$")) {
            throw new AuthException("Username can only contain letters, numbers, and underscores");
        }

        if (storageService.userExists(normalizedUsername)) {
            throw new AuthException("Username already exists");
        }

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(trimmedUsername, passwordHash);

        try {
            storageService.saveUser(user);
        } catch (IOException e) {
            throw new AuthException("Failed to create user");
        }

        return new AuthResult(jwtService.generateToken(trimmedUsername), trimmedUsername);
    }

    public AuthResult login(String username, String password) throws AuthException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthException("Username is required");
        }
        if (password == null || password.isEmpty()) {
            throw new AuthException("Password is required");
        }

        String normalizedUsername = username.trim().toLowerCase();

        User user = storageService.loadUser(normalizedUsername)
                .orElseThrow(() -> new AuthException("Invalid username or password"));

        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new AuthException("Invalid username or password");
        }

        String actualUsername = user.getUsername();
        return new AuthResult(jwtService.generateToken(actualUsername), actualUsername);
    }

    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }
}
