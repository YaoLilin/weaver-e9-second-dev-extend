package com.customization.secdev.extend.workflow;

import com.customization.yll.common.util.WorkflowUtil;
import com.customization.yll.common.workflow.constants.SystemParam;
import com.customization.yll.common.workflow.interfaces.WorkflowFieldValueFetchInterface;
import com.engine.interfaces.secdev.extend.api.domain.enums.SystemParamEnum;
import weaver.conn.RecordSet;

/**
 * @author 姚礼林
 * @desc 系统参数提供者
 * @date 2026/1/16
 **/
public class WorkflowSystemParamProvider {
    private final WorkflowFieldValueFetchInterface workflowFieldValueManager;
    private final RecordSet recordSet;

    public WorkflowSystemParamProvider(WorkflowFieldValueFetchInterface workflowFieldValueManager, RecordSet recordSet) {
        this.workflowFieldValueManager = workflowFieldValueManager;
        this.recordSet = recordSet;
    }

    public String getSystemParam(int requestId, SystemParamEnum paramType) {
        if (paramType == null) {
            throw new IllegalArgumentException("系统参数类型不能为空");
        }
        if (requestId < 1) {
            throw new IllegalArgumentException("请求id不正确，必需大于0");
        }
        switch (paramType) {
            case CREATOR:
                return workflowFieldValueManager.getSystemFieldValue(SystemParam.CREATOR);
            case CREATE_TIME:
                return workflowFieldValueManager.getSystemFieldValue(SystemParam.CREATE_DATE_TIME);
            case REQUEST_ID:
                return String.valueOf(requestId);
            case WORKFLOW_ID:
                return String.valueOf(WorkflowUtil.getWorkflowId(requestId, recordSet));
            default:
                throw new IllegalArgumentException("未定义系统参数获取方式，参数类型：" + paramType);
        }
    }
}
