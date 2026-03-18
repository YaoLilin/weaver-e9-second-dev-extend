package com.engine.interfaces.secdev.extend.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.RecordSetFactory;
import com.customization.yll.common.exception.SqlExecuteException;
import com.engine.core.impl.Service;
import com.engine.interfaces.secdev.extend.api.dao.ParamAssignmentDao;
import com.engine.interfaces.secdev.extend.workflow.dao.WorkflowActionParamConfigDao;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowActionAdvanceParamDTO;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowFieldValue;
import com.engine.interfaces.secdev.extend.workflow.domain.entity.WorkflowActionParamConfigEntity;
import com.engine.interfaces.secdev.extend.api.domain.entity.ParamAssignmentEntity;
import com.engine.interfaces.secdev.extend.api.domain.enums.AssignmentMethod;
import com.engine.interfaces.secdev.extend.api.domain.enums.ParamType;
import com.engine.interfaces.secdev.extend.api.domain.dto.Assignment;
import com.engine.interfaces.secdev.extend.api.domain.vo.AssignmentValue;
import com.engine.interfaces.secdev.extend.workflow.service.WorkflowActionParamConfigService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * @author 姚礼林
 * @desc 流程Action参数配置业务实现类
 * @date 2026/1/10
 **/
public class WorkflowActionParamConfigServiceImpl extends Service implements WorkflowActionParamConfigService {
    private final IntegrationLog log = new IntegrationLog(this.getClass());
    private final WorkflowActionParamConfigDao paramConfigDao;
    private final ParamAssignmentDao assignmentDao;

    public WorkflowActionParamConfigServiceImpl() {
        this(new WorkflowActionParamConfigDao(RecordSetFactory.instance()),
            new ParamAssignmentDao(RecordSetFactory.instance()));
    }

    public WorkflowActionParamConfigServiceImpl(WorkflowActionParamConfigDao paramConfigDao,
                                                ParamAssignmentDao assignmentDao) {
        this.paramConfigDao = paramConfigDao;
        this.assignmentDao = assignmentDao;
    }

    @Override
    public void saveConfig(String actionId, List<WorkflowActionAdvanceParamDTO> params) {
        if (StrUtil.isBlank(actionId)) {
            throw new IllegalArgumentException("actionId 不能为空");
        }

        // 1. 先删除该 Action 的旧配置
        log.info("删除旧配置，actionId: {}", actionId);
        List<Integer> oldAssignmentIds = paramConfigDao.findAssignmentIdsByActionId(actionId);
        if (!paramConfigDao.deleteByActionId(actionId)) {
            log.warn("删除旧配置可能失败，actionId: {}", actionId);
        }
        assignmentDao.deleteByIds(oldAssignmentIds);

        // 2. 如果没有新配置，直接返回
        if (CollUtil.isEmpty(params)) {
            log.info("无新配置需要保存，actionId: {}", actionId);
            return;
        }

        // 3. 逐条插入，建立父子关系
        int[] sortOrderCounter = {0};
        insertConfigsRecursively(params, actionId, null, sortOrderCounter, paramConfigDao, assignmentDao);
        log.info("保存配置成功，actionId: {}", actionId);

    }

