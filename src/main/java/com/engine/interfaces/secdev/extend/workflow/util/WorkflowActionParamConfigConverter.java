package com.engine.interfaces.secdev.extend.workflow.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.util.FieldUtil;
import com.customization.yll.common.util.WorkflowUtil;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowActionAdvanceParamDTO;
import com.engine.interfaces.secdev.extend.api.domain.enums.AssignmentMethod;
import com.engine.interfaces.secdev.extend.api.domain.dto.Assignment;
import com.engine.interfaces.secdev.extend.api.domain.vo.AssignmentValue;
import com.engine.interfaces.secdev.extend.api.domain.vo.AssignmentVo;
import com.engine.interfaces.secdev.extend.workflow.domain.vo.WorkflowActionParamConfigVO;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowFieldValue;
import weaver.conn.RecordSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 姚礼林
 * @desc 流程Action参数配置转换工具类
 * @date 2026/1/10
 **/
public class WorkflowActionParamConfigConverter {
    private static final IntegrationLog log = new IntegrationLog(WorkflowActionParamConfigConverter.class);

    private WorkflowActionParamConfigConverter() {
        // 工具类，禁止实例化
    }

    /**
     * 将 DTO 列表转换为 VO 列表
     *
     * @param dtos DTO列表
     * @param workflowId 流程ID（可选，用于动态获取字段中文名）
     * @return VO列表
     */
    public static List<WorkflowActionParamConfigVO> convertDTOsToVOs(List<WorkflowActionAdvanceParamDTO> dtos,
                                                                     Integer workflowId) {
        List<WorkflowActionParamConfigVO> result = new ArrayList<>();
        // 创建 RecordSet 用于查询字段中文名
        RecordSet recordSet = new RecordSet();
        for (WorkflowActionAdvanceParamDTO dto : dtos) {
            result.add(convertDTOToVO(dto, workflowId, recordSet));
        }
        return result;
    }

    /**
     * 将 DTO 转换为 VO
     *
     * @param dto DTO对象
     * @param workflowId 流程ID（可选，用于动态获取字段中文名）
     * @param recordSet RecordSet对象（用于查询数据库）
     * @return VO对象
     */
    public static WorkflowActionParamConfigVO convertDTOToVO(WorkflowActionAdvanceParamDTO dto,
                                                              Integer workflowId, RecordSet recordSet) {
        // 手动创建 VO 对象并设置字段
        WorkflowActionParamConfigVO vo = new WorkflowActionParamConfigVO();
        vo.setId(dto.getId());
        vo.setActionId(dto.getActionId());
        vo.setParentId(dto.getParentId());
        vo.setName(dto.getName());
        vo.setShowName(dto.getShowName());
        vo.setRequired(dto.getRequired());
        vo.setDefaultValue(dto.getDefaultValue());
        vo.setDesc(buildDesc(dto));
        // type 字段直接使用整数值，前端根据整数值匹配选项
        vo.setType(dto.getType() != null ? dto.getType().getValue() : 0);
        vo.setDetailTable(dto.getDetailTable());
        vo.setSortOrder(dto.getSortOrder());

        // 重建 assignment 对象（动态获取字段中文名）
        vo.setAssignment(buildAssignmentFromDTO(dto, workflowId, recordSet));

        // 递归转换 children
        if (CollUtil.isNotEmpty(dto.getChildren())) {
            List<WorkflowActionParamConfigVO> childrenVOs = new ArrayList<>();
            for (WorkflowActionAdvanceParamDTO childDto : dto.getChildren()) {
                childrenVOs.add(convertDTOToVO(childDto, workflowId, recordSet));
            }
            vo.setChildren(childrenVOs);
        }

        return vo;
    }

    private static String buildDesc(WorkflowActionAdvanceParamDTO dto) {
        String desc = dto.getDesc();
        String defaultValue = dto.getDefaultValue();
        if (StrUtil.isNotBlank(defaultValue)) {
            String defaultText = "默认值：" + defaultValue ;
            if (StrUtil.isNotBlank(desc)) {
                return desc + " - " + defaultText;
            }
            return defaultText;
        }
        return desc;
    }

