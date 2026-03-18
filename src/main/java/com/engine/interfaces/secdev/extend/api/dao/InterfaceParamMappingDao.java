package com.engine.interfaces.secdev.extend.api.dao;

import cn.hutool.core.convert.Convert;
import com.customization.yll.common.exception.SqlExecuteException;
import com.customization.yll.common.util.ModeUtil;
import com.customization.yll.common.util.SqlUtil;
import com.engine.interfaces.secdev.extend.api.constants.ResourceType;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceParamMappingDetail;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceParamMappingListItem;
import com.engine.interfaces.secdev.extend.api.domain.param.InterfaceParamMappingParam;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import weaver.conn.RecordSet;

import java.util.*;

/**
 * @author 姚礼林
 * @desc 接口参数映射配置 Dao
 * @date 2026/1/29
 **/
public class InterfaceParamMappingDao {
    private static final String TABLE_NAME = "uf_api_param_mapping";
    private static final String DETAIL_TABLE_NAME = "uf_api_param_mapping_dt1";
    private static final String FIELD_CONF_ID = "conf_id";
    private static final String FIELD_RESOURCE_TYPE = "resource_type";
    private static final String FIELD_WORKFLOW_ID = "workflow_id";
    private static final String FIELD_MODE_ID = "mode_id";
    private static final String FIELD_INTERFACE_ID = "interface_id";
    private static final String FIELD_BODY_DETAIL_NUM = "body_detail_num";
    private final RecordSet recordSet;

    public InterfaceParamMappingDao(RecordSet recordSet) {
        this.recordSet = recordSet;
    }

    @Nullable
    public Integer findIdByConfId(String confId) {
        String sql = "select id from " + TABLE_NAME + " where " + FIELD_CONF_ID + " = ?";
        if (!recordSet.executeQuery(sql, confId)) {
            throw new SqlExecuteException(buildSqlMessage(sql, confId), sql);
        }
        if (recordSet.next()) {
            return recordSet.getInt("id");
        }
        return null;
    }

    public Integer insertMain(InterfaceParamMappingParam param) {
        int modeId = ModeUtil.getModeIdByTableName(TABLE_NAME, recordSet);
        if (modeId <= 0) {
            return null;
        }
        Map<String, Object> fieldData = new HashMap<>(21);
        fieldData.put(FIELD_CONF_ID, param.getConfId());
        fieldData.put("name", param.getName());
        fieldData.put(FIELD_RESOURCE_TYPE, getResourceTypeValue(param.getDataSource()));
        fieldData.put(FIELD_WORKFLOW_ID, param.getWorkflow());
        fieldData.put(FIELD_MODE_ID, param.getModeId());
        fieldData.put(FIELD_INTERFACE_ID, param.getApiId());
        fieldData.put(FIELD_BODY_DETAIL_NUM, param.getBodyDetailNum());
        return ModeUtil.insertToModeAndGetId(fieldData, TABLE_NAME, modeId, recordSet).orElse(null);
    }

    public void updateMain(Integer mainId, InterfaceParamMappingParam param) {
        String sql = "update " + TABLE_NAME + " set " + FIELD_CONF_ID + "=?, name=?, " + FIELD_RESOURCE_TYPE + "=?, "
            + FIELD_WORKFLOW_ID + "=?, " + FIELD_MODE_ID + "=?, " + FIELD_INTERFACE_ID + "=?, "
            + FIELD_BODY_DETAIL_NUM + "=? where id=?";
        boolean success = recordSet.executeUpdate(sql, param.getConfId(), param.getName(), getResourceTypeValue(param.getDataSource()),
            param.getWorkflow(), param.getModeId(), param.getApiId(), param.getBodyDetailNum(), mainId);
        if (!success) {
            throw new SqlExecuteException(buildSqlMessage(sql, param.getConfId(), param.getName(),
                getResourceTypeValue(param.getDataSource()), param.getWorkflow(), param.getModeId(), param.getApiId(),
                param.getBodyDetailNum(), mainId), sql);
        }
    }

    public List<Integer> findDetailAssignmentIds(Integer mainId) {
        List<Integer> assignmentIds = new ArrayList<>();
        String sql = "select assignment_id from " + DETAIL_TABLE_NAME + " where mainid = ? and assignment_id is not null";
        if (!recordSet.executeQuery(sql, mainId)) {
            throw new SqlExecuteException(buildSqlMessage(sql, mainId), sql);
        }
        while (recordSet.next()) {
            int assignmentId = recordSet.getInt("assignment_id");
            if (assignmentId > 0) {
                assignmentIds.add(assignmentId);
            }
        }
        return assignmentIds;
    }

    public void deleteDetailsByMainId(Integer mainId) {
        String sql = "delete from " + DETAIL_TABLE_NAME + " where mainid = ?";
        if (!recordSet.executeUpdate(sql, mainId)) {
            throw new SqlExecuteException(buildSqlMessage(sql, mainId), sql);
        }
    }

    public void insertDetail(Integer mainId, String paramId, String paramName, Integer assignmentId, Integer detailNum) {
        String sql = "insert into " + DETAIL_TABLE_NAME + " (mainid,param_id,param_name,assignment_id,detail_num) values (?,?,?,?,?)";
        if (!recordSet.executeUpdate(sql, mainId, paramId, paramName, assignmentId, detailNum)) {
            throw new SqlExecuteException(buildSqlMessage(sql, mainId, paramId, paramName, assignmentId, detailNum), sql);
        }
    }

    public int countMappings() {
        String countSql = "select count(1) as total from " + TABLE_NAME;
        if (!recordSet.executeQuery(countSql)) {
            throw new SqlExecuteException(buildSqlMessage(countSql), countSql);
        }
        if (recordSet.next()) {
            return Convert.toInt(recordSet.getString("total"), 0);
        }
        return 0;
    }

