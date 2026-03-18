package com.engine.interfaces.secdev.extend.workflow.web;

import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.util.ParamUtil;
import com.customization.yll.common.web.WebExceptionHandler;
import com.customization.yll.common.web.exception.WebParamException;
import com.customization.yll.common.web.modal.vo.ApiResult;
import com.engine.common.util.ServiceUtil;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowActionAdvanceParamDTO;
import com.engine.interfaces.secdev.extend.workflow.domain.dto.WorkflowActionParamDto;
import com.engine.interfaces.secdev.extend.api.domain.enums.SystemParamEnum;
import com.engine.interfaces.secdev.extend.workflow.domain.param.WorkflowActionParamConfigSaveParam;
import com.engine.interfaces.secdev.extend.api.domain.vo.SystemParamVO;
import com.engine.interfaces.secdev.extend.workflow.domain.vo.WorkflowActionParamConfigVO;
import com.engine.interfaces.secdev.extend.workflow.service.impl.WorkflowActionParamConfigServiceImpl;
import com.engine.interfaces.secdev.extend.workflow.service.impl.WorkflowActionParamExtendServiceImpl;
import com.engine.interfaces.secdev.extend.workflow.util.WorkflowActionParamConfigConverter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 姚礼林
 * @desc 流程 Acton 参数功能扩展接口，可在Action配置页面提供更丰富的配置
 * @date 2025/10/15
 **/
public class WorkflowActionParamExtendAction {

    /**
     * 获取流程 Action 参数
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getParams(@QueryParam("actionPath") String actionPath) {
        try {
            if (StrUtil.isBlank(actionPath)) {
                throw new WebParamException.QueryParamException("[actionPath]参数无值");
            }

            List<WorkflowActionParamDto> params = ServiceUtil.getService(WorkflowActionParamExtendServiceImpl.class)
                    .getParams(actionPath);
            return Response.ok(params).build();
        } catch (ClassNotFoundException e) {
            return Response.status(404).entity(ApiResult.failed("类不存在")).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }

    /**
     * 保存流程 Action 高级参数配置
     */
    @POST
    @Path("/config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveConfig(@Context HttpServletRequest request) {
        try {
            WorkflowActionParamConfigSaveParam param =
                    ParamUtil.parseJsonToObject(request.getInputStream(), WorkflowActionParamConfigSaveParam.class);
            if (param == null) {
                throw new WebParamException.QueryParamException("请求参数不能为空");
            }
            if (StrUtil.isBlank(param.getActionId())) {
                throw new WebParamException.QueryParamException("actionId 参数不能为空");
            }

            ServiceUtil.getService(WorkflowActionParamConfigServiceImpl.class)
                    .saveConfig(param.getActionId(), param.getConfigs());

            return Response.ok(ApiResult.success("保存成功")).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }

    }

    /**
     * 获取流程 Action 高级参数配置
     */
    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfig(@QueryParam("actionId") String actionId,
                              @QueryParam("workflowId") Integer workflowId,
                              @QueryParam("actionPath") String actionPath) {
        try {
            if (StrUtil.isBlank(actionId)) {
                throw new WebParamException.QueryParamException("actionId 参数不能为空");
            }

            // 2. Action Path 为必传参数，如果为空则返回失败
            if (StrUtil.isBlank(actionPath)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResult.failed("actionPath 参数不能为空"))
                        .build();
            }

            // 获取配置（DTO）
            List<WorkflowActionAdvanceParamDTO> dtos = ServiceUtil.getService(WorkflowActionParamConfigServiceImpl.class)
                    .getConfigWithActionPath(actionId, workflowId, actionPath);

            // 将 DTO 转换为 VO
            List<WorkflowActionParamConfigVO> result = WorkflowActionParamConfigConverter.convertDTOsToVOs(dtos, workflowId);

            return Response.ok(ApiResult.success(result)).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }


    /**
     * 获取系统参数列表
     */
    @GET
    @Path("/system-params")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSystemParams() {
        try {
            SystemParamEnum[] enums = SystemParamEnum.values();
            List<SystemParamVO> result = new ArrayList<>(enums.length);
            for (SystemParamEnum item : enums) {
                result.add(new SystemParamVO(item.getCode(), item.getName()));
            }
            return Response.ok(ApiResult.success(result)).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }
}
