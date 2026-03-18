package com.engine.interfaces.secdev.extend.workflow.domain.dto;

import lombok.Data;

/**
 * @author 姚礼林
 * @desc 流程 Action 参数信息
 * @date 2025/10/17
 **/
@Data
public class WorkflowActionParamDto {
    private String paramName;
    private String displayName;
    private String defaultValue;
    private String desc;
    private boolean required;
}
