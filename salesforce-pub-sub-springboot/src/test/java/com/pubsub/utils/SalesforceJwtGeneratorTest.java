package com.pubsub.utils;

import com.pubsub.config.SalesforceJwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class SalesforceJwtGeneratorTest {

    private SalesforceJwtGenerator jwtGenerator;
    private SalesforceJwtConfig jwtConfig;
    private Path tempKeyPath;

    @BeforeEach
    void setUp() throws Exception {
        // Generate a real RSA key pair for the test
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        byte[] privateKey = pair.getPrivate().getEncoded();

        String base64Key = Base64.getEncoder().encodeToString(privateKey);
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN PRIVATE KEY-----\n");
        for (int i = 0; i < base64Key.length(); i += 64) {
            pem.append(base64Key, i, Math.min(i + 64, base64Key.length())).append("\n");
        }
        pem.append("-----END PRIVATE KEY-----");

        tempKeyPath = Files.createTempFile("test-key", ".pem");
        Files.writeString(tempKeyPath, pem.toString());

        jwtConfig = new SalesforceJwtConfig();
        jwtConfig.setClientId("test-client-id");
        jwtConfig.setUsername("test-user@example.com");
        jwtConfig.setLoginUrl("https://login.salesforce.com");
        jwtConfig.setPrivateKeyPath(tempKeyPath.toAbsolutePath().toString());

        jwtGenerator = new SalesforceJwtGenerator(jwtConfig);
    }

    @Test
    void generateToken_ReturnsSignedJwt() throws Exception {
        String token = jwtGenerator.generateToken();
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
        
        Files.deleteIfExists(tempKeyPath);
    }
}