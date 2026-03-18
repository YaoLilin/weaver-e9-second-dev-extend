package com.engine.interfaces.secdev.extend.workflow.dao;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.util.ModeUtil;
import com.engine.interfaces.secdev.extend.workflow.domain.entity.WorkflowActionParamConfigEntity;
import weaver.conn.RecordSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 姚礼林
 * @desc 流程Action参数配置Dao
 * @date 2026/1/10
 **/
public class WorkflowActionParamConfigDao {
    private static final String TABLE_NAME = "uf_action_param";
    private static final String ASSIGNMENT_ID_COLUMN = "assignment_id";
    private final RecordSet recordSet;
    private final int modeId;
    public WorkflowActionParamConfigDao(RecordSet recordSet) {
        this.recordSet = recordSet;
        // 根据表名获取建模ID
        this.modeId = ModeUtil.getModeIdByTableName(TABLE_NAME, recordSet);
    }

    /**
     * 删除指定 Action 的所有配置
     *
     * @param actionId Action标识
     * @return 是否成功
     */
    public boolean deleteByActionId(String actionId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE action_id = ?";
        return recordSet.executeUpdate(sql, actionId);
    }

    /**
     * 插入配置
     *
     * @param entity 实体对象
     * @return 插入后的ID，失败返回null
     */
    public Integer insert(WorkflowActionParamConfigEntity entity) {
        Map<String, Object> fieldData = new HashMap<>(20);
        fieldData.put("action_id", entity.getActionId());
        fieldData.put("parent_id", entity.getParentId());
        fieldData.put("param_name", entity.getParamName());
        fieldData.put("display_name", entity.getDisplayName());
        fieldData.put("detail_table", entity.getDetailTable());
        fieldData.put("sort_order", entity.getSortOrder());
        fieldData.put(ASSIGNMENT_ID_COLUMN, entity.getAssignmentId());

        java.util.Optional<Integer> idOptional = ModeUtil.insertToModeAndGetId(fieldData, TABLE_NAME, modeId, recordSet);
        return idOptional.orElse(null);
    }

    /**
     * 根据 actionId 查询所有配置
     *
     * @param actionId Action标识
     * @return 实体列表
     */
    public List<WorkflowActionParamConfigEntity> findByActionId(String actionId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE action_id = ? ORDER BY sort_order";
        List<WorkflowActionParamConfigEntity> result = new java.util.ArrayList<>();

        if (recordSet.executeQuery(sql, actionId)) {
            while (recordSet.next()) {
                WorkflowActionParamConfigEntity entity = mapRowToEntity();
                result.add(entity);
            }
        }
        return result;
    }

    public List<Integer> findAssignmentIdsByActionId(String actionId) {
        String sql = "SELECT " + ASSIGNMENT_ID_COLUMN + " FROM " + TABLE_NAME
            + " WHERE action_id = ? AND " + ASSIGNMENT_ID_COLUMN + " IS NOT NULL";
        List<Integer> result = new java.util.ArrayList<>();
        if (recordSet.executeQuery(sql, actionId)) {
            while (recordSet.next()) {
                String assignmentId = recordSet.getString(ASSIGNMENT_ID_COLUMN);
                if (StrUtil.isNotBlank(assignmentId)) {
                    result.add(Convert.toInt(assignmentId, 0));
                }
            }
        }
        return result;
    }

    /**
     * 将数据库行映射为实体对象
     */
    private WorkflowActionParamConfigEntity mapRowToEntity() {
        WorkflowActionParamConfigEntity entity = new WorkflowActionParamConfigEntity();
        entity.setId(Convert.toInt(recordSet.getString("id")));
        entity.setActionId(recordSet.getString("action_id"));
        entity.setParentId(Convert.toInt(recordSet.getString("parent_id")));
        entity.setParamName(recordSet.getString("param_name"));
        entity.setDisplayName(recordSet.getString("display_name"));
        entity.setDetailTable(Convert.toInt(recordSet.getString("detail_table")));
        entity.setSortOrder(Convert.toInt(recordSet.getString("sort_order")));
        entity.setAssignmentId(Convert.toInt(recordSet.getString(ASSIGNMENT_ID_COLUMN)));
        return entity;
    }

}
