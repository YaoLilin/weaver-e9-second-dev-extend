package com.engine.interfaces.secdev.extend.api.dao;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.exception.SqlExecuteException;
import com.customization.yll.common.util.ModeUtil;
import com.customization.yll.common.util.SqlUtil;
import com.engine.interfaces.secdev.extend.api.domain.entity.ParamAssignmentEntity;

import org.jetbrains.annotations.Nullable;

import weaver.conn.RecordSet;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 * @author 姚礼林
 * @desc 赋值配置 Dao
 * @date 2026/1/27
 **/
public class ParamAssignmentDao {
    private static final String TABLE_NAME = "uf_param_assignment";
    private final IntegrationLog log = new IntegrationLog(this.getClass());
    private final RecordSet recordSet;
    private final int modeId;

    public ParamAssignmentDao(RecordSet recordSet) {
        this.recordSet = recordSet;
        this.modeId = ModeUtil.getModeIdByTableName(TABLE_NAME, recordSet);
    }

    @Nullable
    public Integer insert(ParamAssignmentEntity entity) {
        if (modeId == -1) {
            log.error("获取建模ID失败，表名：" + TABLE_NAME);
            return null;
        }
        Map<String, Object> fieldData = new HashMap<>(10);
        fieldData.put("assignment_method", entity.getAssignmentMethod());
        fieldData.put("assignment_is_main_table", entity.getAssignmentIsMainTable());
        fieldData.put("assignment_detail_table_num", entity.getAssignmentDetailTableNum());
        fieldData.put("assignment_field_name", entity.getAssignmentFieldName());
        fieldData.put("assignment_value", entity.getAssignmentValue());

        java.util.Optional<Integer> idOptional = ModeUtil.insertToModeAndGetId(fieldData, TABLE_NAME, modeId, recordSet);
        return idOptional.orElse(null);
    }

    public void deleteByIds(List<Integer> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        String placeholders = SqlUtil.buildInClausePlaceholders(ids.size());
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id IN (" + placeholders + ")";
        Object[] params = ids.toArray();
        if (!recordSet.executeUpdate(sql, params)) {
            throw new SqlExecuteException(buildSqlMessage(sql, params), sql);
        }
    }

    public Map<Integer, ParamAssignmentEntity> findByIds(List<Integer> ids) {
        if (CollUtil.isEmpty(ids)) {
            return new HashMap<>(0);
        }
        String placeholders = SqlUtil.buildInClausePlaceholders(ids.size());
        String sql = "SELECT id, assignment_method, assignment_is_main_table, assignment_detail_table_num, "
            + "assignment_field_name, assignment_value FROM " + TABLE_NAME + " WHERE id IN (" + placeholders + ")";
        Map<Integer, ParamAssignmentEntity> result = new LinkedHashMap<>(ids.size());
        Object[] params = ids.toArray();
        if (!recordSet.executeQuery(sql, params)) {
            throw new SqlExecuteException(buildSqlMessage(sql, params), sql);
        }
        while (recordSet.next()) {
            ParamAssignmentEntity entity = new ParamAssignmentEntity();
            entity.setId(Convert.toInt(recordSet.getString("id"), 0));
            entity.setAssignmentMethod(Convert.toInt(recordSet.getString("assignment_method"), 0));
            entity.setAssignmentIsMainTable(Convert.toInt(recordSet.getString("assignment_is_main_table"), 0));
            entity.setAssignmentDetailTableNum(Convert.toInt(recordSet.getString("assignment_detail_table_num"), 0));
            entity.setAssignmentFieldName(recordSet.getString("assignment_field_name"));
            entity.setAssignmentValue(recordSet.getString("assignment_value"));
            result.put(entity.getId(), entity);
        }
        return result;
    }

    private String buildSqlMessage(String sql, Object... params) {
        return "执行sql失败，sql: " + sql + "，params: " + Arrays.toString(params);
    }
}
