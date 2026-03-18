package com.engine.interfaces.secdev.extend.workflow.service;

import com.customization.yll.common.web.modal.vo.PageResult;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowInfoItem;

/**
 * @author 姚礼林
 * @desc 流程信息服务
 * @date 2026/1/27
 **/
public interface WorkflowInfoService {
    PageResult<WorkflowInfoItem> getWorkflowInfoList(Integer id, String workflowName, String tableName,
                                                     Integer pageNo, Integer pageSize);
}
