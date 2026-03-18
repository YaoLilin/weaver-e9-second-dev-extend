package com.engine.interfaces.secdev.extend.workflow.web;

import com.customization.yll.common.web.WebExceptionHandler;
import com.customization.yll.common.web.exception.WebParamException;
import com.customization.yll.common.web.modal.vo.ApiResult;
import com.engine.common.util.ServiceUtil;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.FormFieldsDTO;
import com.engine.interfaces.secdev.extend.workflow.domain.vo.WorkflowFieldsResult;
import com.engine.interfaces.secdev.extend.workflow.service.impl.FormFieldServiceImpl;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author 姚礼林
 * @desc 流程字段接口
 * @date 2026/1/10
 **/
public class WorkflowFieldAction {

    /**
     * 获取流程字段信息
     *
     * @param workflowId 流程ID
     * @return 流程字段结果（WorkflowFieldsResult）
     */
    @Path("")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkflowFields(@QueryParam("workflowId") int workflowId) {
        try {
            if (workflowId <= 0) {
                throw new WebParamException.QueryParamException("workflowId 参数必须大于0");
            }

            FormFieldsDTO dto = ServiceUtil.getService(FormFieldServiceImpl.class)
                    .getWorkflowFields(workflowId);
            if (dto == null) {
                throw new WebParamException.QueryParamException("找不到该流程的表单字段信息，workflowId:" + workflowId);
            }

            // 将 DTO 转换为 Result
            WorkflowFieldsResult result = new WorkflowFieldsResult();
            result.setMainFields(dto.getMainFields());
            result.setDetails(dto.getDetails());

            return Response.ok(ApiResult.success(result)).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }
}
