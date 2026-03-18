package com.engine.interfaces.secdev.extend.workflow.domain.dto;

import lombok.Data;

/**
 * @author 姚礼林
 * @desc 流程字段值对象
 * @date 2026/1/10
 **/
@Data
public class WorkflowFieldValue {
    /**
     * 是否为主表（true-主表，false-明细表）
     */
    private Boolean isMainTable;

    /**
     * 明细表序号（类型为 int，从 1 开始，比如明细表1的序号就是1）
     * 当 isMainTable 为 true 时，此字段可为 null
     */
    private Integer detailTableNum;

    /**
     * 字段数据库名
     */
    private String fieldName;

    /**
     * 赋值显示名称（比如：表单字段：日期(rq)，其中的日期为字段中文名）
     */
    private String displayName;
}
