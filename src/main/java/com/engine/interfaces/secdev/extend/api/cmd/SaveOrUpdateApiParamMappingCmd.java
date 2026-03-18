package com.engine.interfaces.secdev.extend.api.cmd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.RecordSetFactory;
import com.engine.core.interceptor.AbstractCommand;
import com.engine.core.interceptor.CommandContext;
import com.engine.interfaces.secdev.extend.api.dao.InterfaceParamMappingDao;
import com.engine.interfaces.secdev.extend.api.domain.param.InterfaceParamMappingParam;
import com.engine.interfaces.secdev.extend.api.dao.ParamAssignmentDao;
import com.engine.interfaces.secdev.extend.api.domain.dto.Assignment;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowFieldValue;
import com.engine.interfaces.secdev.extend.api.domain.entity.ParamAssignmentEntity;
import com.engine.interfaces.secdev.extend.api.domain.enums.AssignmentMethod;
import com.engine.interfaces.secdev.extend.api.domain.vo.AssignmentValue;
import weaver.conn.RecordSet;

import java.util.List;
/**
 * @author yaolilin
 * @desc 保存接口参数映射配置
 * @date 2025/2/14
 **/
public class SaveOrUpdateApiParamMappingCmd extends AbstractCommand<Boolean> {
    private final InterfaceParamMappingParam param;
    private final ParamAssignmentDao assignmentDao;
    private final InterfaceParamMappingDao mappingDao;
    private final IntegrationLog log = new IntegrationLog(this.getClass());

    public SaveOrUpdateApiParamMappingCmd(InterfaceParamMappingParam param) {
        this(RecordSetFactory.instance(), new ParamAssignmentDao(RecordSetFactory.instance()), param);
    }

    public SaveOrUpdateApiParamMappingCmd(RecordSet recordSet, ParamAssignmentDao assignmentDao,
                                          InterfaceParamMappingParam param) {
        this.assignmentDao = assignmentDao;
        this.mappingDao = new InterfaceParamMappingDao(recordSet);
        this.param = param;
    }

    @Override
    public Boolean execute(CommandContext commandContext) {
        try {
            if (param == null || StrUtil.isBlank(param.getConfId()) || StrUtil.isBlank(param.getName())) {
                return false;
            }
            Integer mainId = mappingDao.findIdByConfId(param.getConfId());
            if (mainId != null) {
                mappingDao.updateMain(mainId, param);
                cleanupDetails(mainId);
            } else {
                mainId = mappingDao.insertMain(param);
                if (mainId == null) {
                    return false;
                }
            }
            saveDetail(mainId, param.getHeaderParameters());
            saveDetail(mainId, param.getQueryParameters());
            saveDetail(mainId, param.getBodyParameters());
            return true;
        } catch (Exception e) {
            log.error("执行发生异常", e);
            return false;
        }
    }

    private void cleanupDetails(Integer mainId) {
        List<Integer> assignmentIds = mappingDao.findDetailAssignmentIds(mainId);
        mappingDao.deleteDetailsByMainId(mainId);
        assignmentDao.deleteByIds(assignmentIds);
    }

    private void saveDetail(Integer mainId, List<InterfaceParamMappingParam.ParamMapper> params) {
        if (CollUtil.isEmpty(params)) {
            return;
        }
        for (InterfaceParamMappingParam.ParamMapper item : params) {
            Integer assignmentId = null;
            if (item.getAssignment() != null && item.getAssignment().getMethod() != null) {
                ParamAssignmentEntity assignmentEntity = buildAssignmentEntity(item.getAssignment());
                assignmentId = assignmentDao.insert(assignmentEntity);
            }
            mappingDao.insertDetail(mainId, item.getParamId(), item.getName(), assignmentId, item.getDetailNum());
            if (CollUtil.isNotEmpty(item.getChildren())) {
                saveDetail(mainId, item.getChildren());
            }
        }
    }

    private ParamAssignmentEntity buildAssignmentEntity(Assignment assignment) {
        ParamAssignmentEntity entity = new ParamAssignmentEntity();
        AssignmentMethod method = assignment.getMethod();
        if (method != null) {
            entity.setAssignmentMethod(method.getValue());
        }
        AssignmentValue value = assignment.getValue();
        if (value != null) {
            if (method == AssignmentMethod.FORM_FIELD && value.getWorkflowField() != null) {
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
}
