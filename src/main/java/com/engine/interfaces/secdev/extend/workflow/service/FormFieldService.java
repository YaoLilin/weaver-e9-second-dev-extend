package com.engine.interfaces.secdev.extend.workflow.service;

import com.engine.interfaces.secdev.extend.workflow.domain.dto.FormFieldsDTO;

/**
 * @author 姚礼林
 * @desc 流程字段业务接口
 * @date 2026/1/10
 **/
public interface FormFieldService {
    /**
     * 根据流程ID获取流程字段信息
     *
     * @param workflowId 流程ID
     * @return 流程字段列表
     */
    FormFieldsDTO getWorkflowFields(int workflowId);

    /**
     * 根据建模ID获取建模表单字段信息
     *
     * @param modeId 建模ID
     * @return 表单字段列表
     */
    FormFieldsDTO getModeFields(int modeId);

    /**
     * 根据表单id获取表单字段
     *
     * @param formId 表单id
     * @return 表单字段
     */
    FormFieldsDTO getFormFieldsByFormId(int formId);
}
