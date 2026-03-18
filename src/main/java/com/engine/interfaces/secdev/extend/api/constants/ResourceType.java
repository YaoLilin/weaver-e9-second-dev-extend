package com.engine.interfaces.secdev.extend.api.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.jetbrains.annotations.Nullable;

/**
 * @author 姚礼林
 * @desc 接口参数映射配置中的数据来源
 * @date 2026/1/29
 **/
public enum ResourceType {
    /**
     * 流程
     */
    WORKFLOW(0,"流程"),
    /**
     * 建模
     */
    MODE(1,"建模");

    private final Integer value;
    private final String name;

    ResourceType(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    @JsonValue
    public Integer getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    @JsonCreator
    @Nullable
    public static ResourceType getByValue(Integer value) {
        for (ResourceType item : values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return null;
    }
}
