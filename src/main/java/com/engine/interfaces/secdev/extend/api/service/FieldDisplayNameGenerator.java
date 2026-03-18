package com.engine.interfaces.secdev.extend.api.service;

import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.RecordSetFactory;
import com.customization.yll.common.util.FieldUtil;
import com.customization.yll.common.util.FormUtil;
import com.customization.yll.common.util.ModeUtil;
import com.customization.yll.common.util.WorkflowUtil;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowFieldValue;
import weaver.conn.RecordSet;

import java.util.Objects;
import java.util.Optional;

/**
 * @author 姚礼林
 * @desc 生成接口参数映射配置中的赋值字段显示名
 * @date 2026/1/30
 **/
public class FieldDisplayNameGenerator {
    private final IntegrationLog log = new IntegrationLog(this.getClass());

    /**
     * 生成赋值字段显示名，适用于流程字段
     *
     * @param workflowId 流程id
     * @param fieldValue 字段赋值信息
     * @throws IllegalArgumentException 如果 workflowId 参数不正确导致无法获取到表单名称，则抛出此异常
     * @return 赋值字段显示名
     */
    public String generateDisplayNameWithWorkflow(Integer workflowId, WorkflowFieldValue fieldValue) {
        Objects.requireNonNull(workflowId, "workflowId 不能为 null");
        if (fieldValue == null || StrUtil.isBlank(fieldValue.getFieldName())) {
            return "";
        }
        RecordSet recordSet = RecordSetFactory.instance();
        String detailTable = "";
        if (!fieldValue.getIsMainTable() && fieldValue.getDetailTableNum() != null) {
            String workflowTableName = WorkflowUtil.getWorkflowTableName(workflowId, recordSet);
            if (StrUtil.isBlank(workflowTableName)) {
                throw new IllegalArgumentException("workflowId 不正确，不能根据 workflowId 查找表单名称");
            }
            detailTable = workflowTableName + "_dt" + fieldValue.getDetailTableNum();
        }
        String fieldLabelName = FormUtil.getFieldName(fieldValue.getFieldName(), workflowId, detailTable, recordSet);
        if (StrUtil.isBlank(fieldLabelName)) {
            log.error("获取流程字段显示名称失败，字段名：{}，流程id：{}，明细表名：{}", fieldValue.getFieldName(),
                    workflowId, detailTable);
            return fieldValue.getFieldName();
        }
        return fieldLabelName + "(" + fieldValue.getFieldName() + ")";
    }

    /**
     * 生成赋值字段显示名，适用于建模字段
     *
     * @param modeId 建模id
     * @param fieldValue 字段赋值信息
     * @throws IllegalArgumentException 如果 modeId 参数不正确，无法取到表单id或表单名称，则抛出此异常
     * @return 赋值字段显示名
     */
    public String generateDisplayNameWithMode(Integer modeId, WorkflowFieldValue fieldValue) {
        Objects.requireNonNull(modeId, "modeId 不能为 null");
        if (fieldValue == null || StrUtil.isBlank(fieldValue.getFieldName())) {
            return "";
        }
        RecordSet recordSet = RecordSetFactory.instance();
        Optional<Integer> formIdOp = ModeUtil.getFormId(modeId, recordSet);
        if (!formIdOp.isPresent()) {
            throw new IllegalArgumentException("modeId 不正确，不能根据 modeId 获取表单id");
        }
        int formId = formIdOp.get();
        String detailTable = "";
        if (!fieldValue.getIsMainTable() && fieldValue.getDetailTableNum() != null) {
            String tableName = FormUtil.getFormTableName(formId, recordSet);
            if (StrUtil.isBlank(tableName)) {
                throw new IllegalArgumentException("formId 不正确，不能根据 formId 获取表单名称");
            }
            detailTable = tableName + "_dt" + fieldValue.getDetailTableNum();
        }
        String fieldLabelName = FieldUtil.getFormFieldName(fieldValue.getFieldName(), formId, detailTable, recordSet);
        if (StrUtil.isBlank(fieldLabelName)) {
            log.error("获取表单字段显示名称失败，字段名：{}，表单id：{}，明细表名：{}", fieldValue.getFieldName(),
                    formId, detailTable);
            return fieldValue.getFieldName();
        }
        return fieldLabelName + "(" + fieldValue.getFieldName() + ")";
    }
}
