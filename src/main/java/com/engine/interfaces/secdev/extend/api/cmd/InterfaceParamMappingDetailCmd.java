package com.engine.interfaces.secdev.extend.api.cmd;

import cn.hutool.core.convert.Convert;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.RecordSetFactory;
import com.engine.core.interceptor.AbstractCommand;
import com.engine.core.interceptor.CommandContext;
import com.engine.interfaces.secdev.extend.api.dao.InterfaceParamMappingDao;
import com.engine.interfaces.secdev.extend.api.constants.ResourceType;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceParamMappingDetail;
import com.engine.interfaces.secdev.extend.api.dao.ParamAssignmentDao;
import com.engine.interfaces.secdev.extend.api.domain.dto.Assignment;
import com.engine.interfaces.secdev.extend.api.service.FieldDisplayNameGenerator;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowFieldValue;
import com.engine.interfaces.secdev.extend.api.domain.entity.ParamAssignmentEntity;
import com.engine.interfaces.secdev.extend.api.domain.enums.AssignmentMethod;
import com.engine.interfaces.secdev.extend.api.domain.vo.AssignmentValue;
import weaver.conn.RecordSet;

import java.util.*;

/**
 * @author 姚礼林
 * @desc 接口参数映射配置详情查询
 * @date 2026/1/29
 **/
public class InterfaceParamMappingDetailCmd extends AbstractCommand<InterfaceParamMappingDetail> {
    private final IntegrationLog log = new IntegrationLog(this.getClass());
    private final ParamAssignmentDao assignmentDao;
    private final InterfaceParamMappingDao mappingDao;
    private final FieldDisplayNameGenerator fieldDisplayNameGenerator;
    private final Integer id;

    public InterfaceParamMappingDetailCmd(Integer id) {
        this(RecordSetFactory.instance(), new ParamAssignmentDao(RecordSetFactory.instance()),
                new FieldDisplayNameGenerator(), id);
    }

    public InterfaceParamMappingDetailCmd(RecordSet recordSet, ParamAssignmentDao assignmentDao,
                                          FieldDisplayNameGenerator fieldDisplayNameGenerator, Integer id) {
        this.assignmentDao = assignmentDao;
        this.mappingDao = new InterfaceParamMappingDao(recordSet);
        this.fieldDisplayNameGenerator = fieldDisplayNameGenerator;
        this.id = id;
    }

    @Override
    public InterfaceParamMappingDetail execute(CommandContext commandContext) {
        try {
            if (id == null || id <= 0) {
                return null;
            }
            InterfaceParamMappingDetail detail = mappingDao.queryDetailMain(id);
            if (detail == null || detail.getId() == null) {
                return detail;
            }
            List<InterfaceParamMappingDetail.ParamMappingItem> mappings = buildMappings(detail);
            detail.setMappings(mappings);
            return detail;
        } catch (Exception e) {
            log.error("执行发生异常", e);
            throw  e;
        }
    }

    private List<InterfaceParamMappingDetail.ParamMappingItem> buildMappings(InterfaceParamMappingDetail detail) {
        List<InterfaceParamMappingDao.ParamMappingRecord> records = mappingDao.queryMappingRecords(detail.getId());
        List<InterfaceParamMappingDetail.ParamMappingItem> list = new ArrayList<>();
        List<Integer> assignmentIds = new ArrayList<>();
        List<Integer> assignmentIdIndex = new ArrayList<>();
        for (InterfaceParamMappingDao.ParamMappingRecord mappingRecord : records) {
            InterfaceParamMappingDetail.ParamMappingItem item = new InterfaceParamMappingDetail.ParamMappingItem();
            item.setParamId(mappingRecord.getParamId());
            item.setParamName(mappingRecord.getParamName());
            item.setDetailNum(mappingRecord.getDetailNum());
            if (mappingRecord.getAssignmentId() != null && mappingRecord.getAssignmentId() > 0) {
                assignmentIds.add(mappingRecord.getAssignmentId());
                assignmentIdIndex.add(list.size());
            }
            list.add(item);
        }
        fillAssignments(list, assignmentIds, assignmentIdIndex, detail);
        return list;
    }

    private void fillAssignments(List<InterfaceParamMappingDetail.ParamMappingItem> list,
                                 List<Integer> assignmentIds,
                                 List<Integer> assignmentIdIndex,InterfaceParamMappingDetail detail) {
        if (assignmentIds.isEmpty()) {
            return;
        }
        Map<Integer, ParamAssignmentEntity> assignmentMap = assignmentDao.findByIds(assignmentIds);
        for (int i = 0; i < assignmentIdIndex.size(); i++) {
            int index = assignmentIdIndex.get(i);
            Integer assignmentId = assignmentIds.get(i);
            ParamAssignmentEntity assignmentEntity = assignmentMap.get(assignmentId);
            if (assignmentEntity != null) {
                list.get(index).setAssignment(buildAssignment(assignmentEntity, detail));
            }
        }
    }

    private Assignment buildAssignment(ParamAssignmentEntity entity,InterfaceParamMappingDetail detail ) {
        AssignmentMethod method = AssignmentMethod.fromValue(Convert.toInt(entity.getAssignmentMethod(), 0));
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
            String displayName = workflowField.getFieldName();
            if (detail.getResourceType() == ResourceType.WORKFLOW) {
                displayName = fieldDisplayNameGenerator.generateDisplayNameWithWorkflow(detail.getWorkflowId(),
                        workflowField);
            } else if (detail.getResourceType()  == ResourceType.MODE) {
                displayName = fieldDisplayNameGenerator.generateDisplayNameWithMode(detail.getModeId(), workflowField);
            }
            workflowField.setDisplayName(displayName);
            value.setWorkflowField(workflowField);
        } else {
            value.setValue(entity.getAssignmentValue());
        }
        assignment.setValue(value);
        return assignment;
    }


}
