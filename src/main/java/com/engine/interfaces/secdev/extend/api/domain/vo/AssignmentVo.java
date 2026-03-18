package com.engine.interfaces.secdev.extend.api.domain.vo;

import lombok.Data;

/**
 * @author 姚礼林
 * @desc 赋值对象
 * @date 2026/1/29
 **/
@Data
public class AssignmentVo {
    /**
     * 赋值方式（枚举类型：表单字段、系统参数、固定值）
     */
    private Integer method;

    /**
     * 值对象
     */
    private AssignmentValue value;
}
