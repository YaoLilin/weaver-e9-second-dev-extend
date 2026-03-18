package com.customization.secdev.extend.api.param;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.exception.SqlExecuteException;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowFieldValue;
import weaver.conn.RecordSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 建模表单字段取值：根据建模表名、主表主键及字段配置，获取主表或明细表字段值。
 * 主表单字段单值；明细表多行时按逗号拼接为字符串，与 WorkflowApiParamValueInjector 规则一致。
 *
 * @author 姚礼林
 * @date 2026/2/4
 */
public class ModeFieldValueFetcher {

    private static final String SQL_SELECT = "SELECT ";
    private static final String SQL_FROM = " FROM ";

    private final RecordSet recordSet;

    public ModeFieldValueFetcher(RecordSet recordSet) {
        this.recordSet = recordSet;
    }

    /**
     * 根据字段配置获取建模表单字段值（主表或明细表，多行明细用逗号拼接）
     *
     * @param tableName 建模主表表名，不能为空
     * @param mainId    建模数据主表主键 id
     * @param field     字段配置（isMainTable、detailTableNum、fieldName），不能为空
     * @return 字段值，取不到或为空则返回空字符串
     * @throws IllegalArgumentException 若 tableName、field 为空或 mainId 无效
     * @throws SqlExecuteException       若 SQL 执行失败
     */
    public String getFieldValue(String tableName, int mainId, WorkflowFieldValue field) {
        if (StrUtil.isBlank(tableName)) {
            throw new IllegalArgumentException("tableName 不能为空");
        }
        if (field == null || StrUtil.isBlank(field.getFieldName())) {
            throw new IllegalArgumentException("field 及 fieldName 不能为空");
        }
        if (mainId <= 0) {
            throw new IllegalArgumentException("mainId 必须大于 0");
        }

        Boolean isMainTable = field.getIsMainTable();
        if (isMainTable != null && isMainTable) {
            return getMainTableFieldValue(tableName, mainId, field.getFieldName());
        }
        Integer detailTableNum = field.getDetailTableNum();
        if (detailTableNum == null || detailTableNum < 1) {
            return "";
        }
        return getDetailTableFieldValue(tableName, mainId, detailTableNum, field.getFieldName());
    }

    /**
     * 获取建模主表单字段值
     *
     * @param tableName 主表表名
     * @param mainId    主表主键
     * @param fieldName 字段名
     * @return 字段值，无记录或空则返回空字符串
     */
    public String getMainTableFieldValue(String tableName, int mainId, String fieldName) {
        String sql = SQL_SELECT + fieldName + SQL_FROM + tableName + " WHERE id = ?";
        if (!recordSet.executeQuery(sql, mainId)) {
            throw new SqlExecuteException("查询建模主表字段失败，sql: " + sql + ", mainId: " + mainId, sql);
        }
        if (!recordSet.next()) {
            return "";
        }
        String value = recordSet.getString(fieldName);
        return value != null ? value : "";
    }

    /**
     * 获取建模明细表某字段值，多行时用逗号拼接（与 WorkflowApiParamValueInjector 明细表规则一致）。
     *
     * @param tableName      主表表名，明细表名为 tableName_dtN
     * @param mainId         主表主键，明细表通过 mainid 关联
     * @param detailTableNum 明细表序号，从 1 开始（对应 _dt1）
     * @param fieldName      字段名
     * @return 单行则返回该值，多行则逗号拼接；无记录则返回空字符串
     */
    public String getDetailTableFieldValue(String tableName, int mainId, int detailTableNum, String fieldName) {
        String detailTable = tableName + "_dt" + detailTableNum;
        String sql = SQL_SELECT + fieldName + SQL_FROM + detailTable + " WHERE mainid = ?";
        if (!recordSet.executeQuery(sql, mainId)) {
            throw new SqlExecuteException("查询建模明细表字段失败，sql: " + sql + ", mainId: " + mainId, sql);
        }
        List<String> values = new ArrayList<>();
        while (recordSet.next()) {
            String value = recordSet.getString(fieldName);
            if (StrUtil.isNotBlank(value)) {
                values.add(value);
            }
        }
        if (CollUtil.isEmpty(values)) {
            return "";
        }
        return String.join(",", values);
    }

    /**
     * 获取建模明细表多列多行数据（用于需要多字段时批量查询）
     *
     * @param tableName      主表表名
     * @param mainId         主表主键
     * @param detailTableNum 明细表序号
     * @param fieldNames     字段名列表
     * @return 每行一个 Map，key 为字段名
     */
    public List<Map<String, String>> getDetailFields(String tableName, int mainId, int detailTableNum,
                                                      List<String> fieldNames) {
        if (CollUtil.isEmpty(fieldNames)) {
            return Collections.emptyList();
        }
        String detailTable = tableName + "_dt" + detailTableNum;
        String fields = fieldNames.stream().collect(Collectors.joining(","));
        String sql = SQL_SELECT + fields + SQL_FROM + detailTable + " WHERE mainid = ?";
        if (!recordSet.executeQuery(sql, mainId)) {
            throw new SqlExecuteException("查询建模明细表失败，sql: " + sql + ", mainId: " + mainId, sql);
        }
        List<Map<String, String>> rows = new ArrayList<>();
        while (recordSet.next()) {
            Map<String, String> row = new HashMap<>(fieldNames.size());
            for (String fn : fieldNames) {
                String v = recordSet.getString(fn);
                row.put(fn, v != null ? v : "");
            }
            rows.add(row);
        }
        return rows;
    }
}
