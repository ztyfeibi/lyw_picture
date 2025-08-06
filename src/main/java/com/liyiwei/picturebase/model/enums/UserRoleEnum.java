package com.liyiwei.picturebase.model.enums;

import io.micrometer.common.util.StringUtils;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum UserRoleEnum {

    USER("普通用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        if (StringUtils.isBlank(value)) return null;

        /**
         * Collectors.toMap(UserRoleEnum::getValue,userRoleEnum -> userRoleEnum)
         * 表示用 getValue() 的返回值做 key，枚举对象本身做 value，最终得到一个 Map<String, UserRoleEnum>。
         */
        Map<String,UserRoleEnum> userRoleEnumMap = Arrays.stream(UserRoleEnum.values()).
                collect(Collectors.toMap(UserRoleEnum::getValue,userRoleEnum -> userRoleEnum));

        return userRoleEnumMap.getOrDefault(value,null);
    }

}
