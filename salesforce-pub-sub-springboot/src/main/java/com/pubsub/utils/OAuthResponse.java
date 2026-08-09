package com.pubsub.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OAuthResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("signature")
    private String signature;
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("instance_url")
    private String instanceUrl;
    @JsonProperty("id")
    private String id;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("issued_at")
    private String issuedAt;
}
