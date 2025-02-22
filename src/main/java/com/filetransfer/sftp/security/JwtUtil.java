package com.filetransfer.sftp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String encryptedSecretKey;

    @Value("${jwt.encryption.key}")
    private String encodedEncryptionKey;

    private Key getSigningKey() {
        try {
            // Decode the Base64-encoded AES key (must be exactly 32 bytes)
            byte[] aesKeyBytes = Base64.getDecoder().decode(encodedEncryptionKey);
            if (aesKeyBytes.length != 32) { // Ensure AES-256 key is 32 bytes
                logger.error("Invalid AES key length: {} bytes", aesKeyBytes.length);
                throw new RuntimeException("Invalid AES key length: " + aesKeyBytes.length + " bytes");
            }

            // Decode Base64-encoded encrypted JWT secret
            byte[] decodedSecret = Base64.getDecoder().decode(encryptedSecretKey);

            // Decrypt secret key using AES-256 with PKCS5Padding
            SecretKeySpec aesKey = new SecretKeySpec(aesKeyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] decryptedKey = cipher.doFinal(decodedSecret);

            logger.debug("JWT secret key successfully decrypted.");
            return Keys.hmacShaKeyFor(decryptedKey);
        } catch (Exception e) {
            logger.error("Failed to decrypt JWT secret key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to decrypt JWT secret key", e);
        }
    }

    public String generateToken(String username) {
        try {
            logger.debug("Generating JWT token for user: {}", username);

            String token = Jwts.builder()
                    .subject(username)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour expiration
                    .signWith(getSigningKey())
                    .compact();

            logger.debug("JWT token generated successfully for user: {}", username);
            return token;
        } catch (Exception e) {
            logger.error("Failed to generate JWT token for user: {}", username, e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    public Claims getClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            logger.debug("Claims extracted successfully from JWT token.");
            return claims;
        } catch (SignatureException e) {
            logger.warn("Invalid JWT signature: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT signature", e);
        } catch (Exception e) {
            logger.error("Failed to extract claims from JWT token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract claims from JWT token", e);
        }
    }

    public String extractUsername(String token) {
        try {
            String username = getClaims(token).getSubject();
            logger.debug("Username extracted from JWT token: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("Failed to extract username from JWT token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract username from JWT token", e);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            boolean isValid = extractUsername(token) != null && !getClaims(token).getExpiration().before(new Date());
            logger.debug("JWT token validation result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            logger.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String generateGuestToken(String guestUsername) {
        long expirationMillis = System.currentTimeMillis() + (1000 * 60 * 60); // 1-hour expiration
        Date expirationDate = new Date(expirationMillis);

        return Jwts.builder()
                .claims(Map.of(
                        "sub", guestUsername,
                        "iat", new Date(),
                        "exp", expirationDate
                ))
                .signWith(getSigningKey())
                .compact();
    }
}