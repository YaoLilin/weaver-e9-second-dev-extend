package com.engine.interfaces.secdev.extend.workflow.domain.dto;

import lombok.Data;

/**
 * @author 姚礼林
 * @desc 流程信息
 * @date 2026/1/27
 **/
@Data
public class WorkflowInfoItem {
    private Integer id;
    private String workflowName;
    private String typeName;
    private Integer version;
    private Integer formId;
    private String tableName;
}
