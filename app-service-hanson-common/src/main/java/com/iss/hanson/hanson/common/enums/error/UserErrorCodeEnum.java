package com.iss.hanson.hanson.common.enums.error;

import com.hanson.rest.enmus.ErrorCode;
import lombok.Getter;

/**
 * @author Hanson
 * @date 2022/1/11  16:12
 */
@Getter
public enum UserErrorCodeEnum implements ErrorCode {
    FROZEN_USER("000101", "com.hanson.user.frozen"),
    NO_SUCH_USER("000102", "com.hanson.user.no_such_user"),
    ALREADY_EXISTS_USER("000103", "com.hanson.user.already_exists_user"),
    NO_AUTHORITY_USER("000104", "com.hanson.user.no_authority_user"),
    DUPLICATE_STAFF_CODE("000105", "com.hanson.user.duplicate_staff_code"),
    TOKEN_IS_INVALID("000106", "com.hanson.user.token_is_invalid"),
    TOKEN_IS_BLANK("000107", "com.hanson.user.token_is_blank"),
    USER_LOGIN_EXPIRE("000108", "com.hanson.user.user_login_expire"),
    NO_AUTHORITY_ROLE("000109", "com.hanson.user.no_authority_role")
    ;

    private String code;
    private String message;

    private UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
