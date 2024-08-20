package com.creativepool.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    // HMAC key for signing the JWT
    private static final String HMAC_KEY_STRING = "your-32-characters-long-hmac-key!"; // 256-bit key
    private static final SecretKey HMAC_KEY = new SecretKeySpec(
            HMAC_KEY_STRING.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    // AES key for encrypting/decrypting the user ID
    private static final String AES_KEY_STRING = "your-32-characters-long-aes-key!"; // 256-bit key
    private static final SecretKey AES_KEY = new SecretKeySpec(
            AES_KEY_STRING.getBytes(StandardCharsets.UTF_8), "AES");

    public String generateToken(String userId) {
        // Encrypt the user ID using AES
        String encryptedUserId = encrypt(userId);

        return Jwts.builder()
                .claim("userId", encryptedUserId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours expiration
                .signWith(HMAC_KEY, SignatureAlgorithm.HS256) // Sign with HMAC
                .compact();
    }

    public String validateTokenAndExtractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(HMAC_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String encryptedUserId = claims.get("userId", String.class);

            // Decrypt the user ID using AES
            return decrypt(encryptedUserId);
        } catch (Exception e) {
            return null; // Return null if validation fails
        }
    }

    private String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, AES_KEY);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while encrypting data", e);
        }
    }

    private String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, AES_KEY);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while decrypting data", e);
        }
    }
}
