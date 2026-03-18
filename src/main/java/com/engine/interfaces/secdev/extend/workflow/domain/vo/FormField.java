package com.engine.interfaces.secdev.extend.workflow.domain.vo;

import lombok.Data;

/**
 * @author 姚礼林
 * @desc 流程字段信息
 * @date 2026/1/10
 **/
@Data
public class FormField {
    /**
     * 字段数据库名
     */
    private String fieldName;

    /**
     * 字段中文名
     */
    private String labelName;

    /**
     * 字段类型
     */
    private String fieldType;
}