    @Override
    public List<WorkflowActionAdvanceParamDTO> getConfigWithActionPath(String actionId, Integer workflowId,
                                                                     String actionPath) throws ClassNotFoundException {
        // 1. 获取 DB 配置
        List<WorkflowActionAdvanceParamDTO> dbConfigs = getConfig(actionId);

        // 2. 解析 Action 类参数
        List<WorkflowActionAdvanceParamDTO> classParams = WorkflowActionParamParser.parse(actionPath);
        if (classParams.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 合并配置：将 DB 的赋值信息（assignment, detailTable）合并到解析出的参数上
        // 构建 DB 配置的映射 Map (递归处理)
        Map<String, WorkflowActionAdvanceParamDTO> dbConfigMap = new java.util.HashMap<>();
        flattenConfig(dbConfigs, dbConfigMap, "");

        // 递归合并
        mergeConfigRecursive(classParams, dbConfigMap, "", actionId);

        return classParams;
    }

    private void flattenConfig(List<WorkflowActionAdvanceParamDTO> configs, Map<String, WorkflowActionAdvanceParamDTO> map, String prefix) {
        if (configs == null) {
            return;
        }
        for (WorkflowActionAdvanceParamDTO config : configs) {
            String key = prefix + config.getName();
            map.put(key, config);
            flattenConfig(config.getChildren(), map, key + ".");
        }
    }

    private void mergeConfigRecursive(List<WorkflowActionAdvanceParamDTO> classParams,
                                      Map<String, WorkflowActionAdvanceParamDTO> dbConfigMap,
                                      String prefix, String actionId) {
        if (classParams == null) {
            return;
        }
        for (WorkflowActionAdvanceParamDTO param : classParams) {
            String key = prefix + param.getName();
            WorkflowActionAdvanceParamDTO dbConfig = dbConfigMap.get(key);

            if (dbConfig != null) {
                // 合并 DB 中的赋值信息
                param.setAssignment(dbConfig.getAssignment());
                param.setDetailTable(dbConfig.getDetailTable());
                if (StrUtil.isNotBlank(dbConfig.getActionId())) {
                    param.setActionId(dbConfig.getActionId());
                }
                // ID 也需要带上，虽然保存时可能没用，但前端 key 可能依赖
                param.setId(dbConfig.getId());
            } else if (StrUtil.isBlank(param.getActionId())) {
                param.setActionId(actionId);
            }

            mergeConfigRecursive(param.getChildren(), dbConfigMap, key + ".", actionId);
        }
    }

    @Override
    public List<WorkflowActionAdvanceParamDTO> getConfig(String actionId) {
        if (StrUtil.isBlank(actionId)) {
            throw new IllegalArgumentException("actionId 不能为空");
        }

        // 查询所有配置（Entity）
        List<WorkflowActionParamConfigEntity> entities = paramConfigDao.findByActionId(actionId);
        Map<Integer, ParamAssignmentEntity> assignmentMap = buildAssignmentMap(entities);

        // 将 Entity 转换为 DTO
        List<WorkflowActionAdvanceParamDTO> dtos = new ArrayList<>();
        for (WorkflowActionParamConfigEntity entity : entities) {
            dtos.add(convertEntityToDTO(entity, assignmentMap));
        }

        // 构建树形结构
        List<WorkflowActionAdvanceParamDTO> treeResult = buildDTOTree(dtos);

        log.info("查询配置成功，actionId: {}, 返回记录数: {}", actionId, treeResult.size());
        return treeResult;
    }

    /**
     * 将扁平化的 DTO 列表构建成树形结构
     */
    private List<WorkflowActionAdvanceParamDTO> buildDTOTree(List<WorkflowActionAdvanceParamDTO> dtos) {
        if (CollUtil.isEmpty(dtos)) {
            return new ArrayList<>();
        }

        // 创建 ID 到 DTO 的映射
        Map<Integer, WorkflowActionAdvanceParamDTO> dtoMap = new java.util.HashMap<>(dtos.size());
        for (WorkflowActionAdvanceParamDTO dto : dtos) {
            dtoMap.put(dto.getId(), dto);
        }

        // 构建树形结构
        List<WorkflowActionAdvanceParamDTO> rootList = new ArrayList<>();
        for (WorkflowActionAdvanceParamDTO dto : dtos) {
            Integer parentId = dto.getParentId();
            if (parentId == null || parentId == 0) {
                // 根节点
                rootList.add(dto);
            } else {
                // 子节点，添加到父节点的 children 中
                WorkflowActionAdvanceParamDTO parent = dtoMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                } else {
                    // 父节点不存在，作为根节点处理
                    log.warn("父节点不存在，将作为根节点处理，id: {}, parentId: {}", dto.getId(), parentId);
                    rootList.add(dto);
                }
            }
        }

        return rootList;
    }

    /**
     * 将 Entity 转换为 DTO
     */
    private WorkflowActionAdvanceParamDTO convertEntityToDTO(WorkflowActionParamConfigEntity entity,
                                                            Map<Integer, ParamAssignmentEntity> assignmentMap) {
        WorkflowActionAdvanceParamDTO dto = new WorkflowActionAdvanceParamDTO();
        dto.setId(entity.getId());
        dto.setActionId(entity.getActionId());
        dto.setParentId(entity.getParentId());
        dto.setName(entity.getParamName());
        dto.setShowName(entity.getDisplayName());
        dto.setRequired(entity.getRequired() != null && entity.getRequired() == 1);
        dto.setType(ParamType.fromValue(entity.getParamType() != null ? entity.getParamType() : 0));
        dto.setDetailTable(entity.getDetailTable());
        dto.setSortOrder(entity.getSortOrder());

        // 设置 assignment 对象
        ParamAssignmentEntity assignment = assignmentMap.get(entity.getAssignmentId());
        dto.setAssignment(buildAssignmentFromEntity(assignment));

        return dto;
    }

    private Assignment buildAssignmentFromEntity(ParamAssignmentEntity entity) {
        if (entity == null || entity.getAssignmentMethod() == null) {
            return null;
        }
        AssignmentMethod method = fromValue(entity.getAssignmentMethod());
        if (method == null) {
            return null;
        }
        Assignment assignment = new Assignment();
        assignment.setMethod(method);
        AssignmentValue value = new AssignmentValue();
        if (method == AssignmentMethod.FORM_FIELD) {
            WorkflowFieldValue workflowField = new WorkflowFieldValue();
            workflowField.setIsMainTable(entity.getAssignmentIsMainTable() != null
                && entity.getAssignmentIsMainTable() == 1);
            workflowField.setDetailTableNum(entity.getAssignmentDetailTableNum());
            workflowField.setFieldName(entity.getAssignmentFieldName());
            value.setWorkflowField(workflowField);
        } else {
            value.setValue(entity.getAssignmentValue());
        }
        assignment.setValue(value);
        return assignment;
    }

