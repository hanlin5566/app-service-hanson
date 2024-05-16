package com.iss.hanson.hanson.client.configuration;

import com.iss.hanson.hanson.client.AuthClient;
import com.iss.hanson.hanson.client.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Hanson
 * @date 2022/3/3  16:19
 */
@Slf4j
@Configuration
@DependsOn("httpClientConfig")
public class OAuthRestTemplateConfig {
    @Autowired
    private HttpComponentsClientHttpRequestFactory httpRequestFactory;

    @Bean
    public AuthClient authClient() {
        return new AuthClient();
    }

    @Bean("authRestTemplate")
    public RestTemplate restTemplate(AuthClient authClient) {
        RestTemplate restTemplate = new RestTemplate();

        // 构造加入interceptor	自己实现的interceptor
        List<ClientHttpRequestInterceptor> interceptorList = new ArrayList<>();
        interceptorList.add((request, body, execution) -> {
            authClient.addToken(request);
            URI uri = request.getURI();
            if (uri.getPath().startsWith("https")) {
                HttpUtils.ignoreSsl();
            }
            log.debug("authRestTemplate call uri: {}", request.getURI());
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode().is4xxClientError()) {
                HttpStatus errorCode = response.getStatusCode();
                log.warn("remote request [{}] error, errorCode : {}", request.getURI(), errorCode.value());
                // 需要申请token
                if (HttpStatus.UNAUTHORIZED.value() == errorCode.value()) {
                    log.warn("remote request [{}] token expire, try reExecute", request.getURI());
                    authClient.addToken(request);
                    response = execution.execute(request, body);
                    return response;
                }
            }
            return response;
        });
        InterceptingClientHttpRequestFactory interceptorFactory = new InterceptingClientHttpRequestFactory(
                new BufferingClientHttpRequestFactory(httpRequestFactory), interceptorList);
        restTemplate.setRequestFactory(interceptorFactory);
        return restTemplate;
    }
}