    /**
     * 从 DTO 重建 assignment 对象（动态获取字段中文名）
     *
     * @param dto DTO对象
     * @param workflowId 流程ID（可选，用于动态获取字段中文名）
     * @param recordSet RecordSet对象（用于查询数据库）
     * @return Assignment对象
     */
    public static AssignmentVo buildAssignmentFromDTO(WorkflowActionAdvanceParamDTO dto,
                                                      Integer workflowId, RecordSet recordSet) {
        Assignment assignment = dto.getAssignment();
        if (assignment == null || assignment.getMethod() == null) {
            return null;
        }

        // 如果是表单字段，需要动态获取字段中文名
        if (assignment.getMethod() == AssignmentMethod.FORM_FIELD) {
            AssignmentValue value = assignment.getValue();
            if (value != null && value.getWorkflowField() != null) {
                WorkflowFieldValue workflowField = value.getWorkflowField();

                // 动态获取字段中文名
                String displayName = getDisplayName(workflowId, recordSet, workflowField);
                // 如果无法获取中文名，则只显示字段数据库名
                if (StrUtil.isBlank(displayName)) {
                    displayName = workflowField.getFieldName();
                }
                workflowField.setDisplayName(displayName);
            }
        }

        AssignmentVo vo = new AssignmentVo();
        if (assignment.getMethod() != null) {
            vo.setMethod(assignment.getMethod().getValue() );
        }
        vo.setValue(assignment.getValue());
        return vo;
    }

    /**
     * 构建树形结构
     *
     * @param allConfigs 所有配置列表
     * @return 树形结构的配置列表
     */
    public static List<WorkflowActionParamConfigVO> buildTree(List<WorkflowActionParamConfigVO> allConfigs) {
        if (CollUtil.isEmpty(allConfigs)) {
            return new ArrayList<>();
        }

        // 创建ID到配置的映射
        Map<Integer, WorkflowActionParamConfigVO> configMap = new HashMap<>();
        for (WorkflowActionParamConfigVO config : allConfigs) {
            configMap.put(config.getId(), config);
        }

        // 构建树形结构
        List<WorkflowActionParamConfigVO> rootList = new ArrayList<>();
        for (WorkflowActionParamConfigVO config : allConfigs) {
            Integer parentId = config.getParentId();
            if (parentId == null || parentId == 0) {
                // 根节点
                rootList.add(config);
            } else {
                // 子节点，添加到父节点的children中
                WorkflowActionParamConfigVO parent = configMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(config);
                } else {
                    // 父节点不存在，作为根节点处理
                    log.warn("父节点不存在，将作为根节点处理，id: {}, parentId: {}", config.getId(), parentId);
                    rootList.add(config);
                }
            }
        }

        return rootList;
    }

    private static String getDisplayName(Integer workflowId, RecordSet recordSet, WorkflowFieldValue workflowField) {
        String displayName = "";
        if (workflowId != null && workflowId > 0 && recordSet != null
                && StrUtil.isNotBlank(workflowField.getFieldName())) {
            try {
                String detailTableName = null;
                // 判断是否为主表字段
                boolean isMainTable = workflowField.getIsMainTable() != null && workflowField.getIsMainTable();
                if (!isMainTable) {
                    // 明细表字段，需要构建明细表名
                    if (workflowField.getDetailTableNum() != null && workflowField.getDetailTableNum() > 0) {
                        String workflowTableName = WorkflowUtil.getWorkflowTableName(workflowId, recordSet);
                        detailTableName = workflowTableName + "_dt" + workflowField.getDetailTableNum();
                    }
                }
                String labelName = FieldUtil.getWorkflowFieldName(
                        workflowField.getFieldName(), workflowId, detailTableName, recordSet);
                if (StrUtil.isNotBlank(labelName)) {
                    // 组装显示名称：中文名(字段数据库名)
                    displayName = labelName + "(" + workflowField.getFieldName() + ")";
                }
            } catch (Exception e) {
                log.warn("获取字段中文名失败，fieldName: {}, workflowId: {}",
                        workflowField.getFieldName(), workflowId, e);
            }
        }
        return displayName;
    }
}
