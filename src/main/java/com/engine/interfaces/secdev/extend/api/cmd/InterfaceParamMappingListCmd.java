package com.engine.interfaces.secdev.extend.api.cmd;

import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.RecordSetFactory;
import com.customization.yll.common.web.modal.vo.PageResult;
import com.engine.core.interceptor.AbstractCommand;
import com.engine.core.interceptor.CommandContext;
import com.engine.interfaces.secdev.extend.api.dao.InterfaceParamMappingDao;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceParamMappingListItem;
import weaver.conn.RecordSet;

import java.util.List;

/**
 * @author 姚礼林
 * @desc 接口参数映射配置列表查询
 * @date 2026/1/29
 **/
public class InterfaceParamMappingListCmd extends AbstractCommand<PageResult<InterfaceParamMappingListItem>> {
    private final InterfaceParamMappingDao mappingDao;
    private final Integer pageNo;
    private final Integer pageSize;
    private final IntegrationLog log = new IntegrationLog(this.getClass());

    public InterfaceParamMappingListCmd(Integer pageNo, Integer pageSize) {
        this(RecordSetFactory.instance(), pageNo, pageSize);
    }

    public InterfaceParamMappingListCmd(RecordSet recordSet, Integer pageNo, Integer pageSize) {
        this.mappingDao = new InterfaceParamMappingDao(recordSet);
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    @Override
    public PageResult<InterfaceParamMappingListItem> execute(CommandContext commandContext) {
        try {
            int currentPage = pageNo == null || pageNo <= 0 ? 1 : pageNo;
            int currentSize = pageSize == null || pageSize <= 0 ? 10 : pageSize;
            int offset = (currentPage - 1) * currentSize;

            int total = mappingDao.countMappings();
            List<InterfaceParamMappingListItem> list = mappingDao.queryMappingList(offset, currentSize);
            list.forEach(item -> item.setResourceTypeName(buildResourceTypeName(item)));
            return buildPageResult(list, total, currentPage, currentSize);
        } catch (Exception e) {
            log.error("执行发生异常", e);
            throw e;
        }
    }

    private String buildResourceTypeName(InterfaceParamMappingListItem item) {
        if (item.getResourceType() != null && item.getResourceType() == 1) {
            return item.getModeName();
        }
        return item.getWorkflowName();
    }

    private PageResult<InterfaceParamMappingListItem> buildPageResult(List<InterfaceParamMappingListItem> list, int total,
                                                                      int pageNo, int pageSize) {
        PageResult<InterfaceParamMappingListItem> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageNo(pageNo);
        result.setPageSize(pageSize);
        return result;
    }

}
