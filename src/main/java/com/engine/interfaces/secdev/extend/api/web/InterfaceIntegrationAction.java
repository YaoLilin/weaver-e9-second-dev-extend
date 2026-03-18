package com.engine.interfaces.secdev.extend.api.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.customization.yll.common.util.ParamUtil;
import com.customization.yll.common.web.WebExceptionHandler;
import com.customization.yll.common.web.modal.vo.ApiResult;
import com.engine.common.util.ServiceUtil;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceConfDto;
import com.engine.interfaces.secdev.extend.api.domain.dto.InterfaceParamMappingDetail;
import com.engine.interfaces.secdev.extend.api.domain.param.InterfaceConfParam;
import com.engine.interfaces.secdev.extend.api.domain.param.InterfaceParamMappingParam;
import com.engine.interfaces.secdev.extend.api.service.InterfaceIntegrationService;
import com.engine.interfaces.secdev.extend.api.service.impl.InterfaceIntegrationServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * @author yaolilin
 * @desc 二次开发扩展模块接口集成，实现配置接口，配置接口参数映射等功能
 * @date 2025/1/17
 **/

public class InterfaceIntegrationAction {

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createInterfaceConf(@Context HttpServletRequest request) {
        try {
            Map<String, Object> params = ParamUtil.requestJson2Map(request);
            JSONObject json = new JSONObject(params);
            InterfaceConfParam interfaceConfParam = JSON.toJavaObject(json, InterfaceConfParam.class);
            if (getService().createInterfaceConf(interfaceConfParam)) {
                return Response.ok().build();
            }
            ApiResult<Object> result = ApiResult.failed("插入数据失败", "");
            return Response.serverError().entity(result).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }

    @Path("/")
    @GET
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInterfaceList(@QueryParam("pageNo") Integer pageNo,
                                     @QueryParam("pageSize") Integer pageSize,
                                     @QueryParam("name") String name,
                                     @QueryParam("url") String url,
                                     @QueryParam("apiId") String apiId) {
        try {
            return Response.ok().entity(getService().getInterfaceList(pageNo, pageSize, name, url, apiId)).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInterfaceConf(@PathParam("id") Integer id) {
        try {
            InterfaceConfDto interfaceConf = getService().getInterfaceConf(id);
            return Response.ok().entity(ParamUtil.parseObjectToJson(interfaceConf)).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }

    @Path("/mapping")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveApiParamMapping(@Context HttpServletRequest request) {
        try {
            InterfaceParamMappingParam mapperParam = ParamUtil.parseJsonToObject(request,
                    InterfaceParamMappingParam.class);
            if (getService().saveApiParamMapping(mapperParam)) {
                return Response.ok(ApiResult.success("保存成功")).build();
            }
            ApiResult<Object> result = ApiResult.failed("插入数据失败", "");
            return Response.serverError().entity(result).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }

    @Path("/mapping")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiParamMappingList(@QueryParam("pageNo") Integer pageNo,
                                           @QueryParam("pageSize") Integer pageSize) {
        try {
            return Response.ok().entity(getService().getApiParamMappingList(pageNo, pageSize)).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }

    @Path("/mapping/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiParamMappingDetail(@PathParam("id") Integer id) {
        try {
            InterfaceParamMappingDetail detail = getService().getApiParamMappingDetail(id);
            return Response.ok().entity(ParamUtil.parseObjectToJson(detail)).build();
        } catch (Exception e) {
            return WebExceptionHandler.handle(e);
        }
    }

    private InterfaceIntegrationService getService() {
        return ServiceUtil.getService(InterfaceIntegrationServiceImpl.class);
    }

}
