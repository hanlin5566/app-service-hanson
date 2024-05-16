package com.iss.hanson.hanson.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Hanson
 * @date 2021/12/3  16:24
 */
@Getter
@AllArgsConstructor
public enum GenderEnum {
    MALE(1, "男"),
    FEMALE(2, "女"),
    ;

    @EnumValue
    private final int code;

    private final String desc;

    private static Map<Integer, GenderEnum> codeMap;

    static {
        codeMap = Arrays.stream(values()).collect(Collectors.toMap(GenderEnum::getCode, Function.identity(), (k1, k2) -> k1));
    }

    @JsonCreator
    public GenderEnum codeOf(Integer code) {
        return codeMap.getOrDefault(code, null);
    }

}
