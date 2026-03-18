package com.engine.interfaces.secdev.extend.api.domain.vo;

import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowFieldValue;
import lombok.Data;

/**
 * @author 姚礼林
 * @desc 赋值值对象
 * @date 2026/1/10
 **/
@Data
public class AssignmentValue {
    /**
     * 流程字段值对象（赋值方式为表单字段时使用）
     */
    private WorkflowFieldValue workflowField;

    /**
     * 值（类型为字符串，赋值方式为非表单字段时使用，比如存储固定值、系统参数）
     * 当赋值方式为系统参数时，存储系统参数代码
     * 当赋值方式为固定值时，存储固定值
     */
    private String value;
}
