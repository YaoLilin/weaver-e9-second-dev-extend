package com.engine.interfaces.secdev.extend.api.domain.dto;

import com.engine.interfaces.secdev.extend.api.domain.enums.AssignmentMethod;
import com.engine.interfaces.secdev.extend.api.domain.vo.AssignmentValue;
import lombok.Data;

/**
 * @author 姚礼林
 * @desc 赋值对象
 * @date 2026/1/10
 **/
@Data
public class Assignment {
    /**
     * 赋值方式（枚举类型：表单字段、系统参数、固定值）
     */
    private AssignmentMethod method;

    /**
     * 值对象
     */
    private AssignmentValue value;
}
