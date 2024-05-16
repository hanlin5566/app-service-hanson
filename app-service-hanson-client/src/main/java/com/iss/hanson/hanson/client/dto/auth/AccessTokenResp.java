package com.iss.hanson.hanson.client.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Hanson
 * @date 2021/8/3  10:36
 */
@Data
public class AccessTokenResp implements Serializable {
    private static final long serialVersionUID = -6306818743581396493L;
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private String expiresIn;
	
    private long genMillisecond;
}
