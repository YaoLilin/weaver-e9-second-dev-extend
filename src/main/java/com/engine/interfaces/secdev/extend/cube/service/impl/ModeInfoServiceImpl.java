package com.engine.interfaces.secdev.extend.cube.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.RecordSetFactory;
import com.engine.core.impl.Service;
import com.engine.interfaces.secdev.extend.cube.domain.dto.ModeInfoItem;
import com.customization.yll.common.util.SqlUtil;
import com.customization.yll.common.web.modal.vo.PageResult;
import com.engine.interfaces.secdev.extend.cube.service.ModeInfoService;
import weaver.conn.RecordSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 姚礼林
 * @desc 建模信息查询服务实现
 * @date 2026/1/23
 **/
public class ModeInfoServiceImpl extends Service implements ModeInfoService {

    private final IntegrationLog log = new IntegrationLog(this.getClass());
    private final RecordSet recordSet;

    public ModeInfoServiceImpl() {
        this(RecordSetFactory.instance());
    }

    public ModeInfoServiceImpl(RecordSet recordSet) {
        this.recordSet = recordSet;
    }

    @Override
    public PageResult<ModeInfoItem> getModeInfoList(Integer id, String modeName, String tableName,
                                                    Integer pageNo, Integer pageSize) {
        int currentPage = pageNo == null || pageNo <= 0 ? 1 : pageNo;
        int currentSize = pageSize == null || pageSize <= 0 ? 10 : pageSize;
        int offset = (currentPage - 1) * currentSize;

        SqlCondition condition = buildCondition(id, modeName, tableName);
        int total = queryTotal(condition);
        List<ModeInfoItem> list = queryList(condition, offset, currentSize);
        return buildPageResult(list, total, currentPage, currentSize);
    }

    private SqlCondition buildCondition(Integer id, String modeName, String tableName) {
        StringBuilder whereSql = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        appendCondition(whereSql, params, id, modeName, tableName);
        return new SqlCondition(whereSql, params);
    }

    private void appendCondition(StringBuilder whereSql, List<Object> params,
                                 Integer id, String modeName, String tableName) {
        if (id != null && id > 0) {
            whereSql.append(" AND m.id = ? ");
            params.add(id);
        }
        if (StrUtil.isNotBlank(modeName)) {
            whereSql.append(" AND m.modename like ? ");
            params.add("%" + modeName.trim() + "%");
        }
        if (StrUtil.isNotBlank(tableName)) {
            whereSql.append(" AND w.tablename like ? ");
            params.add("%" + tableName.trim() + "%");
        }
    }

    private int queryTotal(SqlCondition condition) {
        String countSql = "select count(1) as total " + condition.getBaseSql() + condition.getWhereSql();
        log.info("查询建模信息总数，sql: {}", countSql);
        recordSet.executeQuery(countSql, condition.getParams().toArray());
        if (recordSet.next()) {
            return Convert.toInt(recordSet.getString("total"), 0);
        }
        return 0;
    }

    private List<ModeInfoItem> queryList(SqlCondition condition, int offset, int limit) {
        String baseSql = "select m.id,m.modename,w.tablename " + condition.getBaseSql() + condition.getWhereSql();
        SqlUtil.SqlPageResult pageResult = SqlUtil.buildPageSql(
            recordSet.getDBType(),
            baseSql,
            "order by m.id desc",
            offset,
            limit
        );
        List<Object> listParams = new ArrayList<>(condition.getParams().size() + pageResult.getParams().size());
        listParams.addAll(condition.getParams());
        listParams.addAll(pageResult.getParams());
        log.info("查询建模信息列表，sql: {}", pageResult.getSql());
        recordSet.executeQuery(pageResult.getSql(), listParams.toArray());

        List<ModeInfoItem> list = new ArrayList<>();
        while (recordSet.next()) {
            ModeInfoItem item = new ModeInfoItem();
            item.setId(Convert.toInt(recordSet.getString("id")));
            item.setModeName(recordSet.getString("modename"));
            item.setTableName(recordSet.getString("tablename"));
            list.add(item);
        }
        return list;
    }

    private PageResult<ModeInfoItem> buildPageResult(List<ModeInfoItem> list, int total,
                                                     int pageNo, int pageSize) {
        PageResult<ModeInfoItem> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageNo(pageNo);
        result.setPageSize(pageSize);
        return result;
    }

    private static class SqlCondition {
        private static final String BASE_SQL = " FROM modeinfo m JOIN workflow_bill w on m.formid = w.id ";
        private final StringBuilder whereSql;
        private final List<Object> params;

        private SqlCondition(StringBuilder whereSql, List<Object> params) {
            this.whereSql = whereSql;
            this.params = params;
        }

        private String getBaseSql() {
            return BASE_SQL;
        }

        private String getWhereSql() {
            return whereSql.toString();
        }

        private List<Object> getParams() {
            return params;
        }
    }
}
