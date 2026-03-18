package com.engine.interfaces.secdev.extend.workflow.service;

import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowActionParamDto;

import java.util.List;

/**
 * @author 姚礼林
 * @desc 流程 Acton 参数功能扩展业务接口
 * @date 2025/10/17
 **/
public interface WorkflowActionParamExtendService {

    /**
     * 获取流程 Action 参数
     *
     * @param actionPath Action 路径
     * @return Action 参数
     * @throws ClassNotFoundException 当指定的类路径无法加载时抛出
     */
    List<WorkflowActionParamDto> getParams(String actionPath) throws ClassNotFoundException;
}
