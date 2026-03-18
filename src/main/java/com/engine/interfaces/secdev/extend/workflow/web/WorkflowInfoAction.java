package com.engine.interfaces.secdev.extend.workflow.web;

import com.customization.yll.common.web.WebExceptionHandler;
import com.customization.yll.common.web.modal.vo.ApiResult;
import com.customization.yll.common.web.modal.vo.PageResult;
import com.engine.common.util.ServiceUtil;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowInfoItem;
import com.engine.interfaces.secdev.extend.workflow.service.impl.WorkflowInfoServiceImpl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author 姚礼林
 * @desc 流程信息接口
 * @date 2026/1/27
 **/
public class WorkflowInfoAction {

    /**
     * 获取流程信息列表（支持条件与分页）
     *
     * @param id           流程ID
     * @param workflowName 流程名称
     * @param tableName    流程表名
     * @param pageNo       页码
     * @param pageSize     每页数量
     * @return 流程信息分页结果
     */
    @Path("")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkflowInfoList(@QueryParam("id") Integer id,
                                        @QueryParam("workflowName") String workflowName,
                                        @QueryParam("tableName") String tableName,
                                        @QueryParam("pageNo") Integer pageNo,
                                        @QueryParam("pageSize") Integer pageSize) {
        try {
            PageResult<WorkflowInfoItem> result = ServiceUtil.getService(WorkflowInfoServiceImpl.class)
                .getWorkflowInfoList(id, workflowName, tableName, pageNo, pageSize);
            return Response.ok(ApiResult.success(result)).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }
}
