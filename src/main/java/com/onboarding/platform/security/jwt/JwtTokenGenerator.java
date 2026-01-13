package com.onboarding.platform.security.jwt;

import com.onboarding.platform.security.model.User;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple JWT token generator and validator
 */
@Singleton
public class JwtTokenGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(JwtTokenGenerator.class);
    private static final String ALGORITHM = "HmacSHA256";

    @Value("${jwt.secret:your-256-bit-secret-change-this-in-production}")
    private String secret;

    @Value("${jwt.expiration:3600}")
    private long expirationSeconds;

    public String generateToken(User user) {
        try {
            long expirationTime = Instant.now().getEpochSecond() + expirationSeconds;

            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", user.getUsername());
            payload.put("userId", user.getId().toString());
            payload.put("email", user.getEmail());
            payload.put("role", user.getRole().name());
            payload.put("exp", expirationTime);
            payload.put("iat", Instant.now().getEpochSecond());

            String payloadJson = toJson(payload);
            String encodedPayload = base64UrlEncode(payloadJson);

            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String encodedHeader = base64UrlEncode(header);

            String data = encodedHeader + "." + encodedPayload;
            String signature = sign(data);

            return encodedHeader + "." + encodedPayload + "." + signature;

        } catch (Exception e) {
            LOG.error("Failed to generate JWT token", e);
            throw new RuntimeException("Token generation failed", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            String header = parts[0];
            String payload = parts[1];
            String signature = parts[2];

            String data = header + "." + payload;
            String expectedSignature = sign(data);

            if (!signature.equals(expectedSignature)) {
                LOG.warn("Invalid token signature");
                return false;
            }

            String payloadJson = base64UrlDecode(payload);
            long exp = extractExpiration(payloadJson);

            if (Instant.now().getEpochSecond() > exp) {
                LOG.warn("Token expired");
                return false;
            }

            return true;

        } catch (Exception e) {
            LOG.error("Token validation failed", e);
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            String payload = token.split("\\.")[1];
            String payloadJson = base64UrlDecode(payload);
            return extractField(payloadJson, "sub");
        } catch (Exception e) {
            LOG.error("Failed to extract username from token", e);
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            String payload = token.split("\\.")[1];
            String payloadJson = base64UrlDecode(payload);
            return extractField(payloadJson, "role");
        } catch (Exception e) {
            LOG.error("Failed to extract role from token", e);
            return null;
        }
    }

    // Helper methods

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance(ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        mac.init(secretKey);
        byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(signatureBytes);
    }

    private String base64UrlEncode(String data) {
        return base64UrlEncode(data.getBytes(StandardCharsets.UTF_8));
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private String base64UrlDecode(String encoded) {
        byte[] decoded = Base64.getUrlDecoder().decode(encoded);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else {
                json.append(value);
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    private long extractExpiration(String payloadJson) {
        String expStr = extractField(payloadJson, "exp");
        return expStr != null ? Long.parseLong(expStr) : 0;
    }

    private String extractField(String json, String field) {
        String search = "\"" + field + "\":";
        int start = json.indexOf(search);
        if (start == -1) return null;

        start += search.length();

        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) {
            start++;
        }

        int end = start;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != '"') {
            end++;
        }

        return json.substring(start, end);
    }
}
