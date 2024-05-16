package com.iss.hanson.hanson.common.util;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.RegisteredPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static com.iss.hanson.hanson.common.constant.Constant.TOKEN_USER_CODE;

/**
 * @author JunoSong
 * @version V1.0
 * @Package com.iss.hanson.ua.common.util
 * @date 2022/11/9 9:47
 */
@Component
@Slf4j
public class JwtUtil {
    @Value("${com.hanson.jwt.expireTime:3600000}")
    private Long empireTime;
    @Value("${com.hanson.jwt.key:}")
    private String key;

    /**
     * 根据（人员编码）生成token
     *
     * @param userCode 用户ID（人员编码）
     * @return jwt token
     */
    public String createToken(String userCode) {
        HashMap<String, Object> map = new HashMap<String, Object>(3) {
            private static final long serialVersionUID = 1L;
            {
                put(TOKEN_USER_CODE, String.valueOf(userCode));
                put(RegisteredPayload.EXPIRES_AT, System.currentTimeMillis() + empireTime);
            }
        };
        return JWTUtil.createToken(map, key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 验证token是否有效，时间是否有效
     *
     * @param token token数据
     * @return token检验是否有效：true-有效，false-无效
     */
    public boolean verifyToken(String token) {
        // 校验Key是否有效
        if (!JWTUtil.verify(token, key.getBytes(StandardCharsets.UTF_8))) {
            return false;
        }
        // 校验token日志
        try {
            JWTValidator.of(token).validateDate();
        } catch (ValidateException validateException) {
            log.info("token[{}]校验失败:[{}]", token, validateException.getMessage());
            return false;
        }

        return true;
    }
    public boolean verifyToken(String token,String staffCode) throws ValidateException{
        if (!JWTUtil.verify(token, key.getBytes(StandardCharsets.UTF_8))) {
            return false;
        }
        Object uid = JWT.of(token).getPayload(TOKEN_USER_CODE);
        String userId = String.valueOf(uid);
        if (!staffCode.equals(userId)) {
            return false;
        }
        JWTValidator.of(token).validateDate();
        return true;
    }
}