    private AssignmentMethod fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (AssignmentMethod method : AssignmentMethod.values()) {
            if (method.getValue() == value) {
                return method;
            }
        }
        return null;
    }

    private Map<Integer, ParamAssignmentEntity> buildAssignmentMap(List<WorkflowActionParamConfigEntity> entities) {
        List<Integer> assignmentIds = new ArrayList<>();
        for (WorkflowActionParamConfigEntity entity : entities) {
            Integer assignmentId = entity.getAssignmentId();
            if (assignmentId != null && assignmentId > 0) {
                assignmentIds.add(assignmentId);
            }
        }
        if (assignmentIds.isEmpty()) {
            return new java.util.HashMap<>(0);
        }
        return assignmentDao.findByIds(assignmentIds);
    }

    /**
     * 递归插入配置，建立父子关系
     *
     * @param configs          树形配置列表（SaveDTO）
     * @param actionId         Action标识
     * @param parentId         父级ID（数据库真实ID）
     * @param sortOrderCounter 排序序号计数器（数组形式，用于传递引用）
     * @param dao              Dao对象
     */
    private void insertConfigsRecursively(List<WorkflowActionAdvanceParamDTO> configs, String actionId,
                                          Integer parentId, int[] sortOrderCounter,
                                          WorkflowActionParamConfigDao dao, ParamAssignmentDao assignmentDao) {
        if (CollUtil.isEmpty(configs)) {
            return;
        }

        for (WorkflowActionAdvanceParamDTO dto : configs) {
            Integer assignmentId = null;
            Assignment assignment = dto.getAssignment();
            if (assignment != null && assignment.getMethod() != null) {
                ParamAssignmentEntity assignmentEntity = buildAssignmentEntity(assignment);
                assignmentId = assignmentDao.insert(assignmentEntity);
                if (assignmentId == null) {
                    throw new SqlExecuteException("插入赋值配置失败，参数名称: " + dto.getName());
                }
            }
            // 将 SaveDTO 转换为 Entity
            WorkflowActionParamConfigEntity entity = convertSaveDTOToEntity(dto, actionId, parentId,
                    sortOrderCounter[0]++, assignmentId);

            // 插入当前节点
            Integer currentId = dao.insert(entity);
            if (currentId == null) {
                throw new SqlExecuteException("插入配置失败，参数名称: " + dto.getName());
            }

            // 递归插入子节点
            if (CollUtil.isNotEmpty(dto.getChildren())) {
                insertConfigsRecursively(dto.getChildren(), actionId, currentId, sortOrderCounter, dao, assignmentDao);
            }
        }
    }

    private ParamAssignmentEntity buildAssignmentEntity(Assignment assignment) {
        ParamAssignmentEntity entity = new ParamAssignmentEntity();
        entity.setAssignmentMethod(assignment.getMethod().getValue());
        AssignmentValue value = assignment.getValue();
        if (value != null) {
            if (assignment.getMethod() == AssignmentMethod.FORM_FIELD && value.getWorkflowField() != null) {
                WorkflowFieldValue workflowField = value.getWorkflowField();
                entity.setAssignmentIsMainTable(
                    workflowField.getIsMainTable() != null && workflowField.getIsMainTable() ? 1 : 0);
                entity.setAssignmentDetailTableNum(workflowField.getDetailTableNum());
                entity.setAssignmentFieldName(workflowField.getFieldName());
            } else if (value.getValue() != null) {
                entity.setAssignmentValue(value.getValue());
            }
        }
        return entity;
    }

    /**
     * 将参数 DTO 转换为 Entity
     */
    private WorkflowActionParamConfigEntity convertSaveDTOToEntity(WorkflowActionAdvanceParamDTO dto,
                                                                   String actionId, Integer parentId, int sortOrder,
                                                                   Integer assignmentId) {
        WorkflowActionParamConfigEntity entity = new WorkflowActionParamConfigEntity();
        entity.setActionId(actionId);
        entity.setParentId(parentId);
        entity.setParamName(dto.getName());
        entity.setDisplayName(dto.getShowName());
        entity.setRequired(dto.getRequired() != null && dto.getRequired() ? 1 : 0);
        int paramTypeValue = 0;
        if (dto.getType() != null) {
            paramTypeValue = dto.getType().getValue();
        }
        entity.setParamType(paramTypeValue);
        entity.setDetailTable(dto.getDetailTable());
        entity.setSortOrder(sortOrder);
        entity.setAssignmentId(assignmentId);
        return entity;
    }


}
