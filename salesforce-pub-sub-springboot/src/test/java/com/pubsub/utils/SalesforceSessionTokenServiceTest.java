package com.pubsub.utils;

import io.grpc.CallCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SalesforceSessionTokenServiceTest {

    private SalesforceSessionTokenService sessionTokenService;

    @Mock
    private SalesforceJwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sessionTokenService = new SalesforceSessionTokenService(jwtTokenService);
    }

    @Test
    void login_UsesJwtFlow_ReturnsCredentials() {
        OAuthResponse mockResponse = new OAuthResponse();
        mockResponse.setAccessToken("jwt-token");
        mockResponse.setInstanceUrl("https://jwt.salesforce.com");
        mockResponse.setId("https://login.salesforce.com/id/ORGID/USERID");

        when(jwtTokenService.exchangeToken()).thenReturn(mockResponse);

        CallCredentials credentials = sessionTokenService.login();

        assertThat(credentials).isNotNull();
        if (credentials instanceof APISessionCredentials apiCreds) {
            assertThat(apiCreds.getToken()).isEqualTo("jwt-token");
        }
    }
}