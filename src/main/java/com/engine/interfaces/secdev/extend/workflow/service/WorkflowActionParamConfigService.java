package com.engine.interfaces.secdev.extend.workflow.service;

import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowActionAdvanceParamDTO;

import java.util.List;

/**
 * @author 姚礼林
 * @desc 流程Action参数配置业务接口
 * @date 2026/1/10
 **/
public interface WorkflowActionParamConfigService {
    /**
     * 保存Action参数配置
     *
     * @param actionId Action标识
     * @param configs 参数配置列表（树形结构，SaveDTO）
     */
    void saveConfig(String actionId, List<WorkflowActionAdvanceParamDTO> configs);

    /**
     * 获取 Action 参数配置（结合 Action 类解析和数据库配置）
     *
     * @param actionId Action 标识
     * @param workflowId 流程 ID（可选，用于动态获取字段中文名）
     * @param actionPath Action 类路径
     * @return 参数配置列表（DTO）
     * @throws ClassNotFoundException 类未找到
     */
    List<WorkflowActionAdvanceParamDTO> getConfigWithActionPath(String actionId, Integer workflowId, String actionPath)
            throws ClassNotFoundException;

    /**
     * 获取 Action 参数配置（仅从数据库获取）
     *
     * @param actionId Action 标识
     * @return 参数配置列表（DTO）
     */
    List<WorkflowActionAdvanceParamDTO> getConfig(String actionId);
}
