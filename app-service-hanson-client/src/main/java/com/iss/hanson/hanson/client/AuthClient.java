package com.iss.hanson.hanson.client;

import com.iss.hanson.hanson.client.dto.auth.AccessTokenResp;
import com.iss.hanson.hanson.client.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Hanson
 * @date 2022/3/21  12:14
 * 外部鉴权接口，随业务修改
 */
@Slf4j
public class AuthClient {
    @Value("${external.auth.client.url}")
    private String clientUrl;
    @Value("${external.auth.client.id}")
    private String clientId;
    @Value("${external.auth.client.secret}")
    private String clientSecret;

    @Value("${external.digital.client.url}")
    private String digitalClientUrl;
    @Value("${external.digital.client.id}")
    private String digitalClientId;
    @Value("${external.digital.client.secret}")
    private String digitalClientSecret;

    private ConcurrentHashMap<String, AccessTokenResp> accessTokenMap = new ConcurrentHashMap<>();

    @Autowired
    private RestTemplate restTemplate;

    public AccessTokenResp getAccessToken(String scope) {
        if (accessTokenMap.containsKey(scope)) {
            AccessTokenResp accessTokenResp = accessTokenMap.get(scope);
            long expires = System.currentTimeMillis() - accessTokenResp.getGenMillisecond();
            log.warn("scope:{} genMillisecond:{} expiresIn:{} expires:{}", scope, accessTokenResp.getGenMillisecond(), accessTokenResp.getExpiresIn(), expires);
            //未过期，返回
            if (expires < Long.parseLong(accessTokenResp.getExpiresIn()) * 1000) {
                return accessTokenMap.get(scope);
            }
            //已过期移除,重新获取
            log.warn("scope:{} expire", scope);
        }
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("grant_type", "client_credentials");
        postParameters.add("scope", scope);
        postParameters.add("client_id", clientId);
        postParameters.add("client_secret", clientSecret);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(postParameters, headers);
        if (clientUrl.startsWith("https")) {
            HttpUtils.ignoreSsl();
        }
//        restTemplate.getInterceptors().add(new RestTemplateLogInterceptor());
        AccessTokenResp accessTokenResp = restTemplate.postForObject(clientUrl, entity, AccessTokenResp.class);
        accessTokenResp.setGenMillisecond(System.currentTimeMillis());
        accessTokenMap.put(scope, accessTokenResp);
        return accessTokenResp;
    }

    public AccessTokenResp getDigitalAccessToken(String scope) {
        if (accessTokenMap.containsKey(scope)) {
            AccessTokenResp accessTokenResp = accessTokenMap.get(scope);
            long expires = System.currentTimeMillis() - accessTokenResp.getGenMillisecond();
            log.warn("scope:{} genMillisecond:{} expiresIn:{} expires:{}", scope, accessTokenResp.getGenMillisecond(), accessTokenResp.getExpiresIn(), expires);
            //未过期，返回
            if (expires < Long.parseLong(accessTokenResp.getExpiresIn()) * 1000) {
                return accessTokenMap.get(scope);
            }
            //已过期移除,重新获取
            log.warn("scope:{} expire", scope);
        }
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("grant_type", "client_credentials");
        postParameters.add("scope", scope);
        postParameters.add("client_id", digitalClientId);
        postParameters.add("client_secret", digitalClientSecret);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(postParameters, headers);
        if (clientUrl.startsWith("https")) {
            HttpUtils.ignoreSsl();
        }
//        restTemplate.getInterceptors().add(new RestTemplateLogInterceptor());
        AccessTokenResp accessTokenResp = restTemplate.postForObject(digitalClientUrl, entity, AccessTokenResp.class);
        accessTokenResp.setGenMillisecond(System.currentTimeMillis());
        accessTokenMap.put(scope, accessTokenResp);
        return accessTokenResp;
    }

    public void addToken(HttpRequest request) {
        String scope = request.getHeaders().getFirst("scope");
        AccessTokenResp accessToken = getAccessToken(scope);
        HttpHeaders headers = request.getHeaders();
        headers.add("Authorization", accessToken.getTokenType() + " " + accessToken.getAccessToken());
        headers.add("Host", HttpUtils.getIpAddress());
    }
}