package com.engine.interfaces.secdev.extend.workflow.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.RecordSetFactory;
import com.customization.yll.common.util.SqlUtil;
import com.customization.yll.common.web.modal.vo.PageResult;
import com.engine.core.impl.Service;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowInfoItem;
import com.engine.interfaces.secdev.extend.workflow.service.WorkflowInfoService;
import weaver.conn.RecordSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 姚礼林
 * @desc 流程信息查询服务实现
 * @date 2026/1/27
 **/
public class WorkflowInfoServiceImpl extends Service implements WorkflowInfoService {
    private static final String BASE_FROM_SQL = " FROM workflow_base a "
        + "left join workflow_type t on a.workflowtype = t.id "
        + "join workflow_bill b on a.FORMID = b.id "
        + "WHERE a.activeversionid = a.id or a.activeversionid is null ";

    private final IntegrationLog log = new IntegrationLog(this.getClass());
    private final RecordSet recordSet;

    public WorkflowInfoServiceImpl() {
        this(RecordSetFactory.instance());
    }

    public WorkflowInfoServiceImpl(RecordSet recordSet) {
        this.recordSet = recordSet;
    }

    @Override
    public PageResult<WorkflowInfoItem> getWorkflowInfoList(Integer id, String workflowName, String tableName,
                                                            Integer pageNo, Integer pageSize) {
        int currentPage = pageNo == null || pageNo <= 0 ? 1 : pageNo;
        int currentSize = pageSize == null || pageSize <= 0 ? 10 : pageSize;
        int offset = (currentPage - 1) * currentSize;

        SqlCondition condition = buildCondition(id, workflowName, tableName);
        int total = queryTotal(condition);
        List<WorkflowInfoItem> list = queryList(condition, offset, currentSize);
        return buildPageResult(list, total, currentPage, currentSize);
    }

    private SqlCondition buildCondition(Integer id, String workflowName, String tableName) {
        StringBuilder whereSql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        if (id != null && id > 0) {
            whereSql.append(" AND a.id = ? ");
            params.add(id);
        }
        if (StrUtil.isNotBlank(workflowName)) {
            whereSql.append(" AND a.workflowname like ? ");
            params.add("%" + workflowName.trim() + "%");
        }
        if (StrUtil.isNotBlank(tableName)) {
            whereSql.append(" AND b.tablename like ? ");
            params.add("%" + tableName.trim() + "%");
        }
        return new SqlCondition(whereSql, params);
    }

    private int queryTotal(SqlCondition condition) {
        String countSql = "select count(1) as total " + BASE_FROM_SQL + condition.getWhereSql();
        log.info("查询流程信息总数，sql: {}", countSql);
        recordSet.executeQuery(countSql, condition.getParams().toArray());
        if (recordSet.next()) {
            return Convert.toInt(recordSet.getString("total"), 0);
        }
        return 0;
    }

    private List<WorkflowInfoItem> queryList(SqlCondition condition, int offset, int limit) {
        String baseSql = "select a.id, a.workflowname, t.TYPENAME, a.version, a.formid, b.tablename "
            + BASE_FROM_SQL + condition.getWhereSql();
        SqlUtil.SqlPageResult pageResult = SqlUtil.buildPageSql(
            recordSet.getDBType(),
            baseSql,
            "order by a.id desc",
            offset,
            limit
        );
        log.info("查询流程信息列表，sql: {}", pageResult.getSql());
        List<Object> queryParams = new ArrayList<>(condition.getParams().size() + pageResult.getParams().size());
        queryParams.addAll(condition.getParams());
        queryParams.addAll(pageResult.getParams());
        recordSet.executeQuery(pageResult.getSql(), queryParams.toArray());
        List<WorkflowInfoItem> list = new ArrayList<>();
        while (recordSet.next()) {
            WorkflowInfoItem item = new WorkflowInfoItem();
            item.setId(Convert.toInt(recordSet.getString("id"), 0));
            item.setWorkflowName(recordSet.getString("workflowname"));
            item.setTypeName(recordSet.getString("TYPENAME"));
            item.setVersion(Convert.toInt(recordSet.getString("version"), 0));
            item.setFormId(Convert.toInt(recordSet.getString("formid"), 0));
            item.setTableName(recordSet.getString("tablename"));
            list.add(item);
        }
        return list;
    }

    private PageResult<WorkflowInfoItem> buildPageResult(List<WorkflowInfoItem> list, int total,
                                                         int pageNo, int pageSize) {
        PageResult<WorkflowInfoItem> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageNo(pageNo);
        result.setPageSize(pageSize);
        return result;
    }

    private static class SqlCondition {
        private final StringBuilder whereSql;
        private final List<Object> params;

        private SqlCondition(StringBuilder whereSql, List<Object> params) {
            this.whereSql = whereSql;
            this.params = params;
        }

        private String getWhereSql() {
            return whereSql.toString();
        }

        private List<Object> getParams() {
            return params;
        }
    }
}
