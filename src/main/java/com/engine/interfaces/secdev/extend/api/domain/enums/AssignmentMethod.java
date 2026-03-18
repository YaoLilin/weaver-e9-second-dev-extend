package com.engine.interfaces.secdev.extend.api.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.jetbrains.annotations.Nullable;

/**
 * @author 姚礼林
 * @desc 赋值方式枚举
 * @date 2026/1/10
 **/
public enum AssignmentMethod {
    /**
     * 表单字段
     */
    FORM_FIELD(1, "表单字段"),

    /**
     * 系统参数
     */
    SYSTEM_PARAM(2, "系统参数"),

    /**
     * 固定值
     */
    FIXED_VALUE(3, "固定值");

    private final int value;
    private final String displayName;

    AssignmentMethod(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    @Nullable
    public static AssignmentMethod fromValue(int value) {
        for (AssignmentMethod item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
