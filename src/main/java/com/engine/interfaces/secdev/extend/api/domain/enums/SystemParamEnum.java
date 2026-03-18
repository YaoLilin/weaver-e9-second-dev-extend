package com.engine.interfaces.secdev.extend.api.domain.enums;

import org.jetbrains.annotations.Nullable;

/**
 * @author 姚礼林
 * @desc 系统参数枚举
 * @date 2026/1/15
 **/
public enum SystemParamEnum {
    /**
     * 创建人
     */
    CREATOR("CREATOR", "创建人"),
    /**
     * 创建时间
     */
    CREATE_TIME("CREATE_TIME", "创建时间"),
    /**
     * 请求id
     */
    REQUEST_ID("REQUEST_ID", "请求id"),
    /**
     * 流程id
     */
    WORKFLOW_ID("WORKFLOW_ID", "流程id");

    private final String code;
    private final String name;

    SystemParamEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public static SystemParamEnum getByCode(String code) {
        for (SystemParamEnum value : SystemParamEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
