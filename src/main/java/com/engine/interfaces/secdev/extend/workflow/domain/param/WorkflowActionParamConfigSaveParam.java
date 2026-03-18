package com.engine.interfaces.secdev.extend.workflow.domain.param;

import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowActionAdvanceParamDTO;
import lombok.Data;

import java.util.List;

/**
 * @author 姚礼林
 * @desc 保存流程Action参数配置请求参数
 * @date 2026/1/10
 **/
@Data
public class WorkflowActionParamConfigSaveParam {
    /**
     * Action标识
     */
    private String actionId;

    /**
     * 参数配置列表（树形结构）
     */
    private List<WorkflowActionAdvanceParamDTO> configs;
}
