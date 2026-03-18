package com.engine.interfaces.secdev.extend.api.cmd;

import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.RecordSetFactory;
import com.customization.yll.common.web.modal.vo.PageResult;
import com.engine.core.interceptor.AbstractCommand;
import com.engine.core.interceptor.CommandContext;
import com.engine.interfaces.secdev.extend.api.dao.InterfaceConfDao;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceListItem;
import weaver.conn.RecordSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yaolilin
 * @desc 接口列表查询命令
 * @date 2025/1/23
 **/
public class InterfaceListCmd extends AbstractCommand<PageResult<InterfaceListItem>> {
    private final InterfaceConfDao interfaceConfDao;
    private final Integer pageNo;
    private final Integer pageSize;
    private final String name;
    private final String url;
    private final String apiId;
    private final IntegrationLog log = new IntegrationLog(this.getClass());

    public InterfaceListCmd(Integer pageNo, Integer pageSize, String name, String url, String apiId) {
        this(RecordSetFactory.instance(), pageNo, pageSize, name, url, apiId);
    }

    public InterfaceListCmd(RecordSet recordSet, Integer pageNo, Integer pageSize,
                            String name, String url, String apiId) {
        this.interfaceConfDao = new InterfaceConfDao(recordSet);
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.name = name;
        this.url = url;
        this.apiId = apiId;
    }

    @Override
    public PageResult<InterfaceListItem> execute(CommandContext commandContext) {
        try {
            int currentPage = pageNo == null || pageNo <= 0 ? 1 : pageNo;
            int currentSize = pageSize == null || pageSize <= 0 ? 10 : pageSize;
            int offset = (currentPage - 1) * currentSize;

            SqlCondition condition = buildCondition(name, url, apiId);
            int total = interfaceConfDao.countByCondition(condition.getWhereSql(), condition.getParams());
            List<InterfaceConfDao.InterfaceListRecord> records = interfaceConfDao.listByCondition(
                condition.getWhereSql(), condition.getParams(), offset, currentSize);
            List<InterfaceListItem> list = buildList(records);
            return buildPageResult(list, total, currentPage, currentSize);
        } catch (Exception e) {
            log.error("执行发生异常", e);
            throw e;
        }
    }

    private SqlCondition buildCondition(String name, String url, String apiId) {
        StringBuilder whereSql = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (StrUtil.isNotBlank(name)) {
            whereSql.append(" AND name like ? ");
            params.add("%" + name.trim() + "%");
        }
        if (StrUtil.isNotBlank(url)) {
            whereSql.append(" AND url like ? ");
            params.add("%" + url.trim() + "%");
        }
        if (StrUtil.isNotBlank(apiId)) {
            whereSql.append(" AND api_id like ? ");
            params.add("%" + apiId.trim() + "%");
        }
        return new SqlCondition(whereSql, params);
    }

    private List<InterfaceListItem> buildList(List<InterfaceConfDao.InterfaceListRecord> records) {
        List<InterfaceListItem> result = new ArrayList<>();
        for (InterfaceConfDao.InterfaceListRecord listRecord : records) {
            InterfaceListItem item = new InterfaceListItem();
            item.setId(listRecord.getId());
            item.setApiId(listRecord.getApiId());
            item.setUrl(listRecord.getUrl());
            item.setMethod(getMethod(listRecord.getMethod()));
            item.setName(listRecord.getName());
            result.add(item);
        }
        return result;
    }

    private PageResult<InterfaceListItem> buildPageResult(List<InterfaceListItem> list, int total,
                                                          int pageNo, int pageSize) {
        PageResult<InterfaceListItem> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageNo(pageNo);
        result.setPageSize(pageSize);
        return result;
    }

    private String  getMethod(int method) {
        switch (method) {
            case 0:
                return "GET";
            case 1:
                return "POST";
            case 2:
                return "PUT";
            case 3:
                return "DELETE";
            default:
                throw new IllegalArgumentException("没有对应的方法类型，选择的方法：" + method);
        }
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
