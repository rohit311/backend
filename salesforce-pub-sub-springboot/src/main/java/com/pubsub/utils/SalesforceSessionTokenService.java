package com.pubsub.utils;

import io.grpc.CallCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesforceSessionTokenService {

    private static final String ERROR_INVALID_URI = "Invalid URI syntax: {}";

    private static final String LOG_BEARER_TOKEN_SUCCESS = "Salesforce Bearer token acquired successfully.";
    private static final String LOG_BEARER_TOKEN_MISSING = "Salesforce connection parameters may be incorrect. Bearer token not returned.";

    private final SalesforceJwtTokenService jwtTokenService;

    public CallCredentials login() {
        return parseSuccessfulResponse(jwtTokenService.exchangeToken());
    }

    private CallCredentials parseSuccessfulResponse(OAuthResponse oAuthResponse) {
        if (oAuthResponse == null || !StringUtils.hasText(oAuthResponse.getAccessToken())) {
            log.warn(LOG_BEARER_TOKEN_MISSING);
            return null;
        }

        log.info(LOG_BEARER_TOKEN_SUCCESS);
        log.info("Rohit Oauth: "+ oAuthResponse.getInstanceUrl());
        APISessionCredentials credentials = new APISessionCredentials();
        credentials.setInstanceURL(oAuthResponse.getInstanceUrl());
        credentials.setToken(oAuthResponse.getAccessToken());
        credentials.setTenantId(extractOrganizationId(oAuthResponse.getId()));
        return credentials;
    }

    private String extractOrganizationId(String url) {
        if (url == null) return null;
        try {
            String[] segments = new URI(url).getPath().split("/");
            for (int i = 0; i < segments.length - 1; i++) {
                if ("id".equals(segments[i])) {
                    return segments[i + 1];
                }
            }
        } catch (URISyntaxException e) {
            log.error(ERROR_INVALID_URI, e.getMessage());
        }
        return null;
    }

}
