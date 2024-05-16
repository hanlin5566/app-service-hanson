package com.iss.hanson.hanson.interceptor;

import cn.hutool.jwt.JWT;
import com.hanson.rest.BizException;
import com.hanson.rest.enmus.ErrorCodeEnum;
import com.hanson.util.RequestUtils;
import com.iss.hanson.hanson.common.bo.UserInfoBo;
import com.iss.hanson.hanson.common.enums.error.UserErrorCodeEnum;
import com.iss.hanson.hanson.common.util.JwtUtil;
import com.iss.hanson.hanson.common.util.UserThreadLocalUtil;
import com.iss.hanson.hanson.serevice.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

import static com.iss.hanson.hanson.common.constant.Constant.*;

/**
 * @author: HansonHu
 * @date: 2022-09-15 11:01
 **/
@Component
@Slf4j
public class UserInfoInterceptor implements HandlerInterceptor {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${com.hanson.user-interceptor.excludePathPatterns:}")
    private String excludePathPatterns;

    /**
     * 请求执行前执行的，将用户信息放入ThreadLocal
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isURIExcluded(request.getRequestURI())) {
            return true;
        }
        String token = request.getHeader(TOKEN);
        if(StringUtils.isBlank(token)){
            throw new BizException(UserErrorCodeEnum.TOKEN_IS_BLANK);
        }
        if (!jwtUtil.verifyToken(token)) {
            throw new BizException(UserErrorCodeEnum.TOKEN_IS_INVALID);
        }
        String userCode = String.valueOf(JWT.of(token).getPayload(TOKEN_USER_CODE));
        UserInfoBo userInfoBo = userInfoService.getByCode(userCode);
        if (Objects.isNull(userInfoBo)) {
            return false;
        }
        UserThreadLocalUtil.addCurrentUser(userInfoBo);
        return true;
    }

    /**
     * 接口访问结束后，从ThreadLocal中删除用户信息
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserThreadLocalUtil.remove();
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
