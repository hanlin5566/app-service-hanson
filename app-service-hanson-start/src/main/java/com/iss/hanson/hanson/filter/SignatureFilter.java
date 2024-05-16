package com.iss.hanson.hanson.filter;

import com.alibaba.fastjson.JSONObject;
import com.hanson.util.RequestUtils;
import com.hanson.wrapper.request.RequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.iss.hanson.hanson.common.constant.Constant.*;

@Component
@Slf4j
public class SignatureFilter implements Filter {
    private static HashMap<String, String> secretKeyMap = new HashMap<String, String>();

    private static final String FILTER_SWITCH_KEY = "signature:filterSwitch";

    private static final String INTERCEPT_LIST_KEY = "signature:interceptList"; // springboot全部验证，不需要要加必须验证

    private static final String WHITE_LIST_KEY = "signature:filterWhiteList";

    private static final String BLACK_LIST_KEY = "signature:blackIntercept";

    private static final String ErrorRate_LIST_KEY = "signature:errorRate";

    private static final String STATIC_RES_DIR_START_LIST_KEY = "signature:whiteDirStartList";

    private static final String STATIC_RES_LIST_KEY = "signature:staticResList";

    public static final String SUCCESS = "success";

    public static final String FORBIDDEN = "forbidden";

    private static final String CRONTASK_TYPE = "cronTask";

    private static final Integer SIGN_FILTER_TIME_OUT = 60;


    private static final String PUBLIC_KEY = "NvcGUiOlsiQVBQVF9BRE1JTiIsIm9hdXRoMi1yZXNvdXJjZSJdfQ.euah87VlFHnukc3gPy0bsfmnKmIaXPB-RostDmnwPo4ZW50";

    private static final String AUTH_FAILED = "{ \"code\": \"401\", \"message\": \"验签失败\",\"data\": null,\"success\": true}";

    private static List<String> staticResourceDirList = Arrays.asList("health","actuator","error","webjars","swagger-resources","csrf","html","api-docs");

    private List<String> whiteList = Arrays.asList("/libs", "/mnt", "/index", "/etc", "/mnt", "/system", "/favicon", "/aem", "/assets","1.json");

    private List<String> staticResSuffix = Arrays.asList(".js", ".css", ".mp4", ".gif", ".jpg", ".png", ".jpeg", ".ico", ".json", ".userprops.html");

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @Value("${com.hanson.user-interceptor.excludePathPatterns:}")
    private String excludePathPatterns;

