package net.tfassbender.checklist.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "jwt.expiration.hours", defaultValue = "24")
    int expirationHours;

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "checklist-app")
    String issuer;

    @ConfigProperty(name = "smallrye.jwt.sign.key")
    String signingKeyBase64;

    public String generateToken(String username) {
        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofHours(expirationHours));

        Set<String> roles = new HashSet<>();
        roles.add("user");

        // Decode the base64 key to get raw bytes, matching how the JWK verification key is interpreted
        byte[] keyBytes = Base64.getDecoder().decode(signingKeyBase64);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        return Jwt.issuer(issuer)
                .upn(username)
                .subject(username)
                .groups(roles)
                .expiresAt(expiry)
                .issuedAt(now)
                .sign(secretKey);
    }
}
