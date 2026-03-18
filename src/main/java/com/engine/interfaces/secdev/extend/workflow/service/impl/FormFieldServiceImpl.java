package com.engine.interfaces.secdev.extend.workflow.service.impl;

import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.RecordSetFactory;
import com.customization.yll.common.exception.SqlExecuteException;
import com.engine.core.impl.Service;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.DetailField;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.FormFieldsDTO;
import com.engine.interfaces.secdev.extend.workflow.domain.vo.FormField;
import com.engine.interfaces.secdev.extend.workflow.service.FormFieldService;
import org.jetbrains.annotations.Nullable;
import weaver.conn.RecordSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 姚礼林
 * @desc 流程字段业务实现类
 * @date 2026/1/10
 **/
public class FormFieldServiceImpl extends Service implements FormFieldService {
    private final IntegrationLog log = new IntegrationLog(this.getClass());
    /**
     * 用于从表名中提取明细表序号的正则表达式，匹配 _dt 后面的数字，如 formtable_main_16_dt1 中的 1
     */
    private static final Pattern DETAIL_TABLE_PATTERN = Pattern.compile("_dt(\\d+)");

    /**
     * 根据流程ID获取流程字段信息
     *
     * @param workflowId 流程ID
     * @return 流程字段信息
     * @throws IllegalArgumentException 如果 workflowId 小于1则抛出此异常
     * @throws SqlExecuteException      如果 sql 执行失败则抛出此异常
     */
    @Nullable
    @Override
    public FormFieldsDTO getWorkflowFields(int workflowId) {
        if (workflowId < 1) {
            throw new IllegalArgumentException("workflowId 不能小于1");
        }
        RecordSet recordSet = RecordSetFactory.instance();
        String sql = " select formid from workflow_base where id=?";
        if (!recordSet.executeQuery(sql, workflowId)) {
            throw new SqlExecuteException("查询 formid 失败，workflowId=" + workflowId, sql);
        }
        if (!recordSet.next()) {
            log.warn("无此流程id信息，流程id：" + workflowId);
            return null;
        }
        int formId = recordSet.getInt("formid");
        return getFormFieldsByFormId(formId);
    }

    /**
     * 根据建模ID获取建模表单字段信息
     *
     * @param modeId 建模ID
     * @return 建模表单字段信息
     * @throws IllegalArgumentException 如果 modeId 小于1则抛出此异常
     * @throws SqlExecuteException      如果 sql 执行失败则抛出此异常
     */
    @Nullable
    @Override
    public FormFieldsDTO getModeFields(int modeId) {
        if (modeId < 1) {
            throw new IllegalArgumentException("modeId 不能小于1");
        }
        RecordSet recordSet = RecordSetFactory.instance();
        String sql = "select formid from modeinfo where id=?";
        if (!recordSet.executeQuery(sql, modeId)) {
            throw new SqlExecuteException("查询 formid 失败，modeId=" + modeId, sql);
        }
        if (!recordSet.next()) {
            log.warn("无此建模id信息，建模id：" + modeId);
            return null;
        }
        int formId = recordSet.getInt("formid");
        return getFormFieldsByFormId(formId);
    }

    /**
     * 根据表单id获取表单字段
     *
     * @param formId 表单id
     * @return 表单字段
     * @throws IllegalArgumentException 如果formId为0则抛出此异常
     * @throws SqlExecuteException      如果 sql 执行失败则抛出此异常
     */
    @Override
    public FormFieldsDTO getFormFieldsByFormId(int formId) {
        if (formId == 0) {
            throw new IllegalArgumentException("formId 不能为 0");
        }
        RecordSet recordSet = RecordSetFactory.instance();

        String sql = "SELECT\n" +
                "\ta.id,\n" +
                "\ta.fieldname,\n" +
                "CASE\n" +
                "\t\tWHEN a.fieldhtmltype = 1 THEN\n" +
                "\t\t'单行文本框' \n" +
                "\t\tWHEN a.fieldhtmltype = 2 THEN\n" +
                "\t\t'多行文本框' \n" +
                "\t\tWHEN a.fieldhtmltype = 3 THEN\n" +
                "\t\t'浏览按钮' \n" +
                "\t\tWHEN a.fieldhtmltype = 4 THEN\n" +
                "\t\t'check框' \n" +
                "\t\tWHEN a.fieldhtmltype = 5 THEN\n" +
                "\t\t'选择框' ELSE '未知类型' \n" +
                "\tEND AS type1,\n" +
                "\tc.labelname,\n" +
                "\ta.detailtable \n" +
                "FROM\n" +
                "\tworkflow_billfield a\n" +
                "\tINNER JOIN htmllabelinfo c ON a.fieldlabel = c.indexid \n" +
                "WHERE\n" +
                "\ta.billid = ?\n" +
                "\tAND c.LANGUAGEid = '7'";

        if (!recordSet.executeQuery(sql, formId)) {
            throw new SqlExecuteException("查询表单字段失败，formId:" + formId + "，sql:" + sql);
        }

        // 分类字段：主表字段和明细表字段
        List<FormField> mainFields = new ArrayList<>();
        Map<Integer, List<FormField>> detailFieldsMap = new HashMap<>(10);

        while (recordSet.next()) {
            FormField field = new FormField();
            field.setFieldName(recordSet.getString("fieldname"));
            field.setLabelName(recordSet.getString("labelname"));
            field.setFieldType(recordSet.getString("type1"));

            // detailtable 字段存储的是明细表表名（如 "formtable_main_16_dt1"），需要从中提取序号
            String detailTable = recordSet.getString("detailtable");
            if (StrUtil.isBlank(detailTable)) {
                // 主表字段
                mainFields.add(field);
            } else {
                // 明细表字段，提取明细表序号
                Integer detailTableNum = extractDetailTableNum(detailTable.trim());
                if (detailTableNum != null && detailTableNum > 0) {
                    detailFieldsMap.computeIfAbsent(detailTableNum, k -> new ArrayList<>()).add(field);
                } else {
                    // 无法提取明细表序号，作为主表字段处理
                    log.warn("无法从表名中提取明细表序号，作为主表字段处理，formId: {}, detailTable: {}", formId, detailTable);
                    mainFields.add(field);
                }
            }
        }

        // 构建 WorkflowFieldsDTO
        FormFieldsDTO dto = new FormFieldsDTO();
        dto.setMainFields(mainFields);

        // 构建明细表字段列表（按 detailNum 排序）
        List<DetailField> details = new ArrayList<>();
        detailFieldsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    DetailField detailField = new DetailField();
                    detailField.setDetailNum(entry.getKey());
                    detailField.setFields(entry.getValue());
                    details.add(detailField);
                });
        dto.setDetails(details);

        log.info("查询流程字段成功，formId: {}, 主表字段数: {}, 明细表数: {}",
                formId, mainFields.size(), details.size());
        return dto;
    }

    /**
     * 从明细表表名中提取序号
     * 例如：formtable_main_16_dt1 -> 1
     *
     * @param detailTableName 明细表表名
     * @return 明细表序号，如果无法提取则返回 null
     */
    private Integer extractDetailTableNum(String detailTableName) {
        if (StrUtil.isBlank(detailTableName)) {
            return null;
        }
        Matcher matcher = DETAIL_TABLE_PATTERN.matcher(detailTableName);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("提取的明细表序号格式错误，detailTableName: {}, extracted: {}", detailTableName, matcher.group(1));
                return null;
            }
        }
        return null;
    }
}