    @Override
    public void init(FilterConfig filterConfig) {
        log.debug("进入了init方法...(初始化的时候进入init方法...)");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (isURIExcluded(((HttpServletRequest)request).getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        ServletRequest requestWrapper=null;
        if(request instanceof HttpServletRequest) {
            requestWrapper=new RequestWrapper((HttpServletRequest)request);
        }
        boolean switchStatus = getSwitchStatus();
        //获取请求路径
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String fullUrl =httpServletRequest.getScheme()+"://"+ httpServletRequest.getServerName()+httpServletRequest.getRequestURI()+"?"+httpServletRequest.getQueryString();
        log.debug("fullUrl:"+fullUrl);
        String path = getRelativePath(httpServletRequest);
        log.debug("path: [" + path + "] .");
        // 直接拦截
        if (blackIntercept(httpServletRequest)) {
            // String path = getRelativePath(httpServletRequest);
            log.debug("黑名单拦截: [" + path + "] .");
            try {
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                httpServletResponse.setStatus(403);
                httpServletResponse.setContentType("application/json; charset=utf-8");
                httpServletResponse.setCharacterEncoding("UTF-8");
                PrintWriter writer = httpServletResponse.getWriter();
                writer.print(FORBIDDEN);
                writer.flush();
                writer.close();
                return;
            } catch (Exception e) {
                log.error("Output 403 to browser error." + e.getMessage());
                return;
            }
        }

        if (switchStatus && needVerifySign(httpServletRequest)) {
            String verifyResult = verifySignString(request, (HttpServletRequest)requestWrapper);
            log.debug(getRelativePath(httpServletRequest) + " verify sign result:" + verifyResult);
            if (!StringUtils.equals(SUCCESS, verifyResult)) {
                try {
                    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                    httpServletResponse.setStatus(401);
                    httpServletResponse.setContentType("application/json; charset=utf-8");
                    httpServletResponse.setCharacterEncoding("UTF-8");

                    PrintWriter writer = httpServletResponse.getWriter();
                    writer.print(verifyResult);
                    writer.flush();
                    writer.close();
                    return;
                } catch (Exception e) {
                    log.error("Output 401 to browser error." + e.getMessage());
                    return;
                }
            }
        }
        if(requestWrapper==null) {
            chain.doFilter(request, response);
        }else {
            chain.doFilter(requestWrapper, response);
        }
    }

    @Override
    public void destroy() {
    }

    /**
     * 检查此过滤器开关的状态，如果为空，或者设置为OFF，就软校验。如果开关开启，就硬校验
     * 如果redis本身抛异常，也软校验
     *
     * @return
     */
    private boolean getSwitchStatus() {
        try {
            String filterSwitch = redisTemplate.opsForValue().get(FILTER_SWITCH_KEY);
            return StringUtils.isNotEmpty(filterSwitch) && StringUtils.equals(filterSwitch.toUpperCase(), "ON");
        } catch (Exception e) {
            log.error("get filterSwitch from redis failed. ", e);
            return false;
        }
    }

    /**
     * 检查是否需要验签
     * 如果请求路径是 / ,直接放行
     * 从redis中获取白名单，与默认白名单合并，然后检查请求路径是否以白名单开头，如果是则放行，如果不是则验签。
     *
     * @param request
     * @return
     */
    private boolean needVerifySign(HttpServletRequest request) {
        String pathInfo = getRelativePath(request);
        if ("/".equals(pathInfo)) {
            return false;
        }

        if (isStaticResource(pathInfo)) {
            log.debug("pathInfo: [" + pathInfo + "] is static resource. will not verify sign.");
            return false;
        }
        // json文件需要去黑名单验证，有可能存在接口
        if (isJsonValidate(pathInfo)) {
            log.debug("isJsonValidate: [" + pathInfo + "] is json file. will verify in blacklist.");
            return false;
        }

        // 静态资源目录过滤
        if (isStaticResourceDir(pathInfo)) {
            log.debug("isStaticResourceDir: [" + pathInfo + "] is static dir. will not verify sign.");
            return false;
        }
        if (inWhiteList(pathInfo) || pathInfo.contains("1.json")) {
            log.debug("pathInfo: [" + pathInfo + "] is in white list. will not verify sign.");
            return false;
        }
        return true;
    }

    private boolean isStaticResource(String resourcePath) {
        List<String> arrayList = new ArrayList<>(staticResSuffix);
        try {
            List<String> lrange = redisTemplate.opsForList().range(STATIC_RES_LIST_KEY, 0, -1);
            if(lrange != null) {
                arrayList.addAll(lrange);
            }
        } catch (Exception e) {
            log.error("Get filter static resource list from redis failed. Will use default static resource list.", e);
        }
        if(arrayList.size() > 0) {
            for (String path : arrayList) {
                if (resourcePath.endsWith(path)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isJsonValidate(String resourcePath) {
        if (resourcePath.endsWith(".json")) {
            // 黑名单中进行查询
            List<String> lrange = new ArrayList<>();
            try {
                lrange = redisTemplate.opsForList().range(INTERCEPT_LIST_KEY, 0, -1);
            } catch (Exception e) {
                log.error("isJsonValidate() Error.Get INTERCEPT_LIST_KEY from redis failed. Will use blockList from " +
                        "tb_filter_list.", e);
            }

            // 如果在拦截列表里，说明不是json文件，而是接口请求
            if(lrange != null) {
                for (String interceptPath : lrange) {
                    if (resourcePath.contains(interceptPath)) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    private boolean isStaticResourceDir(String resourcePath) {
        List<String> arrayList = new ArrayList<>(staticResourceDirList);
        try {
            List<String> lrange = redisTemplate.opsForList().range(STATIC_RES_DIR_START_LIST_KEY, 0, -1);
            arrayList.addAll(lrange);
        } catch (Exception e) {
            log.error("Get filter static resource dir list from redis failed. Will use default static resource dir list.", e);
        }
        for (String path : arrayList) {
            if (resourcePath.contains(path)) {
                return true;
            }
        }
        return false;
    }

    private boolean inWhiteList(String resourcePath) {
        List<String> arrayList = new ArrayList<>(whiteList);
        try {
            List<String> lrange = redisTemplate.opsForList().range(WHITE_LIST_KEY, 0, -1);
            if(lrange != null) {
                arrayList.addAll(lrange);
            }
        } catch (Exception e) {
            log.error("Get filter white list from redis failed. Will use whiteList from tb_filter_list.", e);
        }
        if(arrayList.size() > 0) {
            for (String path : arrayList) {
                // if (resourcePath.startsWith(path)) {
                // app防止各个版本前缀不一样
                if (resourcePath.endsWith(path)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getRelativePath(HttpServletRequest request){
        if (request.getAttribute("javax.servlet.include.request_uri") != null){
            String result = (String)request.getAttribute("javax.servlet.include.path_info");
            if (result == null) {
                result = (String)request.getAttribute("javax.servlet.include.servlet_path");
            } else {
                result = (String)request.getAttribute("javax.servlet.include.servlet_path") + result;
            }
            if ((result == null) || (result.equals(""))) {
                result = "/";
            }
            return result;
        }
        String result = request.getPathInfo();
        if (result == null) {
            result = request.getServletPath();
        } else {
            result = request.getServletPath() + result;
        }
        if ((result == null) || (result.equals(""))) {
            result = "/";
        }
        return result;
    }

    // 直接拦截的黑名单
    private boolean blackIntercept(HttpServletRequest request) {
        String path = getRelativePath(request);

        List<String> lrange = new ArrayList<>();
        try {
            lrange = redisTemplate.opsForList().range(BLACK_LIST_KEY, 0, -1);
        } catch (Exception e) {
            log.debug("Get blackIntercept from redis failed.", e);
            // return true;
        }
        // 如果在拦截列表里，直接拦截
        if(lrange != null && lrange.size()>0) {
            for (String interceptPath : lrange) {
                if (path.startsWith(interceptPath)) {
                    log.debug("拦截：" + interceptPath);
                    return true;
                }
            }
        }
        return false;
    }


    // 一些第三方验签失败，不影响错误率error rate
    private boolean checkErrorRate(String path) {
        List<String> lrange = new ArrayList<>();
        try {
            lrange = redisTemplate.opsForList().range(ErrorRate_LIST_KEY, 0, -1);
        } catch (Exception e) {
            log.error("Get ErrorRate_LIST_KEY from redis failed.", e);
            // return true;
        }
        // 如果在拦截列表里，直接拦截
        if(lrange != null && lrange.size()>0) {
            for (String interceptPath : lrange) {
                log.debug("拦截：" + interceptPath);
                if (path.startsWith(interceptPath)) {
                    return false;
                }
            }
        }
        return true;
    }


    private HashMap<String, String> getSecretKeyMap() {
        HashMap<String, String> keyWordMap = null;
        try {
            keyWordMap = JSONObject.parseObject(redisTemplate.opsForValue().get("appKey"), HashMap.class);
        } catch (Exception e) {
            log.error("getEnvValue", e);
        }
        if(keyWordMap == null || keyWordMap.size() <=0) {
            log.debug("RequestFilter env: {}", activeProfile);
            if (activeProfile.equals("stage") || activeProfile.equals("uat") || activeProfile.equals("dev")) {
                secretKeyMap.put("hanson-web", "X2NsaWVudElkIiwic2NvcGUiOlsib2F1dGgyLXJlc29");
                secretKeyMap.put("hanson-exta", "OiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbW");
                secretKeyMap.put("hanson-extb", "F1dGhvcml0aWVzIjpbIkFQUFRfQURNSU5fUkVBRCIsI");
                secretKeyMap.put("hanson-extc", "GhfcGVybWFuZW50X2NsaWVudElkIiwic2NQVF9BRE1J");
                secretKeyMap.put("hanson-extd", "WEyMWUtNWViMmExOGNjYjJkIiwiY2xpZW50X2lkIjoi");
                secretKeyMap.put("hanson-exte", "VyX25hbWUiOiJ1YV9hZG1pbiIsImF1dGhvcml0aWVzI");
                secretKeyMap.put("hanson-extf", "VyX25hbWUiOiJ1YV9hZG1pbiIIUdfE098Vnooo90TYP");  // admin
                secretKeyMap.put("hanson-extg", "0136825E650297E436CE9F3C18A659ACIUIYUYUY356");  // 安卓
                secretKeyMap.put("hanson-exth", "B22CB2531E645BAC6F8F078D172C1321eerrUYUU122");  // ios
                secretKeyMap.put("hanson-exti", "F92E2FF249C65B533E1705F829FB1C5F92Wdfr0Iuur");
                secretKeyMap.put("hanson-extj", "A34B8D2BBBE718CCC550D0EAAD6D9F90767677770we");
            } else {  // activeProfile.equals("prd")
                secretKeyMap.put("hanson-web", "F1dGhvcml0aWVzIjpbIkFQUFRfQURNSU5fUkVBRCIsI");
                secretKeyMap.put("hanson-exta", "YzA5YWI5ZjBlIiwiY2xpZW50X2lkIjoidWFfcGVybWF");
                secretKeyMap.put("hanson-extb", "WVzIjpbIkNPTkZJR19BRE1JTl9SRUFEIiwiQ09ORsyT");
                secretKeyMap.put("hanson-extc", "G1pbiIsImF1dGhvcml0aWVzIjpbIkFQUFRfQURNSU5f");
                secretKeyMap.put("hanson-extd", "MjEtZjc4MjE0ZGM4YzA2IiwiY2xpZW50X2lkIjoidHJ");
                secretKeyMap.put("hanson-exte", "URNSU5fV1JJVEUiXSwianRpIjoiZjk2M2JjNTQ4Iiwi");
                secretKeyMap.put("hanson-extf", "VyX25KH9R0L690Rj87sX902w8990SDKLit932VmlFOW");  // admin
                secretKeyMap.put("hanson-extg", "0D8BF64A9C46AC0D19D66047478B7E95kdfdkfk099E");  // 安卓
                secretKeyMap.put("hanson-exth", "659E3081A2D47D9E4FA2A24F03289935esdd989op71");  // ios
                secretKeyMap.put("hanson-exti", "20C765152A9757F142CBD5FCCDC7BE93ghgh77RSF98");
                secretKeyMap.put("hanson-extj", "D3160392AA98EE8F5CDE7A43CF8B8B0Bhytrdjcds54");
            }

            if(secretKeyMap.size() >0) {
                redisTemplate.opsForValue().set("appkey" ,JSONObject.toJSONString(secretKeyMap));
            }
            return secretKeyMap;
        } else {
            return keyWordMap;
        }
    }

    /**
     * 获取请求体内容
     * @return
     */
    private String getParamsFromRequestBody(HttpServletRequest request){
        BufferedReader br = null;
        String listString = "";
        try {
            br = request.getReader();
            String str = "";
            while ((str = br.readLine()) != null) {
                listString += str;
            }
        } catch (IOException e) {
            // e.printStackTrace();
            log.error("getParamsFromRequestBody error:{}", e);
        }
        return listString;
    }

    /**
     * 从请求中获取 sign ，timestamp, appId。
     * 如果任意一项为空或者为空字符串，则验签失败。
     * 连接timestamp，secret_key, appId, 然后使用MD5加密。判断是否与sign一致，一致则放行，否则验签失败。
     * 5分钟内，同一个应用在同timestamp不能发送两次请求，否则验签失败。
     *
     * @param request
     * @return
     */
    public String verifySignString(ServletRequest request, HttpServletRequest requestWrapper) {
        String body = getParamsFromRequestBody(requestWrapper);
        log.debug("body:"+body);
        String sign = request.getParameter(SIGNATURE);
        String timestamp = request.getParameter(SIGNATURE_TIMESTAMP);
        String appId = request.getParameter(SIGNATURE_APP_ID);
        if (sign == null || timestamp == null || appId == null) {
            if(StringUtils.isNotBlank(body) && !"".equals(body) && body.contains(SIGNATURE)) {
                JSONObject jsonObject = JSONObject.parseObject(body);
                if (jsonObject.size() > 0) {
                    sign = StringUtils.isNotBlank(jsonObject.getString(SIGNATURE)) ? jsonObject.getString(SIGNATURE) : null;
                    timestamp = StringUtils.isNotBlank(jsonObject.getString(SIGNATURE_TIMESTAMP)) ? jsonObject.getString(SIGNATURE_TIMESTAMP) : null;
                    appId = StringUtils.isNotBlank(jsonObject.getString(SIGNATURE_APP_ID)) ? jsonObject.getString(SIGNATURE_APP_ID) : null;
                }
            }
            //取header
            if (StringUtils.isBlank(sign) || StringUtils.isBlank(timestamp)  || StringUtils.isBlank(appId) ) {
                sign = requestWrapper.getHeader(SIGNATURE);
                timestamp = requestWrapper.getHeader(SIGNATURE_TIMESTAMP);
                appId = requestWrapper.getHeader(SIGNATURE_APP_ID);
            }
            if (sign == null || timestamp == null || appId == null) {
                verifyFail(request,"param is null." + "sign=" + sign + ", timestamp=" + timestamp + ", appId=" + appId);
                return AUTH_FAILED;
            }
        }
        if (StringUtils.isBlank(sign) || StringUtils.isBlank(appId) || StringUtils.isBlank(timestamp)) {
            verifyFail(request,"Any of sign, appId, timestamp is blank." + "sign=" + sign + ", timestamp=" + timestamp + ", appId=" + appId);
            return AUTH_FAILED;
        }
        HashMap<String, String> combinedSecretKeyMap = getSecretKeyMap();
        // log.debug("secretKey:"+combinedSecretKeyMap.get(appId));
        String md5Key = timestamp + combinedSecretKeyMap.get(appId) + appId;
        String encoded = DigestUtils.md5DigestAsHex(md5Key.getBytes());
        // 防止重放
        try {
            String signFilterKey = SIGNATURE_FILTER+":"+sign;
            String savedSign = redisTemplate.opsForValue().get(signFilterKey);
            if (savedSign != null) {
                verifyFail(request,"Visit too frequently." + "sign:" + sign + ",timestamp:" + timestamp + ", appId:" + appId + "," + "encode:" + encoded);
                redisTemplate.opsForValue().set(signFilterKey, sign, SIGN_FILTER_TIME_OUT, TimeUnit.SECONDS);
                return AUTH_FAILED;
            }
            redisTemplate.opsForValue().set(signFilterKey, sign, SIGN_FILTER_TIME_OUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("verifySignString error:sign:{},timestamp:{},appId:{},type:{},publicKey:{}",sign,timestamp,appId,e);
        }

        if (!StringUtils.equals(encoded, sign)) {
            verifyFail(request,"Parse sign failed.(not match)" + "sign:" + sign + ",timestamp:" + timestamp + ", appId:" + appId + "," + "encode:" + encoded);
            return AUTH_FAILED;
        }
        return SUCCESS;
    }

    // 验签失败记录日志
    private void verifyFail(ServletRequest request, String msg) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String path = getRelativePath(httpServletRequest);
        if(checkErrorRate(path)) {
            log.debug(msg);
        }else{
            log.warn(msg);
        }
    }

    private boolean isURIExcluded(String requestUri) {
        List<String> paths = RequestUtils.parseToUrlPatterns(excludePathPatterns);

        for(int i = 0; i < paths.size(); ++i) {
            String path = (String)paths.get(i);
            if (requestUri.contains(path)) {
                return true;
            }
        }
        return false;
    }
}