    public List<InterfaceParamMappingListItem> queryMappingList(int offset, int limit) {
        String baseSql = "select m.id,m.conf_id,m.name,m.resource_type,m.workflow_id,m.mode_id,m.interface_id,"
            + " w.workflowname, mi.modename, api.name as api_name "
            + "from " + TABLE_NAME + " m "
            + "left join workflow_base w on m.workflow_id = w.id "
            + "left join modeinfo mi on m.mode_id = mi.id "
            + "left join uf_api_conf api on m.interface_id = api.id ";
        SqlUtil.SqlPageResult pageResult = SqlUtil.buildPageSql(
            recordSet.getDBType(),
            baseSql,
            "order by m.id desc",
            offset,
            limit
        );
        Object[] params = pageResult.getParams().toArray();
        if (!recordSet.executeQuery(pageResult.getSql(), params)) {
            throw new SqlExecuteException(buildSqlMessage(pageResult.getSql(), params), pageResult.getSql());
        }

        List<InterfaceParamMappingListItem> result = new ArrayList<>();
        while (recordSet.next()) {
            InterfaceParamMappingListItem item = new InterfaceParamMappingListItem();
            item.setId(Convert.toInt(recordSet.getString("id"), 0));
            item.setConfId(recordSet.getString(FIELD_CONF_ID));
            item.setName(recordSet.getString("name"));
            item.setResourceType(Convert.toInt(recordSet.getString(FIELD_RESOURCE_TYPE), 0));
            item.setWorkflowId(Convert.toInt(recordSet.getString(FIELD_WORKFLOW_ID), 0));
            item.setModeId(Convert.toInt(recordSet.getString(FIELD_MODE_ID), 0));
            item.setInterfaceId(Convert.toInt(recordSet.getString(FIELD_INTERFACE_ID), 0));
            item.setWorkflowName(recordSet.getString("workflowname"));
            item.setModeName(recordSet.getString("modename"));
            item.setInterfaceName(recordSet.getString("api_name"));
            result.add(item);
        }
        return result;
    }

    public InterfaceParamMappingDetail queryDetailMain(Integer id) {
        String sql = "select m.id,m.conf_id,m.name,m.resource_type,m.workflow_id,m.mode_id,m.interface_id,m.body_detail_num,"
            + " w.workflowname, coalesce(w.formid, b.id) as formid, mi.modename, b.tablename, api.name as api_name "
            + "from uf_api_param_mapping m "
            + "left join workflow_base w on m.workflow_id = w.id "
            + "left join modeinfo mi on m.mode_id = mi.id "
            + "left join workflow_bill b on mi.formid = b.id "
            + "left join uf_api_conf api on m.interface_id = api.id "
            + "where m.id = ?";
        if (!recordSet.executeQuery(sql, id)) {
            throw new SqlExecuteException(buildSqlMessage(sql, id), sql);
        }
        if (!recordSet.next()) {
            return null;
        }
        InterfaceParamMappingDetail detail = new InterfaceParamMappingDetail();
        detail.setId(Convert.toInt(recordSet.getString("id"), 0));
        detail.setConfId(recordSet.getString(FIELD_CONF_ID));
        detail.setName(recordSet.getString("name"));
        Integer resourceType = Convert.toInt(recordSet.getString(FIELD_RESOURCE_TYPE));
        detail.setResourceType(ResourceType.getByValue(resourceType));
        detail.setWorkflowId(Convert.toInt(recordSet.getString(FIELD_WORKFLOW_ID), 0));
        detail.setWorkflowName(recordSet.getString("workflowname"));
        detail.setWorkflowFormId(Convert.toInt(recordSet.getString("formid"), 0));
        detail.setModeId(Convert.toInt(recordSet.getString(FIELD_MODE_ID), 0));
        detail.setModeName(recordSet.getString("modename"));
        detail.setTableName(recordSet.getString("tablename"));
        detail.setInterfaceId(Convert.toInt(recordSet.getString(FIELD_INTERFACE_ID), 0));
        detail.setInterfaceName(recordSet.getString("api_name"));
        detail.setBodyDetailNum(Convert.toInt(recordSet.getString(FIELD_BODY_DETAIL_NUM), null));
        return detail;
    }

    public List<ParamMappingRecord> queryMappingRecords(Integer mainId) {
        String sql = "select param_id,param_name,assignment_id,detail_num from " + DETAIL_TABLE_NAME + " where mainid = ?";
        List<ParamMappingRecord> list = new ArrayList<>();
        if (!recordSet.executeQuery(sql, mainId)) {
            throw new SqlExecuteException(buildSqlMessage(sql, mainId), sql);
        }
        while (recordSet.next()) {
            ParamMappingRecord item = new ParamMappingRecord();
            item.setParamId(recordSet.getString("param_id"));
            item.setParamName(recordSet.getString("param_name"));
            item.setDetailNum(Convert.toInt(recordSet.getString("detail_num"), 0));
            item.setAssignmentId(Convert.toInt(recordSet.getString("assignment_id"), 0));
            list.add(item);
        }
        return list;
    }

    private Integer getResourceTypeValue(ResourceType type) {
        return type == null ? null : type.getValue();
    }

    private String buildSqlMessage(String sql, Object... params) {
        return "执行sql失败，sql: " + sql + "，params: " + Arrays.toString(params);
    }

    @Data
    public static class ParamMappingRecord {
        private String paramId;
        private String paramName;
        private Integer assignmentId;
        private Integer detailNum;
    }
}
