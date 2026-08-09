package com.pubsub.utils;

import com.pubsub.config.SalesforceJwtConfig;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class SalesforceJwtGenerator {

    private final SalesforceJwtConfig jwtConfig;

    public String generateToken() throws Exception {
        PrivateKey privateKey = loadPrivateKey(jwtConfig.getPrivateKeyPath());

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + 3 * 60 * 1000); // 3 minutes expiration

        log.info("Rohit salesforce login URL: [{}]", jwtConfig.getLoginUrl());
        System.out.println("LOGIN URL RAW >>>" + jwtConfig.getLoginUrl() + "<<<");

        String jwt = Jwts.builder()
        .issuer(jwtConfig.getClientId())
        .subject(jwtConfig.getUsername())
        //.audience().add(jwtConfig.getLoginUrl()).and()
        .claim("aud", jwtConfig.getLoginUrl())
        .expiration(exp)
        .issuedAt(now)
        .signWith(privateKey, Jwts.SIG.RS256)
        .compact();

        String payload = jwt.split("\\.")[1];

        System.out.println(
                "JWT PAYLOAD >>> " +
                new String(Base64.getUrlDecoder().decode(payload)) +
                " <<<"
        );

return jwt;

        /*return Jwts.builder()
                .issuer(jwtConfig.getClientId())
                .subject(jwtConfig.getUsername())
                .audience().add(jwtConfig.getLoginUrl()).and()
                .expiration(exp)
                .issuedAt(now)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact(); */
    }

    private PrivateKey loadPrivateKey(String keyOrPath) throws Exception {
        String keyContent;
        if (keyOrPath.contains("-----BEGIN")) {
            // Treat as raw key content
            keyContent = keyOrPath;
        } else if (keyOrPath.startsWith("classpath:")) {
            // Load from classpath
            String resourcePath = keyOrPath.substring("classpath:".length());
            try (var inputStream = new org.springframework.core.io.ClassPathResource(resourcePath).getInputStream()) {
                keyContent = new String(inputStream.readAllBytes());
            }
        } else {
            // Load from file system
            keyContent = new String(Files.readAllBytes(Paths.get(keyOrPath)));
        }

        String privateKeyPEM = keyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }
}
